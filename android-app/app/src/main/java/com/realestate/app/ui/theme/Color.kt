package com.realestate.app.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// Premium palette — light
// A deep indigo-navy primary (trust, stability), a muted slate-teal secondary
// and warm terracotta tertiary give Material3's three color roles a genuinely
// distinct identity instead of three names for one color. A deep bronze/amber
// accent carries every call-to-action — the one deliberately warm, "sold/deal"
// note against an otherwise cool, composed palette. Every text-on-color pairing
// below was checked against WCAG AA (>=4.5:1 for normal text, >=3:1 for
// large text/UI outlines) rather than picked by eye.
// ============================================================================

val BrandLight = Color(0xFF1D3A6E)
val OnBrandLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFDCE6F5)
val OnPrimaryContainerLight = Color(0xFF1D3A6E)

val SecondaryLight = Color(0xFF3E6C6B)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFDCEDEA)
val OnSecondaryContainerLight = Color(0xFF3E6C6B)

val TertiaryLight = Color(0xFF9C5A3C)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFF3DFD4)
val OnTertiaryContainerLight = Color(0xFF6B3F2C)

// The one warm, energetic note in the palette — every primary call-to-action button.
val AccentLight = Color(0xFF8C5A2B)
val OnAccentLight = Color(0xFFFFFFFF)

val InkLight = Color(0xFF14181F)
val BackgroundLight = Color(0xFFF5F6F8)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFE9ECF1)
val SecondaryTextLight = Color(0xFF5B6472)
val DividerLight = Color(0xFFE4E7ED)
// >=3:1 against Background — meets WCAG's non-text contrast guidance for UI component boundaries.
val OutlineLight = Color(0xFF7C8598)
val OutlineVariantLight = Color(0xFFD8DCE3)
val DisabledLight = Color(0xFFE2E4E9)
val OnDisabledLight = Color(0xFF9CA1AC)

val SuccessLight = Color(0xFF15803D)
val OnSuccessLight = Color(0xFFFFFFFF)
val DangerLight = Color(0xFFC23636)
val OnDangerLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFBE2E2)
val OnErrorContainerLight = Color(0xFF8F2323)
val WarningLight = Color(0xFFF59E0B)
val OnWarningLight = Color(0xFF2B1B00)
val InfoLight = Color(0xFF2569B0)
val OnInfoLight = Color(0xFFFFFFFF)
// A distinct hue from warning/success/danger/info — used for "reserved" so it never renders
// identically to another property status.
val ReservedLight = Color(0xFF7C4FE0)
val OnReservedLight = Color(0xFFFFFFFF)

val InverseSurfaceLight = Color(0xFF262E3A)
val InverseOnSurfaceLight = Color(0xFFF2F4F7)
val InversePrimaryLight = Color(0xFF8FAEE0)
val ScrimLight = Color(0xFF000000)

// ============================================================================
// Premium palette — dark
// Every hue keeps its light-mode identity (still "the blue one", "the teal
// one", "the terracotta one") but is lightened and desaturated just enough to
// sit comfortably on a near-black background, per Material3's dark-theme
// guidance — never the raw light-mode color dimmed, which reads muddy.
// ============================================================================

val BrandDark = Color(0xFF8FAEE0)
val OnBrandDark = Color(0xFF0B1B33)
val PrimaryContainerDark = Color(0xFF223A5E)
val OnPrimaryContainerDark = Color(0xFFC9DAF2)

val SecondaryDark = Color(0xFF7FB3AE)
val OnSecondaryDark = Color(0xFF0C211F)
val SecondaryContainerDark = Color(0xFF23413F)
val OnSecondaryContainerDark = Color(0xFFCDEAE7)

val TertiaryDark = Color(0xFFD99B7E)
val OnTertiaryDark = Color(0xFF3A1D12)
val TertiaryContainerDark = Color(0xFF5A3323)
val OnTertiaryContainerDark = Color(0xFFF3D9CB)

val AccentDark = Color(0xFFD9A066)
val OnAccentDark = Color(0xFF2A1810)

val InkDark = Color(0xFFEDEFF3)
val BackgroundDark = Color(0xFF0F131A)
val SurfaceDark = Color(0xFF171C24)
val SurfaceVariantDark = Color(0xFF232A35)
val SecondaryTextDark = Color(0xFF98A0AC)
val DividerDark = Color(0xFF262D38)
val OutlineDark = Color(0xFF6B7480)
val OutlineVariantDark = Color(0xFF333B47)
val DisabledDark = Color(0xFF2B313B)
val OnDisabledDark = Color(0xFF6B7280)

val SuccessDark = Color(0xFF4ADE9A)
val OnSuccessDark = Color(0xFF06231A)
val DangerDark = Color(0xFFF17171)
val OnDangerDark = Color(0xFF2B0A0A)
val ErrorContainerDark = Color(0xFF5C1F1F)
val OnErrorContainerDark = Color(0xFFF8D4D4)
val WarningDark = Color(0xFFFBBF24)
val OnWarningDark = Color(0xFF2B1B00)
val InfoDark = Color(0xFF6AA9EE)
val OnInfoDark = Color(0xFF001C3D)
val ReservedDark = Color(0xFFA78BFA)
val OnReservedDark = Color(0xFF2E1065)

val InverseSurfaceDark = Color(0xFFE7EAF0)
val InverseOnSurfaceDark = Color(0xFF171C24)
val InversePrimaryDark = Color(0xFF1D3A6E)
val ScrimDark = Color(0xFF000000)
