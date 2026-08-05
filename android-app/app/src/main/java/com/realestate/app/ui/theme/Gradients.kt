package com.realestate.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Calm brand-indigo three-point gradient for hero surfaces (wallet card, auth header, story card). */
@Composable
fun heroGradient(): Brush = Brush.linearGradient(
    colorStops = arrayOf(
        0f to Color(0xFF4A6FA8),
        0.55f to Color(0xFF1D3A6E),
        1f to Color(0xFF0F2545)
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

/** Warm bronze-to-deep-bronze gradient for the primary call-to-action — a richer alternative to
 *  the flat accent color for the highest-emphasis buttons/banners in the app. */
@Composable
fun ctaGradient(): Brush {
    val accent = MaterialTheme.extendedColors.accent
    return Brush.linearGradient(
        colorStops = arrayOf(0f to accent, 1f to Color(0xFF6B4520))
    )
}

/** Two-tone success gradient — for a completed-deal banner or a "sold" celebratory surface. */
@Composable
fun successGradient(): Brush {
    val success = MaterialTheme.extendedColors.success
    return Brush.linearGradient(colorStops = arrayOf(0f to success, 1f to Color(0xFF0F5C2C)))
}

/** Two-tone warning gradient — for an attention-needed banner (e.g. negotiation deadline). */
@Composable
fun warningGradient(): Brush {
    val warning = MaterialTheme.extendedColors.warning
    return Brush.linearGradient(colorStops = arrayOf(0f to warning, 1f to Color(0xFFC97A00)))
}

/** Subtle deep-navy gradient for a "premium/featured" highlighted card — distinct from the
 *  everyday flat surface card, without introducing a new unrelated color. */
@Composable
fun premiumCardGradient(): Brush = Brush.linearGradient(
    colorStops = arrayOf(
        0f to Color(0xFF1D3A6E),
        1f to Color(0xFF0F2545)
    )
)
