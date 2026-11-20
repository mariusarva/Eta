package io.github.mangi.eta.data.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateCheckerTest {

    @Test
    fun newerPatchVersionIsNewer() {
        assertTrue(AppUpdateChecker.isNewer("3.0.7", "3.0.6"))
    }

    @Test
    fun sameVersionIsNotNewer() {
        assertFalse(AppUpdateChecker.isNewer("3.0.6", "3.0.6"))
    }

    @Test
    fun olderVersionIsNotNewer() {
        assertFalse(AppUpdateChecker.isNewer("3.0.5", "3.0.6"))
    }

    @Test
    fun missingSegmentsCountAsZero() {
        assertTrue(AppUpdateChecker.isNewer("3.1", "3.0.9"))
        assertFalse(AppUpdateChecker.isNewer("3.0", "3.0.1"))
    }

    @Test
    fun vPrefixIsIgnored() {
        assertTrue(AppUpdateChecker.isNewer("v3.1.0", "3.0.6"))
        assertFalse(AppUpdateChecker.isNewer("v3.0.6", "3.0.6"))
    }

    @Test
    fun debugSuffixIsIgnored() {
        assertFalse(AppUpdateChecker.isNewer("3.0.6", "3.0.6-debug"))
        assertTrue(AppUpdateChecker.isNewer("3.0.10", "3.0.9-debug"))
    }

    @Test
    fun buildMetadataIsIgnored() {
        assertFalse(AppUpdateChecker.isNewer("3.0.6", "3.0.6+a1b2c3d"))
        assertFalse(AppUpdateChecker.isNewer("3.0.6", "3.0.6-debug+a1b2c3d"))
        assertTrue(AppUpdateChecker.isNewer("3.0.7", "3.0.6+a1b2c3d"))
    }

    @Test
    fun forkSequenceParticipatesWhenUpstreamVersionIsEqual() {
        // 上游相同，fork 序号更大的才算新版本。
        assertTrue(AppUpdateChecker.isNewer("3.4.4-fork.17", "3.4.4-fork.16"))
        assertFalse(AppUpdateChecker.isNewer("3.4.4-fork.16", "3.4.4-fork.16"))
        assertFalse(AppUpdateChecker.isNewer("3.4.4-fork.15", "3.4.4-fork.16"))
    }

    @Test
    fun forkTagIsNewerThanPlainUpstreamVersion() {
        assertTrue(AppUpdateChecker.isNewer("3.4.4-fork.1", "3.4.4"))
        assertFalse(AppUpdateChecker.isNewer("3.4.4", "3.4.4-fork.3"))
    }

    @Test
    fun buildMetadataIsIgnored() {
        assertFalse(AppUpdateChecker.isNewer("3.4.4-fork.17", "3.4.4-fork.17+abc1234"))
    }
}
