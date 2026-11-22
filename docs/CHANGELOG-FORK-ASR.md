# 离线语音落地日志（2026-11-19，v3.4.4+b5fe773）

本文件记录把 Vosk / SenseVoice / Qwen3-ASR 三引擎离线识别真正跑通的完整过程。
每一条都是先出现现象、再有定位证据、最后是改动，便于以后回溯。

---

## 1. 三引擎分派恢复：sherpa 反编译产物不能编译

**现象**：`LocalSpeechRecognizer` 只能分派 Vosk，GGUF / ONNX 直接抛 `UnsupportedOperationException`。

**定位**：把 `com.k2fsa.sherpa.onnx` 的 27 个类从参考 APK 用 jadx 反编译后放进
`app/src/main/java`，本地 `javac` 直接报 **100 处** `method getXxx() is already defined`：
Kotlin `data class` 编译后已有 `getXxx()`，jadx 又把属性访问器当方法再生成一遍。

```
FeatureConfig.java:77: error: method getSampleRate() is already defined in class FeatureConfig
OfflineRecognizerConfig.java:181: error: method getFeignConfig() is already defined ...
```

**改动**：删掉 jadx 产物，改用 k2-fsa/sherpa-onnx 官方 `sherpa-onnx/kotlin-api/` 源码：

| 文件 | 作用 |
| --- | --- |
| `OfflineRecognizer.kt` | 识别器 + 全部 `Offline*ModelConfig` / `OfflineRecognizerConfig` |
| `OfflineStream.kt` | 流对象与 `acceptWaveform` |
| `FeatureConfig.kt` / `HomophoneReplacerConfig.kt` / `QnnConfig.kt` | 被 modelConfig 引用 |

**为什么安全**：JNI 用 `GetFieldID` 按字段名取值。逐类比对了 jadx 产物与官方源码的字段集合，
结果全部是「jadx 字段 ⊆ 官方字段」，缺口为空（`OfflineModelConfig` 25 个、`OfflineRecognizerConfig` 11 个、
`OfflineQwen3AsrModelConfig` 10 个……）。字段多出来不影响，少一个才会读空。

---

## 2. catalog 全部 8 个模型恢复可选

**现象**：设置页里 6 个大模型是灰的，无法下载。

**原因**：上一轮为了「不让用户点到装不上的模型」，把这 6 个条目标成 `available=false`。
根子是引擎没接上，不是模型本身有问题。

**改动**：8 个条目全部 `available=true`、`availabilityReason=null`，恢复推荐理由文案。
`LocalSpeechRegistry.selectionOrDefault` 去掉「Vosk 优先」的回退逻辑。

---

## 3. 本地模型不能做文件转写

**现象**：录音结束后报「本地模型不支持文件转写，请改用实时识别」。

**原因**：`FileSpeechRecognition.recognizeSpeechFile()` 的 LOCAL 分支被写成直接 `throw`。

**改动**：新增 `LocalSpeechFileTranscription`：

- 录音本身就是 16 kHz / 单声道 / PCM16（`SpeechRecorder.RATE = 16_000`），直接取 RIFF 的 `data` 块；
- 服务端单会话上限 30 秒 PCM（`MAX_AUDIO_BYTES`），所以按 **28 秒**切段；
- 每段通过 `LocalSpeechEngine.recognize()` 走一次独立会话（warm cache 复用模型），拼接结果；
- 格式不符（非 16 kHz / 非单声道 / 非 PCM16）明确报错，不做静默重采样。

---

## 4. 输入栏：插入按钮挤压发送键

**现象**：运行中出现「暂停」「插入」两个按钮后，发送键被压扁。

**原因**：`Row` 里非 weight 子项拿到的是「剩余宽度」约束。前面按钮变多后，末尾 36 dp 的发送键
被压到小于 36 dp。

**改动**（`AgentChatInputBar.kt`）：

