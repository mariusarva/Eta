package io.github.mangi.eta.data.repository

import android.content.Context
import androidx.room.withTransaction
import io.github.mangi.eta.data.db.ConversationContextCheckpointEntity
import io.github.mangi.eta.data.db.ConversationEntity
import io.github.mangi.eta.data.db.ConversationMessageEntity
import io.github.mangi.eta.data.db.ConversationStateEntity
import io.github.mangi.eta.data.db.EtaDatabase
import io.github.mangi.eta.data.db.McpServerEntity
import io.github.mangi.eta.data.db.ProviderEntity
import io.github.mangi.eta.data.db.ProviderModelEntity
import io.github.mangi.eta.data.db.ProviderWithModelsSeed
import io.github.mangi.eta.data.db.ScheduledTaskEntity
import io.github.mangi.eta.data.db.ScheduledTaskRunEntity
import io.github.mangi.eta.data.db.SkillRegistryEntity
import io.github.mangi.eta.data.datastore.SettingsDataStore
import io.github.mangi.eta.data.model.AssistantOverlaySettings
import io.github.mangi.eta.data.model.ConversationTitleSettings
import io.github.mangi.eta.data.model.ProviderAuthModes
import io.github.mangi.eta.data.model.SpeechSettings
import io.github.mangi.eta.agent.scheduler.ScheduledTaskAlarmScheduler
import io.github.mangi.eta.R
import io.github.mangi.eta.core.AndroidAgentLogger
import io.github.mangi.eta.core.AppStrings
import io.github.mangi.eta.core.safeLogType
import io.github.mangi.eta.core.safeStackTrace
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import java.io.InputStream
import java.io.OutputStream

/** Eta 用户数据备份；旧备份没有角色字段时保留设备上的角色库。 */
@Serializable
internal data class EtaBackupDocument(
    val format: String = FORMAT,
    val schemaVersion: Int = SCHEMA_VERSION,
    val exportedAt: Long,
    val providers: List<EtaBackupProvider> = emptyList(),
    val catalogRevision: Int = 0,
    val selectedProviderId: String? = null,
    val selectedModelId: String? = null,
    val conversations: List<ConversationEntity> = emptyList(),
    val messages: List<ConversationMessageEntity> = emptyList(),
    val contextCheckpoints: List<ConversationContextCheckpointEntity> = emptyList(),
    val conversationState: ConversationStateEntity? = null,
    val memoryMd: String = "",
    val roleplay: CharacterBackupData? = null,
    /** 定时任务（含运行记录）。 */
    val scheduledTasks: List<ScheduledTaskEntity> = emptyList(),
    val scheduledTaskRuns: List<ScheduledTaskRunEntity> = emptyList(),
    /** 技能启用状态（技能文件本身在 files/skills，全量包才带）。 */
    val skills: List<SkillRegistryEntity> = emptyList(),
    /** MCP 服务器配置；凭据不进备份，恢复后需重新授权。 */
    val mcpServers: List<McpServerEntity> = emptyList(),
    /** 语音、朗读、助手浮窗等设置。 */
    val speechSettings: SpeechSettings? = null,
    val assistantOverlaySettings: AssistantOverlaySettings? = null,
    /** 会话自动标题（含所用模型与推理强度）。 */
    val conversationTitleSettings: ConversationTitleSettings? = null,
) {
    companion object {
        const val FORMAT = "eta-backup"
        // 旧版会把未知原生 Provider 当作 OpenAI-compatible，必须通过备份版本阻止错误恢复。
        // v5：新增定时任务、技能、MCP、语音设置；旧版文件缺这些字段，按空处理即可。
        const val SCHEMA_VERSION = 5
    }
}

@Serializable
internal data class EtaBackupProvider(
    val provider: ProviderEntity,
    val models: List<ProviderModelEntity> = emptyList(),
)

