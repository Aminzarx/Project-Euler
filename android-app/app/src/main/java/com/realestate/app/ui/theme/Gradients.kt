package com.realestate.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Diagonal three-point gradient for hero areas (headers, empty states, story card). */
@Composable
fun heroGradient(): Brush {
    val scheme = MaterialTheme.colorScheme
    return Brush.linearGradient(
        colorStops = arrayOf(
            0f to scheme.primary,
            0.55f to scheme.tertiary,
            1f to scheme.secondary
        )
    )
}

/** Softer three-point gradient for special cards (wallet, highlights). */
@Composable
fun cardGradient(): Brush {
    val scheme = MaterialTheme.colorScheme
    return Brush.linearGradient(
        colorStops = arrayOf(
            0f to scheme.primaryContainer,
            0.5f to scheme.tertiaryContainer,
            1f to scheme.secondaryContainer
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
