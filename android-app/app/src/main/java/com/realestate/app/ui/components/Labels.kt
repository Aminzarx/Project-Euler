package com.realestate.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CompareArrows
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.realestate.app.data.DealType
import com.realestate.app.data.PropertyStatus
import com.realestate.app.data.PropertyType
import com.realestate.app.ui.theme.extendedColors

fun PropertyType.label(): String = when (this) {
    PropertyType.APARTMENT -> "آپارتمان"
    PropertyType.VILLA -> "ویلا"
    PropertyType.LAND -> "زمین"
    PropertyType.OFFICE -> "اداری"
    PropertyType.SHOP -> "مغازه"
}

fun DealType.label(): String = when (this) {
    DealType.SALE -> "فروش"
    DealType.RENT -> "اجاره"
}

fun PropertyStatus.label(): String = when (this) {
    PropertyStatus.NEW -> "جدید"
    PropertyStatus.READY -> "آماده"
    PropertyStatus.ACTIVE -> "فعال"
    PropertyStatus.NEGOTIATING -> "در حال مذاکره"
    PropertyStatus.RESERVED -> "رزرو شده"
    PropertyStatus.SOLD -> "فروخته شده"
    PropertyStatus.RENTED -> "اجاره داده شده"
    PropertyStatus.ARCHIVED -> "بایگانی‌شده"
}

// NOTE: PropertyStatus.NEW reads MaterialTheme.colorScheme.primary, and — in this app's current
// theme — primary/tertiary resolve to the identical brand color. SOLD/RENTED therefore deliberately
// read colorScheme.outline (a genuinely different, already-existing token) rather than tertiary, so
// a new listing and a closed one never render as the same color. RESERVED gets its own dedicated
// "reserved" token instead of reusing warning, since NEGOTIATING already claims that color and the
// two are functionally different states a user needs to tell apart at a glance.
@Composable
fun PropertyStatus.color(): Color = when (this) {
    PropertyStatus.NEW -> MaterialTheme.colorScheme.primary
    PropertyStatus.READY -> MaterialTheme.extendedColors.info
    PropertyStatus.ACTIVE -> MaterialTheme.extendedColors.success
    PropertyStatus.NEGOTIATING -> MaterialTheme.extendedColors.warning
    PropertyStatus.RESERVED -> MaterialTheme.extendedColors.reserved
    PropertyStatus.SOLD -> MaterialTheme.colorScheme.outline
    PropertyStatus.RENTED -> MaterialTheme.colorScheme.outline
    PropertyStatus.ARCHIVED -> MaterialTheme.colorScheme.onSurfaceVariant
}

/** A status-specific icon so a status is recognizable without relying on color alone
 *  (color-blindness, grayscale screenshots, low-contrast displays). */
fun PropertyStatus.icon(): ImageVector = when (this) {
    PropertyStatus.NEW -> Icons.Rounded.AutoAwesome
    PropertyStatus.READY -> Icons.Rounded.CheckCircle
    PropertyStatus.ACTIVE -> Icons.Rounded.Storefront
    PropertyStatus.NEGOTIATING -> Icons.Rounded.CompareArrows
    PropertyStatus.RESERVED -> Icons.Rounded.HourglassEmpty
    PropertyStatus.SOLD -> Icons.Rounded.EmojiEvents
    PropertyStatus.RENTED -> Icons.Rounded.Check
    PropertyStatus.ARCHIVED -> Icons.Rounded.History
}
