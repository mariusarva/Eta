package io.github.mangi.eta.ui.components.custom

import android.content.Context
import android.os.Looper
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@LooperMode(LooperMode.Mode.PAUSED)
class CustomMessageSaveControllerTest {
    @Test fun mainScopeSaves3501ChineseCharactersWithoutTrimmingOrTruncation() {
        val context = RuntimeEnvironment.getApplication()
        val scope = MainScope()
        val writes = java.util.concurrent.CopyOnWriteArrayList<String>()
        val persistence = object : CustomMessagePersistence {
            override suspend fun saveMemory(context: Context, text: String): String {
                writes += text
                return "已保存"
            }
            override suspend fun saveSkill(context: Context, text: String): String = error("wrong mode")
        }
        try {
            val controller = MessageSaveController(context, scope, persistence)
            val original = "汉".repeat(3501)
            controller.begin(original)
            assertEquals(original, controller.review?.originalText)
            assertEquals(original, controller.review?.text)
            assertTrue(writes.isEmpty())
            controller.confirm()
            val deadline = System.nanoTime() + 5_000_000_000L
            while (controller.saving && System.nanoTime() < deadline) {
                shadowOf(Looper.getMainLooper()).idle()
                Thread.sleep(5)
            }
            assertFalse("save did not finish", controller.saving)
            assertEquals(listOf(original), writes.toList())
            assertNull(controller.review)
            assertEquals(false, controller.result?.error)
        } finally {
            scope.cancel()
        }
    }
}
