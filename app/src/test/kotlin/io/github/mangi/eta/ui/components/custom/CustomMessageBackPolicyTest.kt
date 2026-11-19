package io.github.mangi.eta.ui.components.custom

import org.junit.Assert.assertEquals
import org.junit.Test

class CustomMessageBackPolicyTest {
    @Test fun noOwnerNeverInvokesBackHandlerEvenWhenDisabled() = checkOwner(false, false)
    @Test fun activityOwnerAllowsBackHandler() = checkOwner(true, false)
    @Test fun navigationOwnerAloneAllowsBackHandler() = checkOwner(false, true)
    @Test fun bothOwnersAllowBackHandler() = checkOwner(true, true)

    private fun checkOwner(activityOwner: Boolean, navigationOwner: Boolean) {
        // All owner/selection/menu combinations, including the first message's disabled handler.
        for (selecting in listOf(false, true)) for (expanded in listOf(false, true)) {
            var registrations = 0
            var enabled: Boolean? = null
            if (hasMessageBackHandlerOwner(activityOwner, navigationOwner)) {
                registrations++
                enabled = selecting && !expanded
            }
            val hasOwner = activityOwner || navigationOwner
            val label = "activity=$activityOwner navigation=$navigationOwner selecting=$selecting expanded=$expanded"
            assertEquals(label, if (hasOwner) 1 else 0, registrations)
            assertEquals(label, if (hasOwner) selecting && !expanded else null, enabled)
        }
    }
}