- 发送键 `size(36.dp)` → `requiredSize(36.dp)`：不受 Row 剩余约束影响；
- 暂停 / 插入按钮 `ChatInputActionSize(40dp)` → `RunActionButtonSize(36dp)`，同时 `requiredSize`；
- 两者与相邻按钮间距 `10dp` → `4dp`；
- 运行中隐藏「上下文用量」按钮，把宽度让出来。

---

## 5. 语音测试没有完成按钮

**现象**：点「测试识别」开始录音后，界面上只有「取消」，不知道怎样结束并提交。

**原因**：设计上「点整行」等于完成（标题会变成「完成录音」），但没有可见按钮。

**改动**：`state.active` 时在右侧同时给出「完成」（`CouiButtonRole.Primary`）与「取消」，
COUI 与 MIUIX 两套样式都改。

---

## 6. 「本机离线」出现三个

**现象**：语音服务下拉里三个同名「本机离线」。

**原因**：`AsrProvider` 有 `LOCAL` / `SHERPA_NCNN` / `QWEN3_LOCAL` 三个枚举，`label()` 都映射到
同一个字符串 `speech_asr_local_engine`。

**改动**：UI 列表过滤掉后两个（枚举保留，旧设置仍可反序列化）：

```kotlin
val asrOptions = AsrProvider.entries.filter { it != AsrProvider.SHERPA_NCNN && it != AsrProvider.QWEN3_LOCAL }
selectedIndex = asrOptions.indexOf(settings.asr)
```

---

## 7. 推荐模型下载不了（网络层）

**现象**：点下载直接失败。

**定位**：本机网络访问 `huggingface.co` TLS 直接断（curl exit 35）；`hf-mirror.com` 对这批仓库
一律 `308 → huggingface.co`，等于不可用。

**改动**：

1. 新增 `.github/workflows/model-mirror.yml`：在 runner 上下载 catalog 里 7 个固定版本文件，
   逐个核对 `sha256` 与 `sizeBytes`，全部通过才发布到 Release `local-models-v1`；
2. `LocalSpeechDownload` 下载顺序改为「GitHub Release 镜像 → huggingface 原址」，
   `allowedUrl` 放行 `github.com` 的 Release 路径与 `*.githubusercontent.com` 重定向目标；
3. 摘要校验没有放松：安装阶段仍按 catalog 的 `sha256` 全量校验。

---

## 8. 下载到百分之十几就退出（真正的断流）

**现象**：进度 10–15% 就停，界面回到「未安装 / 待检测」。

**证据**：同一条链路用 curl 拉同一个文件，稳定在 **197 MB / 240 MB（81%）** 处断：

```
curl: (56) OpenSSL SSL_read: OpenSSL/3.5.7: error:0A000126:SSL routines::unexpected eof while reading
```

这不是超时，是连接被中途掐断。

**旧代码为什么糟糕**：`attemptDownload` 结束时用
`require(dest.length() == file.sizeBytes)` 收口。字节不够 → `IllegalArgumentException`
→ 走 `install` 的非 NETWORK 分支 → `finally` 直接 `deleteRecursively()` 删掉 stage。
于是已经下好的几百 MB 全丢，用户每次都要从 0 开始。

**改动**（`LocalSpeechDownload.kt` / `LocalSpeechModelStore.kt`）：

- `readTimeout` 5 s → 30 s；
- stage 目录从 `.stage-<id>-<uuid>` 改为固定的 `.stage-<id>`：取消、进程被回收后重来可以续传；
- `Range: bytes=N-`，服务器回 200 表示忽略 Range 则从头重写，回 206 则续写；
- 长度收口交给「实际落盘字节数」：超过才算格式错误，不够一律抛 `IOException` 交给外层续传；
- 重试从「固定 4 次」改为「**连续 3 次毫无进展**才换源」，只要有前进就一直续
  （253 MB 在这条链路通常需要 2–3 轮）；
