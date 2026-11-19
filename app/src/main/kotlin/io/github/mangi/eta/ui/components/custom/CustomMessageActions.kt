package io.github.mangi.eta.ui.components.custom

import android.content.Context
import io.github.mangi.eta.agent.skill.SkillInstallResult
import io.github.mangi.eta.agent.skill.SkillPackageLimits
import io.github.mangi.eta.agent.skill.SkillResourceReadResult
import io.github.mangi.eta.agent.skill.SkillRuntime
import io.github.mangi.eta.data.repository.AgentMemoryMutation
import io.github.mangi.eta.data.repository.AgentMemoryRepository
import io.github.mangi.eta.data.repository.AgentMemoryStore
import io.github.mangi.eta.data.repository.AgentMemoryWriteResult
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.CodingErrorAction
import java.nio.CharBuffer
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

/** Explicit user saves: no model extraction, content filtering, replacement, or private DB access. */
internal object CustomMessageActions : CustomMessagePersistence {
    override suspend fun saveMemory(context: Context, text: String): String = withContext(Dispatchers.IO) {
        require(text.isNotEmpty()) { "记忆内容不能为空" }
        // Manual user saves use the store's 1 MiB UTF-8 file quota, not the model tool's
        // 3500-character argument schema. The merged-file quota is checked under its lock.
        require(strictUtf8(text).size <= AgentMemoryStore.MAX_FILE_BYTES) {
            "记忆正文不能超过 1 MiB UTF-8；追加后的记忆文件总大小也不能超过 1 MiB，不会截断正文"
        }
        // EtaApp initializes the singleton. Do not create another AgentMemoryStore: its lock
        // would not be shared with official editor/tool writes.
        var revision = AgentMemoryRepository.snapshot().revision
        repeat(2) { attempt ->
            currentCoroutineContext().ensureActive()
            when (val result = AgentMemoryRepository.mutate(AgentMemoryMutation.Append(revision, text))) {
                is AgentMemoryWriteResult.Success -> {
                    check(AgentMemoryRepository.snapshot().revision == result.snapshot.revision) {
                        "记忆追加后读回版本不一致（可能有并发修改），未确认结果；请检查记忆后再重试"
                    }
                    return@withContext "已追加到记忆 MEMORY.md，读回验证通过（${result.snapshot.byteSize} 字节；版本 ${result.snapshot.revision.take(12)}）"
                }
                is AgentMemoryWriteResult.Conflict -> {
                    if (attempt == 1) error("记忆正在被其他操作修改，未保存；请重试")
                    revision = result.snapshot.revision
                }
            }
        }
        error("记忆未保存")
    }

    override suspend fun saveSkill(context: Context, text: String): String = withContext(Dispatchers.IO) {
        val document = CustomMessageSkillFormat.document(text)
        val bytes = strictUtf8(document.markdown)
        require(bytes.size.toLong() <= SkillPackageLimits().maxSkillFileBytes) {
            "技能文件不能超过 ${SkillPackageLimits().maxSkillFileBytes} 字节（含元数据）"
        }
        val archive = ByteArrayOutputStream().also { buffer ->
            ZipOutputStream(buffer).use { zip ->
                zip.putNextEntry(ZipEntry("${document.id}/SKILL.md"))
                zip.write(bytes)
                zip.closeEntry()
            }
        }.toByteArray()
        val coroutineContext = currentCoroutineContext()
        coroutineContext.ensureActive()
        // The v343 installer strictly validates YAML metadata and quotas, takes the official
        // cross-process mutation lock, stages/atomically commits, registers source=user and
        // enabled=true, invalidates the shared index cache, and rolls back on commit failure.
        // Never enable replacement. Only an exactly identical user file can be re-registered below.
        when (val result = SkillRuntime.createPackageInstaller(context.applicationContext).installLocalZip(
            openStream = { ByteArrayInputStream(archive) },
            replaceUserSkill = false,
            isCancelled = { !coroutineContext.isActive },
        )) {
            is SkillInstallResult.Success -> {
                check(result.installed.singleOrNull()?.id == document.id) {
                    "安装返回的技能标识不匹配；请检查技能管理页"
                }
                "已保存并启用用户技能 ${document.id}（SKILL.md，${bytes.size} 字节）"
            }
            is SkillInstallResult.Conflict -> {
                coroutineContext.ensureActive()
                val index = SkillRuntime.createIndexService(context.applicationContext)
                // Same official reentrant cross-process lock spans read/compare/register/readback.
                // No file replacement, even for a hash collision, malformed file or builtin ID.
                index.withMutationLock {
                    coroutineContext.ensureActive()
                    check(result.conflicts.size == 1 && result.conflicts.single().id == document.id &&
                        result.conflicts.single().existingSource == "user") {
                        "技能标识冲突；未覆盖任何已有技能"
                    }
                    val registry = index.captureRegistryRecoverySnapshots(listOf(document.id)).single()
                    check(!registry.entryExisted || registry.source == "user") {
                        "技能 ID 与非用户技能冲突，拒绝修改"
                    }
                    val expectedRoot = File(SkillRuntime.skillsRoot(context), document.id).absoluteFile
                    val entries = index.listSkillsForManagement(forceRefresh = true)
                        .filter { it.id == document.id && it.installed }
                    check(entries.size == 1) { "已有技能索引不唯一或解析失败；未覆盖任何文件" }
                    val entry = entries.single()
                    check(entry.source == "user" && File(entry.rootPath).canonicalFile == expectedRoot &&
                        expectedRoot.canonicalFile == expectedRoot) {
                        "已有技能路径或来源不符；未覆盖任何文件"
                    }
                    val read = SkillRuntime.createResourceReader(context).readText(entry, "SKILL.md")
                    // v342 used the same content hash, but a fixed Chinese description.
                    // Accept ONLY either complete audited wrapper byte-for-byte; never normalize body.
                    val legacy = strictUtf8("---\nname: ${document.id}\ndescription: 手动保存的对话消息，供需要复用该条对话内容时参考\n---\n\n$text")
                    check(read is SkillResourceReadResult.Success &&
                        (strictUtf8(read.text).contentEquals(bytes) || strictUtf8(read.text).contentEquals(legacy))) {
                        "技能内容冲突或读回不一致；未覆盖任何已有技能"
                    }
                    index.registerInstalledUserSkills(listOf(document.id))
                    check(index.findInstalledSkill(document.id)?.let { it.source == "user" && it.enabled } == true) {
                        "技能内容已保留，但启用或索引刷新尚未确认；请检查技能列表"
                    }
                    "技能已保存并启用（已存在且内容一致，读回验证通过）：${document.id}"
                }
            }
            is SkillInstallResult.Failure -> error(
                "技能保存失败 [${result.error.code}]：${result.error.message}" +
                    if (result.recoveryRequired) "；官方安装器保留了恢复备份，请先处理技能恢复" else "",
            )
        }
    }
    private fun strictUtf8(text: String): ByteArray {
        val encoded = Charsets.UTF_8.newEncoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .encode(CharBuffer.wrap(text))
        return ByteArray(encoded.remaining()).also { encoded.get(it) }
    }
}
