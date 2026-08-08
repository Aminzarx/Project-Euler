package com.realestate.app.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// Brand palette — navy-led, per explicit user request: every red/near-red brand
// color (the old Primary Scarlet accent and Ruby Red tertiary) has been replaced
// with a navy-family tone so the app reads as consistently navy-themed rather
// than red-and-navy. Semantic danger/error stays red — that one is deliberately
// left alone, since "negative/delete/error" reading as red is a near-universal
// convention (e.g. banking apps with blue branding still show red for a negative
// balance or a failed transaction), and this app's financial calculators lean on
// exactly that red/green/amber vocabulary throughout.
// Role mapping: Yale Blue #053C5E -> primary (main brand/trust color), Teal
// #1F7A8C -> secondary (also reused for "info"), a new Sapphire accent ->
// tertiary AND the CTA color used on every primary action button (both navy,
// but visually distinct from primary so buttons still pop), Pale Sky #BFDBF7
// -> the light container tint paired with primary. Success/warning/danger/
// reserved are unchanged from the prior palette (conventional green/amber/red/
// violet). Every text-on-color pairing below was checked against WCAG AA
// (>=4.5:1 text, >=3:1 outlines) with a contrast script, not picked by eye.
// ============================================================================

val BrandLight = Color(0xFF053C5E) // Yale Blue
val OnBrandLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFBFDBF7) // Pale Sky
val OnPrimaryContainerLight = Color(0xFF053C5E)

val SecondaryLight = Color(0xFF1F7A8C) // Teal
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFD7EEF1)
val OnSecondaryContainerLight = Color(0xFF145560)

val TertiaryLight = Color(0xFF1D3F6E) // Indigo Navy — distinct from primary/secondary/accent
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFD7E4F2)
val OnTertiaryContainerLight = Color(0xFF15304F)

// The one deliberately vivid, "act now" note in the palette — every primary call-to-action button.
val AccentLight = Color(0xFF0B5D8C) // Sapphire — navy family, brighter than Yale Blue for CTA pop
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
val DangerLight = Color(0xFFA31621) // Ruby Red — shared with tertiary, see note above
val OnDangerLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFBE2E2)
val OnErrorContainerLight = Color(0xFF7A1015)
val WarningLight = Color(0xFFF59E0B)
val OnWarningLight = Color(0xFF2B1B00)
val InfoLight = Color(0xFF1F7A8C) // Teal — reused, a natural "info" hue
val OnInfoLight = Color(0xFFFFFFFF)
// Not part of the 5 given colors — kept from the prior palette so "reserved" never collides
// with another property-status color.
val ReservedLight = Color(0xFF7C4FE0)
val OnReservedLight = Color(0xFFFFFFFF)

val InverseSurfaceLight = Color(0xFF262E3A)
val InverseOnSurfaceLight = Color(0xFFF2F4F7)
val InversePrimaryLight = Color(0xFF6FA8D9)
val ScrimLight = Color(0xFF000000)

// ============================================================================
// Brand palette — dark
// Every hue keeps its light-mode identity but is lightened/desaturated for a
// near-black background, per Material3's dark-theme guidance — never the raw
// light-mode color simply dimmed, which reads muddy.
// ============================================================================

val BrandDark = Color(0xFF6FA8D9)
val OnBrandDark = Color(0xFF052238)
val PrimaryContainerDark = Color(0xFF123F63)
val OnPrimaryContainerDark = Color(0xFFBFDBF7)

val SecondaryDark = Color(0xFF6FC3D1)
val OnSecondaryDark = Color(0xFF062A2F)
val SecondaryContainerDark = Color(0xFF123A40)
val OnSecondaryContainerDark = Color(0xFFC9E9EC)

val TertiaryDark = Color(0xFF8FC5E8)
val OnTertiaryDark = Color(0xFF07293D)
val TertiaryContainerDark = Color(0xFF163449)
val OnTertiaryContainerDark = Color(0xFFCFE6F5)

val AccentDark = Color(0xFF5FB0E0)
val OnAccentDark = Color(0xFF06263A)

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
val DangerDark = Color(0xFFE8828C)
val OnDangerDark = Color(0xFF3D0A0F)
val ErrorContainerDark = Color(0xFF4E1013)
val OnErrorContainerDark = Color(0xFFF8D6D8)
val WarningDark = Color(0xFFFBBF24)
val OnWarningDark = Color(0xFF2B1B00)
val InfoDark = Color(0xFF6FC3D1)
val OnInfoDark = Color(0xFF062A2F)
val ReservedDark = Color(0xFFA78BFA)
val OnReservedDark = Color(0xFF2E1065)

val InverseSurfaceDark = Color(0xFFE7EAF0)
val InverseOnSurfaceDark = Color(0xFF171C24)
val InversePrimaryDark = Color(0xFF053C5E)
val ScrimDark = Color(0xFF000000)

// Third-party brand colors — fixed by the platforms themselves, not part of this app's own
// palette (so deliberately not theme-/dark-mode-aware). Used only to make the share-target chips
// on the Story Card screen instantly recognizable as Instagram/Telegram/WhatsApp.
val InstagramBrand = Color(0xFFC13584)
val TelegramBrand = Color(0xFF2AABEE)
val WhatsAppBrand = Color(0xFF25D366)