- `install` 默认 **保留** stage，只有校验失败才清理。

**效果**：同一份 253 MB 文件，用同样的循环续传策略在 2 轮内拉完。

---

## 9. 下载与页面生命周期解耦

**现象**：切走设置页 / 切到后台后下载就没了。

**原因**：下载跑在 `rememberCoroutineScope()` 里，离开页面即取消。

**改动**：新增 `LocalSpeechDownloads.scope`（`SupervisorJob + Main.immediate`），
`operate(..., background = true)` 的下载走进程级 scope。用户主动点取消仍生效（取消的是页面上的 Job）。

---

## 10. 装上了但一直「待检测 / 无法启用」（缺许可资产）

**现象**：推荐模型下载完成后一直显示「待检测」，Vosk small 却能装。

**定位**：stage 目录里只有 `installation.json`，缺 `LICENSE.txt` 与 `catalog-entry.json`——
`writeMetadata()` 写第二个文件时抛异常。查 assets：

```
assets/local_speech/licenses/Vosk-Apache-2.0.txt        ← 只有这一份
```

而 catalog 引用的还有 `Qwen-Apache-2.0.txt` 与 `SenseVoice-Model-License-1.1.txt`，**从未加入**。
Vosk 能用、其它全废，与现象完全吻合。

**改动**：

- 补齐两份许可文本（Apache-2.0 全文；SenseVoice 取 FunAudioLLM/SenseVoice 仓库 LICENSE）；
- 降级兜底：`writeMetadata` 缺 asset 时写空文件，`verify` 缺 asset 时跳过比对。
  许可文案不构成安全边界（模型完整性由 `sha256` 保证），不该让漏一个 txt 使整类模型不可用。

---

## 11. 装好后识别报「本机离线原生库不兼容」（缺 libc++_shared.so）

**现象**：模型安装成功，点识别立刻失败。

**证据**（logcat）：

```
Rejecting re-init on previously-failed class GgufSpeechNative:
java.lang.UnsatisfiedLinkError: dlopen failed: library "libc++_shared.so" not found:
needed by .../lib/arm64/libeta_transcribe_jni.so
EtaLocalSpeech: provider=local session-error=CONFIGURATION
```

**原因**：当初从参考 APK 只挑了 6 个 so，漏了 `libc++_shared.so`。

**核对方法**（脚本解析 ELF `DT_NEEDED`，比 grep 字符串可靠）：

| so | NEEDED |
| --- | --- |
| `libeta_transcribe_jni.so` | `libdl libc libm **libc++_shared**` |
| `libsherpa-onnx-jni.so` | `libandroid liblog libonnxruntime libm libdl libc` |
| `libsherpa-ncnn-jni.so` | `libncnn libandroid libjnigraphics …` |
| `libvosk.so` / `libncnn.so` / `libonnxruntime.so` | 仅系统库 |

**改动**：`jniLibs/arm64-v8a/libc++_shared.so`（1,423,696 字节，取自参考 APK）。

---

## 12. 卡顿：debug 变体是主因

**现象**：界面操作、输入法滑入滑出都卡。

**证据**（`dumpsys gfxinfo io.github.mangi.eta`）：

| 变体 | 50th | 99th | 现代 janky | legacy janky |
| --- | --- | --- | --- | --- |
| debug | 12–14 ms | 34–44 ms | 0.2–0.5% | **20–38%** |
| release | 同量级但长尾明显收敛 | | < 0.6% | 显著下降 |

legacy 口径在 120 Hz 屏上阈值是 8.3 ms，debug 构建必然超。另有主线程堆栈显示大量
`art::interpreter::ExecuteSwitchImplCpp`——debug 包主要走解释执行。另外 `proguard-rules.pro`
里 `-assumenosideeffects ModuleLogger.debug` 只在 release 生效，debug 保留全部日志写入。

**改动**：