internal data class EtaBackupSummary(
    val providerCount: Int,
    val modelCount: Int,
    val conversationCount: Int,
    val messageCount: Int,
    val memoryBytes: Int,
    val characterCount: Int = 0,
)

internal class EtaBackupException(message: String, cause: Throwable? = null) :
    IllegalArgumentException(message, cause)

internal object EtaBackupRepository {
    private const val EXPORT_PAGE_SIZE = 64

    suspend fun export(context: Context, output: OutputStream): EtaBackupSummary = withContext(Dispatchers.IO) {
        var phase = "prepare_export"
        try {
            val appContext = context.applicationContext
            CharacterRepository.initialize(appContext)
            val database = EtaDatabase.get(appContext)
            val settings = SettingsDataStore.settings()
            val catalogRevision = SettingsDataStore.officialModelCatalogRevision()
            EtaBackupJsonStreams.export(appContext, output, onPhase = { phase = it }) { writer ->
                database.withTransaction {
                    val dao = database.conversationDao()
                    val providers = database.providerDao().providers().map {
                        EtaBackupProvider(it.provider.withoutOAuthSecrets(), it.models)
                    }
                    val memory = AgentMemoryRepository.snapshot().content
                    writer.value("format", String.serializer(), EtaBackupDocument.FORMAT)
                    writer.value("schemaVersion", Int.serializer(), EtaBackupDocument.SCHEMA_VERSION)
                    writer.value("exportedAt", Long.serializer(), System.currentTimeMillis())
                    writer.value("providers", ListSerializer(EtaBackupProvider.serializer()), providers)
                    writer.value("catalogRevision", Int.serializer(), catalogRevision)
                    writer.value("selectedProviderId", String.serializer().nullable, settings.selectedProviderId)
                    writer.value("selectedModelId", String.serializer().nullable, settings.selectedModelId)
                    writer.value("conversationState", ConversationStateEntity.serializer().nullable, dao.state())
                    writer.value("memoryMd", String.serializer(), memory)
                    val boundAvatars = linkedMapOf<String, String?>()
                    var conversationCount = 0
                    var messageCount = 0
                    phase = "export_conversations"
                    writer.beginArray(EtaBackupJsonStreams.CONVERSATIONS)
                    exportRows({ dao.conversationEntityRowsPage(EXPORT_PAGE_SIZE, it) }) { raw ->
                        val row = dao.restoreConversation(raw)
                        writer.record(ConversationEntity.serializer(), row)
                        CharacterBackupTransfer.avatarReference(row)?.let { (id, avatar) ->
                            if (id !in boundAvatars) boundAvatars[id] = avatar
                        }
                        conversationCount++
                    }
                    writer.endArray()
                    phase = "export_messages"
                    writer.beginArray(EtaBackupJsonStreams.MESSAGES)
                    exportRows({ dao.allMessageRowsPage(EXPORT_PAGE_SIZE, it) }) { raw ->
                        writer.record(ConversationMessageEntity.serializer(), dao.restoreMessage(raw))
                        messageCount++
                    }
                    writer.endArray()
                    phase = "export_checkpoints"
                    writer.beginArray(EtaBackupJsonStreams.CHECKPOINTS)
                    exportRows({ dao.contextCheckpointRowsPage(EXPORT_PAGE_SIZE, it) }) { raw ->
                        writer.record(ConversationContextCheckpointEntity.serializer(), dao.restoreCheckpoint(raw))
                    }
                    writer.endArray()
                    phase = "export_roleplay"
                    val roleplay = CharacterBackupTransfer.snapshot(appContext, boundAvatars)
                    writer.value("roleplay", CharacterBackupData.serializer().nullable, roleplay)
                    // 定时任务、技能、MCP、语音设置：数量都很小，直接进 header。
                    phase = "export_scheduled"
                    val tasks = runCatching { database.scheduledTaskDao().allTasks() }.getOrDefault(emptyList())
                    val taskRuns = runCatching {
                        tasks.flatMap { task -> database.scheduledTaskDao().listRuns(task.id, SCHEDULED_RUN_LIMIT) }
                    }.getOrDefault(emptyList())
                    writer.value("scheduledTasks", ListSerializer(ScheduledTaskEntity.serializer()), tasks)
                    writer.value("scheduledTaskRuns", ListSerializer(ScheduledTaskRunEntity.serializer()), taskRuns)
                    phase = "export_skills"
                    val skills = runCatching { database.skillDao().registryEntries() }.getOrDefault(emptyList())
                    writer.value("skills", ListSerializer(SkillRegistryEntity.serializer()), skills)
                    phase = "export_mcp"
                    val mcpServers = runCatching { database.mcpServerDao().serverRows() }.getOrDefault(emptyList())
                    writer.value("mcpServers", ListSerializer(McpServerEntity.serializer()), mcpServers)
                    phase = "export_speech"
                    val speech = runCatching { SettingsDataStore.speechSettings() }.getOrNull()
                    val overlay = runCatching { SettingsDataStore.assistantOverlaySettings() }.getOrNull()
                    writer.value("speechSettings", SpeechSettings.serializer().nullable, speech)
                    writer.value(
                        "assistantOverlaySettings",
                        AssistantOverlaySettings.serializer().nullable,
                        overlay,
                    )
                    val titleSettings = runCatching { SettingsDataStore.conversationTitleSettings() }.getOrNull()
                    writer.value(
                        "conversationTitleSettings",
                        ConversationTitleSettings.serializer().nullable,
                        titleSettings,
                    )
                    EtaBackupSummary(providers.size, providers.sumOf { it.models.size }, conversationCount,
                        messageCount, memory.toByteArray(Charsets.UTF_8).size, roleplay.characters.size)
                }
            }
        } catch (failure: Throwable) {
            logFailure("export", phase, failure)
            throw failure
        }
    }

