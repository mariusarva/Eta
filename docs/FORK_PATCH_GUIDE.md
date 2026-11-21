# fork 补丁与发版流程

本文件是操作手册：每次改动如何落日志、如何出包、如何发 Release。

---

## 1. 改动落地

```bash
cd /workspace/tmp-work/eta-fork

# 1) 改代码
# 2) 本地无法编译（chroot 无 Android SDK），直接推 CI 验证
git add -A
git commit -m "fix(ui): 简短描述"

# 3) 推送（网络不稳，失败就重试；也常见 non-fast-forward，先 pull --rebase）
for i in 1 2 3 4; do timeout 150 git push origin main 2>&1 | tail -1 && break; sleep 8; done
```

**注意**：并发会话可能同时推 main，push 被拒先 `git pull --rebase origin main` 再推。
rebase 有丢改动的前例（`deleteSingleMessage` 丢过一次），rebase 后必须 `grep` 确认关键符号还在。

## 2. 构建

主仓库 `mariusarva/Eta` 的 Actions 被平台禁用（API 显示 enabled，dispatch 返回 422），
统一走中转仓库 `mariusarva/eta-ci`：

```bash
SHA=$(cd /workspace/tmp-work/eta-fork && git rev-parse HEAD)
gh workflow run build.yml --repo mariusarva/eta-ci -f ref=$SHA -f forkVersion=<N>
```

- `ref` 必须是**完整 40 位 SHA**；短 SHA 或分支名会 fetch 失败。
- `forkVersion` 写入 `versionName` 与 `versionCode`。

## 3. 下载产物

GitHub artifact 直连很慢（26MB 常要 20–40 分钟且中途断），用分段并行脚本：

```bash
TOKEN=$(cd /workspace/tmp-work/eta-fork && gh auth token)
AID=$(gh api "/repos/mariusarva/eta-ci/actions/runs/<RUN_ID>/artifacts" -q '.artifacts[0].id')
curl -s -o /dev/null -w '%{redirect_url}' \
  -H "Authorization: Bearer $TOKEN" -H "Accept: application/vnd.github+json" \
  "https://api.github.com/repos/mariusarva/eta-ci/actions/artifacts/$AID/zip" > /tmp/durl.txt
bash /workspace/dl_seg.sh    # 8 段并行 + 分段重试，约 1 分钟
unzip -o -q /workspace/art14.zip -d /workspace/tmp-artN
```

## 4. 发 Release

```bash
cd /workspace/tmp-artN
mv "Eta-v3.4.4-fork.N+<sha>.apk" "Eta-v3.4.4-fork.N.apk"
cat > RELEASE_NOTES.md <<'MD'
# Eta fork 3.4.4-fork.N（一句话）

## 现象 / 定位 / 改动 / 验证
...
MD
cp /workspace/tmp-art7/FORK_FEATURES.md .

gh release create v3.4.4-fork.N --repo mariusarva/Eta --target <完整SHA> \
  --title "..." --notes-file RELEASE_NOTES.md
for i in 1 2 3 4 5 6 7 8; do
  gh release upload v3.4.4-fork.N --repo mariusarva/Eta \
    "Eta-v3.4.4-fork.N.apk" RELEASE_NOTES.md FORK_FEATURES.md --clobber && break
  sleep 8
done
```

- `--target` 必须是完整 40 位 SHA，7 位会 422。
- Release 附件上传偶发 TLS 中断，脚本里带重试。
- **tag 只能用一次**：`gh release create` 报 `tag_name already exists` 说明该 tag 被占用，
  换下一个序号，不要删旧 tag 复用（会与已发布的 APK 混淆）。

## 5. 安装

```bash
cp /workspace/tmp-artN/Eta-v3.4.4-fork.N.apk /data/local/tmp/
pm install -r -d /data/local/tmp/Eta-v3.4.4-fork.N.apk
dumpsys package io.github.mangi.eta | grep -m1 versionName
```

- 必须用 `/data/local/tmp/` 路径；`/storage/emulated/0/...` 会因 system_server 的 SELinux
  拒绝读 fuse 而失败（`Can't open file`）。
- `-d` 允许降级；签名固定后正常升级不需要它，但保留无害。

## 6. 日志

每次改动都要更新 `docs/CHANGELOG-FORK.md`，格式固定：

```
## fork.N（日期）标题

**现象**：用户看到什么。
**定位**：查到什么证据（日志原文、代码路径、DB 查询结果）。
**改动**：改了哪些文件、为什么这样改。
**验证**：怎么确认有效（真机现象、gfxinfo 数字、DB 行数）。
```

失败的尝试也写进去，标清楚为什么失败、怎么绕开的。这是给未来的自己看的。

## 7. 常见坑

| 现象 | 原因 | 处理 |
|---|---|---|
| 启动闪退，`IllegalStateException` 结构不符 | `@Entity` 没声明迁移里建的索引 | 补 `indices = [...]`，版本号 +1 |
| 编译报 `Unresolved reference` | 以旧 tag 文件覆盖了 main 版本 | 一律以 main 为底手工改 |
| `git push` 被拒 | 并发会话已推 | `pull --rebase` 后再推，并核对改动没丢 |
| `pm install` 报 `Can't open file` | 用了 `/storage` 路径 | 拷到 `/data/local/tmp/` |
| artifact 下载断流 | 直连 blob 存储不稳 | 用 `dl_seg.sh` 分段并行 |
| 检查更新永远说无新版 | `isNewer` 剥掉了 fork 序号 | 已在 fork.22 修，保留 fork 序号比较 |
