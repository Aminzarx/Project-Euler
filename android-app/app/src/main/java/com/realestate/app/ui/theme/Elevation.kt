package com.realestate.app.ui.theme

import androidx.compose.ui.unit.dp

/** Named soft-shadow depths — keeps every card/control/floating surface picking from one
 *  consistent scale instead of a one-off dp value per component. Higher = visually closer to
 *  the user, per Material3's elevation-implies-hierarchy convention. */
object Elevation {
    /** List rows (AppListRow) — the subtlest lift, barely off the page. */
    val row = 4.dp

    /** Interactive controls: buttons, circular icon buttons. */
    val control = 5.dp

    /** Standalone content cards (AppCard). */
    val card = 8.dp

    /** Surfaces that float above scrolling content (FloatingBottomNav). */
    val floating = 10.dp

    /** Menus and tooltips — brief, transient overlays anchored to a trigger. */
    val menu = 8.dp

    /** Bottom sheets — a full-width surface pulled up over the whole screen. */
    val sheet = 12.dp

    /** Dialogs — the most foreground, modal-blocking surface in the app. */
    val dialog = 16.dp
}