    suspend fun import(
        context: Context,
        input: InputStream,
        onCommitted: () -> Unit = {},
        forgetCredentials: (String) -> Unit = ProviderCredentialRepository::forget,
    ): EtaBackupSummary = withContext(Dispatchers.IO) {
        var phase = "stage_import"
        try {
            val appContext = context.applicationContext
            EtaBackupJsonStreams.stage(appContext, input) { phase = it }.use { staged ->
                phase = "validate_import"
                val summary = EtaBackupValidation.validate(staged)
                val document = staged.header
                val database = EtaDatabase.get(appContext)
                val previousMemory = AgentMemoryRepository.snapshot().content
                val previousRoleMemories = document.roleplay?.assets.orEmpty().associate {
                    it.characterId to CharacterMemoryRepository.snapshot(appContext, it.characterId).content
                }
                phase = "restore_assets"
                val avatarPaths = document.roleplay?.let { CharacterBackupTransfer.restoreAssets(appContext, it) }.orEmpty()
                val cancellation = currentCoroutineContext()
                val oauthProviders = document.providers.map { it.provider }
                    .filter { ProviderAuthModes.isOAuth(it.authMode) }
                val credentialIds = document.providers.mapTo(linkedSetOf()) { it.provider.id }
                // 显式检查原任务取消，使 Room 提交到凭据清理之间不留下被取消打断的空隙。
                withContext(NonCancellable) {
                    var memoryWriteStarted = false
                    try {
                        database.withTransaction {
                            cancellation.ensureActive()
                            phase = "restore_providers"
                            credentialIds += database.providerDao().providers().map { it.provider.id }
                            database.providerDao().replaceAll(document.providers.map {
                                ProviderWithModelsSeed(it.provider.awaitingCredentialCleanup(), it.models)
                            })
                            val dao = database.conversationDao()
                            phase = "clear_conversations"
                            dao.deleteMessages()
                            dao.deleteContextCheckpoints()
                            dao.deleteConversations()
                            dao.deleteState()
                            phase = "restore_conversations"
                            staged.conversations { row ->
                                cancellation.ensureActive()
                                dao.insertConversations(listOf(CharacterBackupTransfer.remapConversation(row, avatarPaths)))
                            }
                            phase = "restore_messages"
                            staged.messages {
                                cancellation.ensureActive()
                                dao.insertMessages(listOf(it))
                            }
                            phase = "restore_checkpoints"
                            staged.checkpoints {
                                cancellation.ensureActive()
                                dao.insertContextCheckpoints(listOf(it))
                            }
                            document.conversationState?.let { dao.insertState(it) }
                            phase = "restore_roleplay"
                            document.roleplay?.let { roleplay ->
                                database.characterDao().replaceAll(
                                    roleplay.characters.map { it.copy(avatarPath = avatarPaths[it.id]) }, roleplay.persona,
                                )
                            }
                            phase = "restore_memory"
                            cancellation.ensureActive()
                            memoryWriteStarted = true
                            AgentMemoryRepository.replaceAll(document.memoryMd)
                            document.roleplay?.let { CharacterBackupTransfer.restoreMemories(appContext, it) }
                            cancellation.ensureActive()
                        }
                        // 定时任务 / 技能 / MCP / 语音设置：各自独立事务，
                        // 某一类失败不连累已恢复的会话数据，只把错误累积到日志。
                        restoreAuxiliary(appContext, database, document)
                    } catch (failure: Throwable) {
                        if (memoryWriteStarted) {
                            try { AgentMemoryRepository.replaceAll(previousMemory) } catch (restoreFailure: Throwable) {
                                failure.addSuppressed(restoreFailure)
                            }
                            previousRoleMemories.forEach { (id, content) ->
                                try { CharacterMemoryRepository.replaceAll(appContext, id, content) } catch (restoreFailure: Throwable) {
                                    failure.addSuppressed(restoreFailure)
                                }
                            }
                        }
                        CharacterBackupTransfer.discardAssets(avatarPaths, failure)
                        throw failure
                    }
                    onCommitted()
                    try {
                        phase = "clear_provider_credentials"
                        credentialIds.forEach(forgetCredentials)
                        phase = "activate_provider_connections"
                        database.withTransaction {
                            oauthProviders.forEach { provider ->
                                check(database.providerDao().activateRestoredOAuthProvider(
                                    provider.id, provider.authMode, provider.isEnabled,
                                ) == 1)
                            }
                        }
                    } catch (failure: Exception) {
                        throw IllegalStateException(
                            AppStrings.get(
                                R.string.backup_err_import_oauth_cleanup_incomplete,
                                "数据已导入，但订阅登录清理未完成。订阅连接保持停用，请重新选择登录方式并登录。",
                            ),
                            failure,
                        )
                    }
                    try {
                        phase = "restore_settings"
                        SettingsDataStore.setSelection(document.selectedProviderId, document.selectedModelId)
                        SettingsDataStore.setOfficialModelCatalogRevision(document.catalogRevision)
                        ProviderRepository.ensureBuiltInsMerged()
                        ProviderRepository.repairSelection()
                    } catch (failure: Exception) {
                        throw IllegalStateException(
                            AppStrings.get(
                                R.string.backup_err_import_default_model_incomplete,
                                "数据已导入，但默认模型设置未完全恢复，请重新选择服务商和模型。",
                            ),
                            failure,
                        )
                    }
                }
                summary
            }
        } catch (failure: Throwable) {
            logFailure("import", phase, failure)
            throw failure
        }
    }

