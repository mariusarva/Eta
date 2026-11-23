#!/usr/bin/env python3
"""检查页面是否同时接入了明暗模式与 Monet 取色。

背景：会话分类、GitHub 账号两个页面曾只写 COUI 版式，在 MIUIX 样式下 COUITheme
回落到浅色默认值，深色模式整页变浅；另有一段时间 COUI 配色固定用
lightColorScheme()/darkColorScheme()，完全不响应 Monet 取色。这两类问题靠肉眼
翻页很难覆盖全，因此固化成检查。

检查项：
  E1  在 AgentAppTheme 之外直接构造 COUI 基线配色（lightColorScheme/darkColorScheme），
      会绕过 Monet 种子色。这是错误。
  E2  *Screen.kt 里使用固定 hex 且同文件没有任何明暗分支。这是错误
      （确实不随主题变的品牌/语义色请加入 ALLOWED_FIXED_COLOR_FILES 并注明原因）。
  E3  弹窗/浮层用固定 hex 当底色。这是错误：写死的白/灰不跟随 Monet，
      应改用 opaqueThemedSurface() / opaquePageSurface()（见 OpaqueSurfaces.kt）。
  E4  页面用了 Modifier / dp / Spacer 等基础符号但没有对应 import。这是错误：
      CI 编译会失败（曾因 GitHub 账号页补版式时漏 Modifier/dp 而挂过一次）。
  W1  页面用了 COUI 组件，但既没有 MiuixScaffoldPage 也没有任何 uiStyle 分支。
      这类页面在 MIUIX 样式下仍渲染 COUI 组件，拿不到 Miuix 的 Monet 配色。
  W2  页面既无主题色引用也无 Scaffold，可能把配色完全交给子组件（多为误报，
      新增页面时人工确认一次）。

用法：python3 scripts/check-theme-coverage.py [--strict]
  --strict 时把警告也视为失败，用于发版前把关。
退出码 0 通过，1 有问题。
"""
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
UI = ROOT / "app/src/main/kotlin/io/github/mangi/eta/ui"

# 设计上不随主题变化的语义色/品牌色，出现在这些文件里不算违规。
ALLOWED_FIXED_COLOR_FILES = {
    "StatusColors.kt": "语义状态色（成功/警告），深浅主题共用",
    "CharacterAvatar.kt": "字标渐变，白字需要固定饱和底",
    "EtaAssistantEdgeGlow.kt": "语音极光配色",
    "MarkdownInline.kt": "链接色基线，运行时会由 MarkdownStyle 覆盖",
    "SettingsScreen.kt": "设置项图标品牌色",
    "GitHubAccountScreen.kt": "图标品牌蓝 #4285F4",
    "ConversationCategoriesScreen.kt": "图标品牌蓝 #4285F4；弹窗实色底已有 luminance 分支",
    "CharacterPortraitCard.kt": "收藏星标金",
    "AgentAppTheme.kt": "种子色表",
    "AnsiSgr.kt": "终端 ANSI 调色板",
    "TerminalScreenBuffer.kt": "终端默认前景/背景",
}

# 只做状态转发、配色完全由子组件负责的页面，不需要自己引用主题色。
PURE_CONTAINER_SCREENS = {
    "AgentHomeScreen.kt": "仅把 state 传给 AgentChatBody，配色由后者负责",
}

errors: list[str] = []
warnings: list[str] = []


def strip_comments(src: str) -> str:
    """去掉 // 与 /* */ 注释和字符串字面量，避免注释里的词被当成代码使用。"""
    out = []
    i, n = 0, len(src)
    while i < n:
        c = src[i]
        if c == "/" and i + 1 < n and src[i + 1] == "/":
            j = src.find("\n", i)
            i = n if j < 0 else j
            continue
        if c == "/" and i + 1 < n and src[i + 1] == "*":
            j = src.find("*/", i + 2)
            i = n if j < 0 else j + 2
            continue
        if c == '"':
            i += 1
            while i < n:
                if src[i] == "\\":
                    i += 2
                    continue
                if src[i] == '"':
                    i += 1
                    break
                i += 1
            continue
        out.append(c)
        i += 1
    return "".join(out)


def check_screen(path: Path) -> None:
    src = path.read_text(encoding="utf-8")
    name = path.name

    themed = any(
        token in src
        for token in ("MiuixTheme.colorScheme", "CouiTheme.colorScheme",
                      "CouiPageDefaults.themeColors", "LocalUiStyle", "AppearanceUiStyle")
    )
    has_scaffold = bool(re.search(r"\b(Coui|Miuix)(Lazy)?ScaffoldPage\s*\(", src))
    uses_coui = bool(re.search(r"\bCoui[A-Z]\w*\s*\(", src))
    uses_miuix_page = bool(re.search(r"\bMiuixScaffoldPage\s*\(", src))

    # E2：固定 hex 需要同文件明暗分支
    hexes = re.findall(r"Color\(0x[0-9A-Fa-f]{6,8}\)", src)
    if hexes and name not in ALLOWED_FIXED_COLOR_FILES:
        has_dark_branch = any(
            token in src
            for token in ("luminance()", "isDark", "CouiTheme.isDark", "COUITheme.isDark")
        )
        if not has_dark_branch:
            errors.append(
                f"E2 {name}: {len(hexes)} 处固定色且无明暗分支；"
                f"若确属品牌/语义色，加入 ALLOWED_FIXED_COLOR_FILES 并写明原因"
            )

    # W1：COUI 页面缺 MIUIX 版式。
    # 有 uiStyle 分支就说明两套都写了（可能共用 pageContent，也可能各写一套），不算缺失；
    # 只在「用了 COUI 组件、既没有 MiuixScaffoldPage 也没有任何样式分支」时报警。
    has_style_branch = "AppearanceUiStyle" in src or "LocalUiStyle" in src
    if uses_coui and not uses_miuix_page and not has_style_branch:
        warnings.append(
            f"W1 {name}: 使用 COUI 组件但没有 MIUIX 版式或 uiStyle 分支；"
            f"MIUIX 样式下仍走 COUI 视觉，拿不到 Miuix 主题色"
        )

    # W2：完全没有主题色引用
    if not themed and not has_scaffold and name not in PURE_CONTAINER_SCREENS:
        warnings.append(f"W2 {name}: 未引用主题色也无 Scaffold，确认配色是否都由子组件负责")


