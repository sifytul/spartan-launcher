package com.spartan.launcer.service

import org.junit.Assert.assertEquals
import org.junit.Test

class UnlockDetectorTest {

    @Test
    fun keyguardThenLauncherReportsUnlock() {
        val detector = UnlockDetector()
        assertEquals(UnlockDetector.Signal.LOCKED, detector.onWindowPackage("com.android.systemui"))
        assertEquals(UnlockDetector.Signal.UNLOCKED, detector.onWindowPackage("com.spartan.launcer"))
    }

    @Test
    fun keyguardOemPackageThenAppReportsUnlock() {
        val detector = UnlockDetector()
        assertEquals(UnlockDetector.Signal.LOCKED, detector.onWindowPackage("com.android.keyguard"))
        assertEquals(UnlockDetector.Signal.UNLOCKED, detector.onWindowPackage("com.google.android.gm"))
    }

    @Test
    fun keyguardReappearanceReArms() {
        val detector = UnlockDetector()
        detector.onWindowPackage("com.android.systemui")
        detector.onWindowPackage("com.spartan.launcer")
        assertEquals(UnlockDetector.Signal.LOCKED, detector.onWindowPackage("com.android.systemui"))
        assertEquals(UnlockDetector.Signal.UNLOCKED, detector.onWindowPackage("com.spartan.launcer"))
    }

    @Test
    fun nullAndBlankPackagesAreIgnored() {
        val detector = UnlockDetector()
        assertEquals(UnlockDetector.Signal.NONE, detector.onWindowPackage(null))
        assertEquals(UnlockDetector.Signal.NONE, detector.onWindowPackage(""))
        assertEquals(UnlockDetector.Signal.NONE, detector.onWindowPackage("   "))
    }

    @Test
    fun normalAppSwitchingProducesNoSignalUnlessUnlocking() {
        val detector = UnlockDetector()
        assertEquals(UnlockDetector.Signal.NONE, detector.onWindowPackage("com.google.android.gm"))
        detector.onWindowPackage("com.android.systemui")
        assertEquals(UnlockDetector.Signal.UNLOCKED, detector.onWindowPackage("com.google.android.gm"))
        assertEquals(UnlockDetector.Signal.NONE, detector.onWindowPackage("com.google.android.gm"))
    }
}