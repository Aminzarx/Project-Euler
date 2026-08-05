package com.realestate.app.ui.theme

/** Central motion-duration scale (ms) — every animation in the app moves at one of these speeds
 *  instead of a scattered set of one-off magic numbers. */
object MotionDuration {
    /** Quick, low-travel feedback: press-scale, icon color/size swaps. */
    const val FAST = 150

    /** Default transition speed: screen-to-screen fades, cross-fades. */
    const val MEDIUM = 250

    /** Larger-travel motion: hierarchical push/pop slides. */
    const val SLOW = 350
}