# 基础符号 → 必需 import 的映射；只在源码里出现该符号时才检查。
REQUIRED_IMPORTS = {
    "Modifier": "import androidx.compose.ui.Modifier",
    "dp": "import androidx.compose.ui.unit.dp",
    "sp": "import androidx.compose.ui.unit.sp",
    "Spacer": "import androidx.compose.foundation.layout.Spacer",
}


def check_missing_imports() -> None:
    """E4：用了基础符号却没有对应 import，编译必然失败。

    主题检查脚本跑得比编译快，把这类低级错误挡在 CI 之前。
    用词边界匹配，避免把 "dpi"/"Modifier.xyz 之外的词" 误判。
    """
    for path in sorted(UI.rglob("*.kt")):
        src = path.read_text(encoding="utf-8")
        # 只检查 Compose 文件：没有 @Composable 的文件不会用到这些符号
        if "@Composable" not in src:
            continue
        code = strip_comments(src)
        for symbol, imp in REQUIRED_IMPORTS.items():
            if imp in src:
                continue
            if symbol in ("dp", "sp"):
                # 要求前面是空白、括号或运算符，排除 `dp: Int` 这种参数名与 a.dp
                used = re.search(r"(?<![\w.])" + symbol + r"(?![\w:])", code)
            else:
                used = re.search(r"\b" + symbol + r"\b", code)
            if used:
                errors.append(f"E4 {path.name}: 用了 {symbol} 但缺少 {imp}")


def check_dialog_backgrounds() -> None:
    """E3：弹窗/浮层不得用固定 hex 当底色。

    弹窗底色必须不透明（否则透出下层），但写死 #FFFFFF / #2A2A2E 会绕过 Monet。
    统一走 opaqueThemedSurface()：把主题容器色合成到背景上，等价不透明且保留色相。
    """
    result = subprocess.run(
        ["grep", "-rn", "-E", r"\.background\(.*Color\(0x[0-9A-Fa-f]{6,8}\)",
         str(ROOT / "app/src/main/kotlin")],
        capture_output=True, text=True,
    )
    for line in result.stdout.splitlines():
        path, lineno = line.split(":", 2)[:2]
        name = path.split("/")[-1]
        if name in ALLOWED_FIXED_COLOR_FILES:
            continue
        # 只在弹窗/浮层相关的文件里报，普通页面由 E2 覆盖
        src = Path(path).read_text(encoding="utf-8")
        if not re.search(r"\b(Dialog|Popup|ModalBottomSheet)\s*\(", src):
            continue
        errors.append(
            f"E3 {name}:{lineno} 弹窗底色用了固定色，不跟随 Monet；"
            f"改用 opaqueThemedSurface() / opaquePageSurface()"
        )


def check_no_bypass() -> None:
    """E1：AgentAppTheme 之外不得直接构造 COUI 基线配色。"""
    result = subprocess.run(
        ["grep", "-rn", "-E", r"(lightColorScheme|darkColorScheme)\s*\(",
         str(ROOT / "app/src/main/kotlin")],
        capture_output=True, text=True,
    )
    for line in result.stdout.splitlines():
        if "AgentAppTheme.kt" in line:
            continue
        file_part = line.split(":", 1)[0]
        errors.append(
            f"E1 {file_part.split('/')[-1]}: 直接构造 COUI 基线配色，绕过 Monet 种子色"
        )


def main() -> int:
    strict = "--strict" in sys.argv
    screens = sorted(UI.rglob("*Screen.kt"))
    if not screens:
        print("未找到任何 *Screen.kt，路径可能变了", file=sys.stderr)
        return 1

    for screen in screens:
        check_screen(screen)
    check_no_bypass()
    check_dialog_backgrounds()
    check_missing_imports()

    print(f"检查 {len(screens)} 个页面")
    for item in warnings:
        print(f"  警告 {item}")
    for item in errors:
        print(f"  错误 {item}")

    if errors:
        print(f"\n{len(errors)} 处错误")
        return 1
    if strict and warnings:
        print(f"\nstrict 模式：{len(warnings)} 处警告视为失败")
        return 1
    print("通过" + (f"（{len(warnings)} 处警告）" if warnings else ""))
    return 0


if __name__ == "__main__":
    sys.exit(main())
