package com.realestate.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.realestate.app.R

val VazirmatnFontFamily = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

private fun style(size: Int, lineHeight: Int, weight: FontWeight, spacing: Double = 0.0) = TextStyle(
    fontFamily = VazirmatnFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = spacing.sp
)

// Premium minimal hierarchy: Large Title 28-32/Bold, Page Title 24/SemiBold,
// Section Title 18, Body 15-16, Caption 13 — generous line spacing throughout.
val Typography = Typography(
    displayLarge = style(34, 44, FontWeight.Bold),
    displayMedium = style(32, 42, FontWeight.Bold),
    displaySmall = style(28, 38, FontWeight.Bold),
    headlineLarge = style(24, 32, FontWeight.SemiBold),
    headlineMedium = style(22, 30, FontWeight.SemiBold),
    headlineSmall = style(20, 28, FontWeight.SemiBold),
    titleLarge = style(18, 26, FontWeight.SemiBold),
    titleMedium = style(16, 24, FontWeight.Medium),
    titleSmall = style(14, 20, FontWeight.Medium),
    bodyLarge = style(16, 26, FontWeight.Normal),
    bodyMedium = style(15, 24, FontWeight.Normal),
    bodySmall = style(13, 20, FontWeight.Normal),
    labelLarge = style(14, 20, FontWeight.Medium),
    labelMedium = style(13, 18, FontWeight.Medium),
    labelSmall = style(12, 16, FontWeight.Medium)
)
