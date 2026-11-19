package io.github.mangi.eta.ui.components.custom

import android.content.Context
import androidx.compose.runtime.compositionLocalOf

/** Message-save boundary; ancestors may override it or provide null to disable saves. */
internal interface CustomMessagePersistence {
    suspend fun saveMemory(context: Context, text: String): String
    suspend fun saveSkill(context: Context, text: String): String
}

// The factory is lazy; CustomMessageActions has no dependency on this local at initialization.
internal val LocalCustomMessagePersistence = compositionLocalOf<CustomMessagePersistence?> { CustomMessageActions }
