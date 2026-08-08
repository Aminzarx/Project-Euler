package com.realestate.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val danger: Color,
    val onDanger: Color,
    val warning: Color,
    val onWarning: Color,
    val info: Color,
    val onInfo: Color,
    val reserved: Color,
    val onReserved: Color,
    val secondaryText: Color,
    val divider: Color,
    val disabled: Color,
    val onDisabled: Color,
    val accent: Color,
    val onAccent: Color
)

val LightExtendedColors = ExtendedColors(
    success = SuccessLight,
    onSuccess = OnSuccessLight,
    danger = DangerLight,
    onDanger = OnDangerLight,
    warning = WarningLight,
    onWarning = OnWarningLight,
    info = InfoLight,
    onInfo = OnInfoLight,
    reserved = ReservedLight,
    onReserved = OnReservedLight,
    secondaryText = SecondaryTextLight,
    divider = DividerLight,
    disabled = DisabledLight,
    onDisabled = OnDisabledLight,
    accent = AccentLight,
    onAccent = OnAccentLight
)

val DarkExtendedColors = ExtendedColors(
    success = SuccessDark,
    onSuccess = OnSuccessDark,
    danger = DangerDark,
    onDanger = OnDangerDark,
    warning = WarningDark,
    onWarning = OnWarningDark,
    info = InfoDark,
    onInfo = OnInfoDark,
    reserved = ReservedDark,
    onReserved = OnReservedDark,
    secondaryText = SecondaryTextDark,
    divider = DividerDark,
    disabled = DisabledDark,
    onDisabled = OnDisabledDark,
    accent = AccentDark,
    onAccent = OnAccentDark
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    get() = LocalExtendedColors.current
