package io.github.mangi.eta.ui.components.custom

/** Only a menu action can request selection; pointer long-press only opens the menu. */
internal data class CustomMessageInteractionState(
    val menuOpen: Boolean = false,
    val selecting: Boolean = false,
) {
    fun openMenu(enabled: Boolean) = if (enabled) copy(menuOpen = true) else this
    fun dismissMenu() = copy(menuOpen = false)
    fun beginSelection(enabled: Boolean, hasText: Boolean) =
        if (enabled && hasText) copy(menuOpen = false, selecting = true) else this
}
