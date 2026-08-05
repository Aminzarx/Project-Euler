package com.realestate.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Calm brand-blue three-point gradient for hero surfaces (wallet card, auth header, story card). */
@Composable
fun heroGradient(): Brush = Brush.linearGradient(
    colorStops = arrayOf(
        0f to Color(0xFF6E8CBD),
        0.55f to Color(0xFF4C6B9A),
        1f to Color(0xFF293D5C)
    )
)

/** Soft, barely-there gradient for secondary surface cards. */
@Composable
fun cardGradient(): Brush {
    val scheme = MaterialTheme.colorScheme
    return Brush.linearGradient(
        colorStops = arrayOf(
            0f to scheme.surface,
            0.5f to scheme.surfaceVariant,
            1f to scheme.surface
        )
    )
}

/** Bottom-anchored scrim so light text stays legible over a photo. */
fun imageScrimGradient(): Brush = Brush.verticalGradient(
    colorStops = arrayOf(
        0f to Color.Transparent,
        0.5f to Color.Transparent,
        1f to Color.Black.copy(alpha = 0.65f)
    )
)
