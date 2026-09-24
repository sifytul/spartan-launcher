package com.spartan.launcer.service

/**
 * Detects phone unlocks from the accessibility window stream, without any
 * broadcast or polling. When the active window is the keyguard (SystemUI) the
 * device is considered locked; when a normal app or the launcher takes over
 * afterwards, the device was unlocked. Note: on some devices the open
 * notification shade also reports the SystemUI package, so closing the shade
 * can occasionally count as an unlock — an accepted tradeoff.
 */
class UnlockDetector {

    enum class Signal { NONE, LOCKED, UNLOCKED }

    private var keyguardVisible = false

    fun onWindowPackage(pkg: String?): Signal {
        if (pkg.isNullOrBlank()) return Signal.NONE
        if (pkg in KEYGUARD_PACKAGES) {
            keyguardVisible = true
            return Signal.LOCKED
        }
        if (keyguardVisible) {
            keyguardVisible = false
            return Signal.UNLOCKED
        }
        return Signal.NONE
    }

    private companion object {
        val KEYGUARD_PACKAGES = setOf("com.android.systemui", "com.android.keyguard")
    }
}