1. 新增 `.github/workflows/fork-release.yml`：CI 现场生成签名，出 `assembleRelease`（R8 + 资源裁剪，
   APK 从 57.5 MB 降到 26.5 MB）；
2. 补 R8 keep（JNI 按名访问，混淆即崩）：

```proguard
-keep class io.github.mangi.eta.agent.voice.local.GgufSpeechNative { *; }
-keepclasseswithmembernames class * { native <methods>; }
-keep class com.k2fsa.sherpa.onnx.** { *; }
-keep class org.vosk.** { *; }
-keep class com.sun.jna.** { *; }
```

3. 非 Vosk 模型原先每次 `inspect` / 冷加载都全量 SHA（单文件最大 590 MB），改为目录指纹缓存
   （inode / ctime / mtime / nanos），文件被替换即失效。

---

## 交付物

| 项 | 位置 |
| --- | --- |
| Release APK | Actions artifact `Eta-v3.4.4+<sha>.apk`（`Eta Fork Release`） |
| 模型镜像 | Release `local-models-v1`（7 个文件，sha256 与 catalog 一致） |
| 代码 | `mariusarva/Eta` main |
| 设计说明 | `docs/FORK_FEATURES.md` 的「离线语音」一节 |

## 仍未验证

- Qwen3-ASR 0.6B（562 MB / 615 MB）只在目录里可选，未在设备上跑过推理耗时；
- 1.4 GB 的 `vosk_cn_022` 未实测下载与内存占用；
- `libsherpa-ncnn-jni.so` 对应的 sherpa-ncnn 引擎尚无 UI 入口。

---

## 13. 语音播报（TTS）三处问题

**现象**：设置页「试听音色」红字提示「系统朗读须通过本地播放控制器调用」；自动播报没有声音。

### 13.1 SYSTEM 引擎根本没接线

`SystemSpeechPlayback` 写好了但**没有任何调用点**。`SpeechPlaybackController.speak()` 一律走
「云端合成 PCM → AudioTrack 播放」的管线，而 `synthesizeSpeech()` 对 `TtsProvider.SYSTEM`
直接抛错。所以选「系统朗读（离线）」永远播不出声，试听也只是报那句红字。

**改动**：`SpeechPlaybackController` 在拿到音频租约后先判断 `config.tts == SYSTEM`，
直接交给 `SystemSpeechPlayback.play(readable, config.systemTts, config.language)`；
`stop()` 调用 `SystemSpeechPlayback.stopCurrent()`（只停当前播报，保留引擎实例）。

### 13.2 离线音色判定把「支持联网」当成「必须联网」

```kotlin
// 修改前
!voice.isNetworkConnectionRequired &&
    TextToSpeech.Engine.KEY_FEATURE_NETWORK_SYNTHESIS !in voice.features.orEmpty() && ...
```

`KEY_FEATURE_NETWORK_SYNTHESIS` 的常量值是 `"networkTimeoutMs"`，语义是网络合成的**超时参数**：
引擎支持联网合成就会带上它，不代表必须联网。拿它当判据会把 MIUI 小爱（`com.xiaomi.mibrain.speech`，
本机唯一引擎）的中文音色全部判成「不可离线」→ 「仅离线音色」默认开启 → 播报直接失败。

**改动**：只保留权威判据 `!voice.isNetworkConnectionRequired` 与 `KEY_FEATURE_NOT_INSTALLED`。

### 13.3 每段播报都重建引擎 / 用 QUEUE_FLUSH 截尾

- `play()` 每段结束 `tts.shutdown()`，下一次播报重新初始化（几百毫秒）。自动播报连续分段时
  声音会明显落后于文本。改为按引擎名缓存实例（`engineFor`），只在引擎名变化或首次调用时重建，
  `close()` 时统一 shutdown。
- 分段循环里每段都用 `QUEUE_FLUSH`，会把上一段尾部截掉。改为首段 `QUEUE_FLUSH`、后续 `QUEUE_ADD`。
