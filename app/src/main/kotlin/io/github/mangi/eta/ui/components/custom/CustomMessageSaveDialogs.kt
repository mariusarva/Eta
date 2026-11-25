package io.github.mangi.eta.ui.components.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/** v342 review -> background persistence -> rounded result; button row never scrolls. */
@Composable
internal fun MessageSaveDialogs(controller: MessageSaveController) {
    val result = controller.result
    val review = controller.review
    if (result != null) {
        MessageSaveDialogFrame(
            title = if (result.error) "保存未确认" else "保存结果",
            onDismiss = controller::dismissResult,
            buttons = { SaveDialogButton("确定", controller::dismissResult) },
        ) { bodyHeight ->
            Text(
                result.message,
                modifier = Modifier.heightIn(max = bodyHeight).verticalScroll(rememberScrollState()),
                fontSize = 16.sp,
                lineHeight = 24.sp,
            )
        }
    } else if (review != null) {
        MessageSaveDialogFrame(
            title = if (review.skill) "保存技能" else "保存记忆",
            onDismiss = controller::dismiss,
            dismissible = !controller.saving,
            buttons = {
                SaveDialogButton("取消", controller::dismiss, enabled = !controller.saving, secondary = true)
                SaveDialogButton(
                    if (controller.saving) "保存中…" else "确认保存",
                    controller::confirm,
                    enabled = !controller.saving,
                )
            },
        ) { bodyHeight ->
            Column(Modifier.heightIn(max = bodyHeight).verticalScroll(rememberScrollState())) {
                Text("请审核或编辑正文。仅保存到本地，不需要网络。", fontSize = 16.sp, lineHeight = 24.sp)
                if (!review.skill) {
                    Text(
                        "记忆文件总上限为 1 MiB（UTF-8，含已有内容）；超限会提示，不会截断正文。",
                        modifier = Modifier.padding(top = 8.dp),
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                    )
                }
                BasicTextField(
                    value = review.text,
                    onValueChange = controller::edit,
                    enabled = !controller.saving,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                        .background(if (isSystemInDarkTheme()) Color(0xFF303034) else Color.White, RoundedCornerShape(12.dp))
                        .padding(12.dp).heightIn(min = 120.dp),
                    textStyle = TextStyle(color = saveForeground(), fontSize = 16.sp, lineHeight = 24.sp),
                )
                review.error?.let {
                    Text(it, modifier = Modifier.padding(top = 8.dp), color = Color(0xFFD34747),
                        fontSize = 16.sp, lineHeight = 24.sp)
                }
            }
        }
    }
}

@Composable
private fun MessageSaveDialogFrame(
    title: String,
    onDismiss: () -> Unit,
    dismissible: Boolean = true,
    buttons: @Composable () -> Unit,
    body: @Composable (androidx.compose.ui.unit.Dp) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = dismissible,
            dismissOnClickOutside = dismissible,
        ),
    ) {
        val view = LocalView.current
        DisposableEffect(view) {
            (view.parent as? DialogWindowProvider)?.window?.setDimAmount(0.1f)
            onDispose { }
        }
        BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val formWidth = minOf(maxWidth * 0.9f, 420.dp)
            val bodyHeight = maxHeight * 0.45f
            Box(
                modifier = Modifier.width(formWidth).shadow(8.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isSystemInDarkTheme()) Color(0xFF242428) else Color(0xFFF4F4F7)),
            ) {
                Column {
                    Text(title, modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp),
                        fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold)
                    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) { body(bodyHeight) }
                    // Outside the scrolling body: full 48dp capsules and 16dp right gutter.
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.End,
                    ) { buttons() }
                }
            }
        }
    }
}

@Composable
private fun SaveDialogButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    secondary: Boolean = false,
) {
    val background = if (isSystemInDarkTheme()) {
        if (secondary) Color(0xFF303034) else Color(0xFF3E3E42)
    } else {
        if (secondary) Color(0xFFE5E5E8) else Color(0xFFEAEAEC)
    }
    Box(
        modifier = Modifier.padding(start = 8.dp).alpha(if (enabled) 1f else 0.5f)
            .clip(RoundedCornerShape(24.dp)).background(background)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .sizeIn(minWidth = 80.dp, minHeight = 48.dp).padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) { Text(label, fontSize = 16.sp, maxLines = 1) }
}

@Composable
private fun saveForeground(): Color =
    if (isSystemInDarkTheme()) Color(0xFFF4F4F7) else Color(0xFF202020)

@Composable
private fun Text(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp,
    lineHeight: TextUnit = 24.sp,
    color: Color = saveForeground(),
    fontWeight: FontWeight = FontWeight.Normal,
    maxLines: Int = Int.MAX_VALUE,
) {
    BasicText(text, modifier = modifier, style = TextStyle(color = color, fontSize = fontSize,
        lineHeight = lineHeight, fontWeight = fontWeight), maxLines = maxLines)
}
