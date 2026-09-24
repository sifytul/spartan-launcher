package com.spartan.launcer.data.model

/**
 * How long a "Use anyway" grace lasts on the block screen.
 */
enum class GraceMode {
    /** Grace is cleared the next time the phone is unlocked. */
    UNLOCK,

    /** Grace lasts a fixed number of minutes ([LauncherSettings.shortVideoGraceMinutes]). */
    MINUTES
}