    suspend fun inspect(context: Context, input: InputStream): EtaBackupSummary = withContext(Dispatchers.IO) {
        EtaBackupJsonStreams.stage(context.applicationContext, input).use { EtaBackupValidation.validate(it) }
    }

    private suspend fun <T> exportRows(load: suspend (Int) -> List<T>, write: suspend (T) -> Unit) {
        var offset = 0
        while (true) {
            currentCoroutineContext().ensureActive()
            val page = load(offset)
            for (row in page) {
                currentCoroutineContext().ensureActive()
                write(row)
            }
            if (page.size < EXPORT_PAGE_SIZE) return
            offset += page.size
        }
    }

    private fun ProviderEntity.withoutOAuthSecrets(): ProviderEntity =
        if (authMode == ProviderAuthModes.API_KEY) this else copy(apiKey = "")

    private fun ProviderEntity.awaitingCredentialCleanup(): ProviderEntity =
        if (ProviderAuthModes.isOAuth(authMode)) {
            // 即使提交后进程中断，导入的相同 ID 也不能接上设备中尚未清除的旧授权。
            copy(authMode = ProviderAuthModes.API_KEY, apiKey = "", isEnabled = false)
        } else withoutOAuthSecrets()

    private fun logFailure(operation: String, phase: String, failure: Throwable) {
        if (failure is CancellationException) return
        AndroidAgentLogger.error("Agent backup failed: operation=$operation phase=$phase " +
            "type=${failure.safeLogType()}\n${failure.safeStackTrace()}")
    }

