package com.realestate.app.ui.theme

import androidx.compose.ui.unit.dp

/** Named soft-shadow depths — keeps every card/control/floating surface picking from one
 *  consistent scale instead of a one-off dp value per component. */
object Elevation {
    /** List rows (AppListRow) — the subtlest lift, barely off the page. */
    val row = 4.dp

    /** Interactive controls: buttons, circular icon buttons. */
    val control = 5.dp

    /** Standalone content cards (AppCard). */
    val card = 8.dp

    /** Surfaces that float above scrolling content (FloatingBottomNav). */
    val floating = 10.dp
}
