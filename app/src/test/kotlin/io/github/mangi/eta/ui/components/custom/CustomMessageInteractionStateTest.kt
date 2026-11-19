package io.github.mangi.eta.ui.components.custom

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomMessageInteractionStateTest {
    @Test fun ordinaryBodyStartsNonSelecting() {
        assertEquals(CustomMessageInteractionState(false, false), CustomMessageInteractionState())
    }
    @Test fun longPressOnlyOpensUnifiedMenu() {
        val state = CustomMessageInteractionState().openMenu(enabled = true)
        assertTrue(state.menuOpen)
        assertFalse(state.selecting)
        assertFalse(state.dismissMenu().selecting)
    }
    @Test fun disabledLongPressDoesNotOpenMenuOrSelection() {
        assertEquals(CustomMessageInteractionState(), CustomMessageInteractionState().openMenu(false))
    }
    @Test fun explicitActionEntersInlineSelectionAndClosesMenu() {
        val state = CustomMessageInteractionState().openMenu(true).beginSelection(true, true)
        assertFalse(state.menuOpen)
        assertTrue(state.selecting)
    }
    @Test fun unavailableOrEmptyBodyCannotStartSelection() {
        for (enabled in listOf(false, true)) for (hasText in listOf(false, true)) {
            assertEquals(enabled && hasText,
                CustomMessageInteractionState().beginSelection(enabled, hasText).selecting)
        }
    }
    @Test fun openingAndDismissingMenuDoesNotClearExplicitHandles() {
        val selected = CustomMessageInteractionState().beginSelection(true, true)
        assertEquals(selected, selected.openMenu(true).dismissMenu())
    }
    @Test fun freshSourceOrFinishedStateHasNoMenuOrSelection() {
        val selected = CustomMessageInteractionState().beginSelection(true, true)
        assertTrue(selected.selecting)
        // Production recreates this state on composer/message/text/enabled key changes and Done.
        val reset = CustomMessageInteractionState()
        assertFalse(reset.selecting)
        assertFalse(reset.menuOpen)
    }
}