    /**
     * 恢复定时任务、技能、MCP、语音设置。
     *
     * 每一类各自独立事务：某一类失败只记录日志，不连累已经恢复的会话数据。
     * 旧版备份没有这些字段时全部为空列表/空值，跳过即可。
     */
    private suspend fun restoreAuxiliary(
        context: Context,
        database: EtaDatabase,
        document: EtaBackupDocument,
    ) {
        if (document.scheduledTasks.isNotEmpty() || document.scheduledTaskRuns.isNotEmpty()) {
            runCatching {
                database.withTransaction {
                    val dao = database.scheduledTaskDao()
                    dao.deleteAllRuns()
                    dao.deleteAllTasks()
                    document.scheduledTasks.forEach { dao.upsertTask(it) }
                    document.scheduledTaskRuns.forEach { dao.upsertRun(it) }
                }
                ScheduledTaskAlarmScheduler.syncNextAlarm(context)
            }.onFailure {
                AndroidAgentLogger.warn("Backup restore scheduled tasks skipped: type=${it.safeLogType()}")
            }
        }
        if (document.skills.isNotEmpty()) {
            runCatching {
                database.withTransaction { database.skillDao().replaceRegistry(document.skills) }
            }.onFailure {
                AndroidAgentLogger.warn("Backup restore skills skipped: type=${it.safeLogType()}")
            }
        }
        if (document.mcpServers.isNotEmpty()) {
            runCatching {
                database.withTransaction {
                    val dao = database.mcpServerDao()
                    dao.deleteAllRows()
                    // 凭据不进备份：恢复的服务器一律先停用，等用户重新授权。
                    document.mcpServers.forEach { row ->
                        dao.insert(row.copy(enabled = false, toolsJson = "[]"))
                    }
                }
            }.onFailure {
                AndroidAgentLogger.warn("Backup restore mcp skipped: type=${it.safeLogType()}")
            }
        }
        document.speechSettings?.let { settings ->
            runCatching { SettingsDataStore.setSpeechSettings(settings) }.onFailure {
                AndroidAgentLogger.warn("Backup restore speech skipped: type=${it.safeLogType()}")
            }
        }
        document.assistantOverlaySettings?.let { settings ->
            runCatching { SettingsDataStore.setAssistantOverlaySettings(settings) }.onFailure {
                AndroidAgentLogger.warn("Backup restore overlay skipped: type=${it.safeLogType()}")
            }
        }
        document.conversationTitleSettings?.let { settings ->
            runCatching { SettingsDataStore.setConversationTitleSettings(settings) }.onFailure {
                AndroidAgentLogger.warn("Backup restore title settings skipped: type=${it.safeLogType()}")
            }
        }
    }
}
