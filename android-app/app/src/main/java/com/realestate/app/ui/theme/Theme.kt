package com.realestate.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = BrandLight,
    onPrimary = OnBrandLight,
    primaryContainer = DividerLight,
    onPrimaryContainer = BrandLight,
    secondary = BrandLight,
    onSecondary = OnBrandLight,
    secondaryContainer = DividerLight,
    onSecondaryContainer = BrandLight,
    tertiary = BrandLight,
    onTertiary = OnBrandLight,
    tertiaryContainer = DividerLight,
    onTertiaryContainer = BrandLight,
    background = BackgroundLight,
    onBackground = InkLight,
    surface = SurfaceLight,
    onSurface = InkLight,
    surfaceVariant = DividerLight,
    onSurfaceVariant = SecondaryTextLight,
    outline = OutlineLight,
    error = DangerLight,
    onError = OnDangerLight
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandDark,
    onPrimary = OnBrandDark,
    primaryContainer = DividerDark,
    onPrimaryContainer = BrandDark,
    secondary = BrandDark,
    onSecondary = OnBrandDark,
    secondaryContainer = DividerDark,
    onSecondaryContainer = BrandDark,
    tertiary = BrandDark,
    onTertiary = OnBrandDark,
    tertiaryContainer = DividerDark,
    onTertiaryContainer = BrandDark,
    background = BackgroundDark,
    onBackground = InkDark,
    surface = SurfaceDark,
    onSurface = InkDark,
    surfaceVariant = DividerDark,
    onSurfaceVariant = SecondaryTextDark,
    outline = OutlineDark,
    error = DangerDark,
    onError = OnDangerDark
)

/** Soft, borderless rounded-corner scale used across every card, sheet and image in the app. */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun RealEstateAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors,
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}
