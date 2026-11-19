package io.github.mangi.eta.ui.components.custom

/** Owner availability gates composition itself, not just the callback's enabled state. */
internal fun hasMessageBackHandlerOwner(
    hasOnBackPressedDispatcherOwner: Boolean,
    hasNavigationEventDispatcherOwner: Boolean,
): Boolean = hasOnBackPressedDispatcherOwner || hasNavigationEventDispatcherOwner
