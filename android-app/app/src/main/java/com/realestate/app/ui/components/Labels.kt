package com.realestate.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Agriculture
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CompareArrows
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.Factory
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.PendingActions
import androidx.compose.material.icons.rounded.PersonSearch
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.StickyNote2
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Terrain
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Villa
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Warehouse
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.realestate.app.data.CaseFlag
import com.realestate.app.data.CasePriority
import com.realestate.app.data.CaseTransactionType
import com.realestate.app.data.CaseType
import com.realestate.app.data.DealType
import com.realestate.app.data.MortgageStatus
import com.realestate.app.data.PropertyStatus
import com.realestate.app.data.PropertyType
import com.realestate.app.data.RequestValidityType
import com.realestate.app.data.property.TimelineEventType
import com.realestate.app.ui.theme.extendedColors

fun PropertyType.label(): String = when (this) {
    PropertyType.APARTMENT -> "آپارتمان"
    PropertyType.VILLA -> "ویلا"
    PropertyType.LAND -> "زمین"
    PropertyType.OFFICE -> "اداری"
    PropertyType.SHOP -> "تجاری"
    PropertyType.WAREHOUSE -> "انبار"
    PropertyType.INDUSTRIAL -> "صنعتی"
    PropertyType.GARDEN -> "باغ"
    PropertyType.FARM -> "مزرعه"
    PropertyType.BUILDING -> "ساختمان"
    PropertyType.PROJECT -> "پروژه"
}

fun PropertyType.icon(): ImageVector = when (this) {
    PropertyType.APARTMENT -> Icons.Rounded.Apartment
    PropertyType.VILLA -> Icons.Rounded.Villa
    PropertyType.LAND -> Icons.Rounded.Terrain
    PropertyType.OFFICE -> Icons.Rounded.Business
    PropertyType.SHOP -> Icons.Rounded.Storefront
    PropertyType.WAREHOUSE -> Icons.Rounded.Warehouse
    PropertyType.INDUSTRIAL -> Icons.Rounded.Factory
    PropertyType.GARDEN -> Icons.Rounded.Park
    PropertyType.FARM -> Icons.Rounded.Agriculture
    PropertyType.BUILDING -> Icons.Rounded.LocationCity
    PropertyType.PROJECT -> Icons.Rounded.Construction
}

fun DealType.label(): String = when (this) {
    DealType.SALE -> "فروش"
    DealType.RENT -> "اجاره"
}

fun CaseType.label(): String = when (this) {
    CaseType.OWNER -> "مالک"
    CaseType.CLIENT_REQUEST -> "درخواست مشتری"
}

fun CaseType.icon(): ImageVector = when (this) {
    CaseType.OWNER -> Icons.Rounded.Storefront
    CaseType.CLIENT_REQUEST -> Icons.Rounded.PersonSearch
}

fun CaseTransactionType.label(): String = when (this) {
    CaseTransactionType.SALE -> "فروش"
    CaseTransactionType.PURCHASE -> "خرید"
    CaseTransactionType.FULL_MORTGAGE -> "رهن کامل"
    CaseTransactionType.RENT -> "اجاره"
    CaseTransactionType.MORTGAGE_AND_RENT -> "رهن و اجاره"
    CaseTransactionType.CONSTRUCTION_PARTNERSHIP -> "مشارکت در ساخت"
    CaseTransactionType.PRE_SALE -> "پیش‌فروش"
    CaseTransactionType.PRE_PURCHASE -> "پیش‌خرید"
    CaseTransactionType.PROPERTY_EXCHANGE -> "معاوضه ملک"
    CaseTransactionType.INVESTMENT -> "سرمایه‌گذاری"
    CaseTransactionType.OTHER -> "سایر"
}

fun CasePriority.label(): String = when (this) {
    CasePriority.LOW -> "کم"
    CasePriority.NORMAL -> "معمولی"
    CasePriority.HIGH -> "بالا"
    CasePriority.URGENT -> "فوری"
}

@Composable
fun CasePriority.color(): Color = when (this) {
    CasePriority.LOW -> MaterialTheme.colorScheme.onSurfaceVariant
    CasePriority.NORMAL -> MaterialTheme.colorScheme.primary
    CasePriority.HIGH -> MaterialTheme.extendedColors.warning
    CasePriority.URGENT -> MaterialTheme.extendedColors.danger
}

fun RequestValidityType.label(): String = when (this) {
    RequestValidityType.NO_EXPIRATION -> "بدون انقضا"
    RequestValidityType.DAYS_7 -> "۷ روز"
    RequestValidityType.DAYS_15 -> "۱۵ روز"
    RequestValidityType.DAYS_30 -> "۳۰ روز"
    RequestValidityType.DAYS_60 -> "۶۰ روز"
    RequestValidityType.DAYS_90 -> "۹۰ روز"
    RequestValidityType.CUSTOM -> "تاریخ دلخواه"
}

fun MortgageStatus.label(): String = when (this) {
    MortgageStatus.NONE -> "بدون رهن"
    MortgageStatus.PARTIAL -> "رهن جزئی"
    MortgageStatus.FULL -> "در رهن کامل"
}

fun CaseFlag.label(): String = when (this) {
    CaseFlag.VACANT -> "تخلیه"
    CaseFlag.NEGOTIABLE -> "قابل مذاکره"
    CaseFlag.IMMEDIATE_SALE -> "فروش فوری"
    CaseFlag.EXCHANGE_ACCEPTED -> "معاوضه پذیرفته می‌شود"
    CaseFlag.PARKING_REQUIRED -> "پارکینگ"
    CaseFlag.ELEVATOR_REQUIRED -> "آسانسور"
    CaseFlag.STORAGE_REQUIRED -> "انباری"
    CaseFlag.BALCONY_REQUIRED -> "بالکن"
    CaseFlag.GARDEN_REQUIRED -> "حیاط/باغچه"
    CaseFlag.LUXURY_REQUIRED -> "لوکس"
    CaseFlag.FURNISHED_REQUIRED -> "مبله"
    CaseFlag.NEW_BUILDING_REQUIRED -> "نوساز"
    CaseFlag.ACCESSIBILITY_REQUIRED -> "دسترسی معلولین"
    CaseFlag.LOAN_REQUIRED -> "نیاز به وام"
    CaseFlag.FLEXIBLE_DEPOSIT -> "رهن قابل تعدیل"
    CaseFlag.FLEXIBLE_RENT -> "اجاره قابل تعدیل"
    CaseFlag.CONVERSION_ALLOWED -> "تبدیل رهن به اجاره"
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
    PropertyStatus.SEARCHING -> "در حال جستجو"
    PropertyStatus.VISITED -> "بازدید شده"
    PropertyStatus.WAITING -> "در انتظار"
    PropertyStatus.CONTRACT_SIGNED -> "قرارداد بسته شد"
    PropertyStatus.CANCELLED -> "لغو شده"
    PropertyStatus.EXPIRED -> "منقضی شده"
}

// NOTE: PropertyStatus.NEW reads MaterialTheme.colorScheme.primary, and — in this app's current
// theme — primary/tertiary resolve to the identical brand color. SOLD/RENTED therefore deliberately
// read colorScheme.outline (a genuinely different, already-existing token) rather than tertiary, so
// a new listing and a closed one never render as the same color. RESERVED gets its own dedicated
// "reserved" token instead of reusing warning, since NEGOTIATING already claims that color and the
// two are functionally different states a user needs to tell apart at a glance. The Client-Request
// statuses added for the Case redesign reuse this same limited palette rather than inventing new
// colors, mapped by what they mean rather than which case type they belong to: SEARCHING/WAITING
// are "in progress" (info/warning), VISITED is a milestone (accent), CONTRACT_SIGNED is a closed
// win (success, matching SOLD's meaning), CANCELLED is a closed loss (danger), EXPIRED is closed
// and inactive (outline, matching SOLD/RENTED's muted treatment).
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
    PropertyStatus.SEARCHING -> MaterialTheme.extendedColors.info
    PropertyStatus.VISITED -> MaterialTheme.extendedColors.accent
    PropertyStatus.WAITING -> MaterialTheme.extendedColors.warning
    PropertyStatus.CONTRACT_SIGNED -> MaterialTheme.extendedColors.success
    PropertyStatus.CANCELLED -> MaterialTheme.extendedColors.danger
    PropertyStatus.EXPIRED -> MaterialTheme.colorScheme.outline
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
    PropertyStatus.SEARCHING -> Icons.Rounded.Search
    PropertyStatus.VISITED -> Icons.Rounded.Visibility
    PropertyStatus.WAITING -> Icons.Rounded.PendingActions
    PropertyStatus.CONTRACT_SIGNED -> Icons.Rounded.Description
    PropertyStatus.CANCELLED -> Icons.Rounded.Cancel
    PropertyStatus.EXPIRED -> Icons.Rounded.EventBusy
}

/** Per-event-type icon for timeline/activity feeds — shared by HomeScreen and the Profile dashboard. */
fun TimelineEventType.icon(): ImageVector = when (this) {
    TimelineEventType.CREATED -> Icons.Rounded.Add
    TimelineEventType.EDITED -> Icons.Rounded.Edit
    TimelineEventType.PRICE_CHANGED -> Icons.Rounded.TrendingUp
    TimelineEventType.SHARED -> Icons.Rounded.Share
    TimelineEventType.ARCHIVED -> Icons.Rounded.History
    TimelineEventType.RESTORED -> Icons.Rounded.CheckCircle
    TimelineEventType.NOTE_ADDED -> Icons.Rounded.StickyNote2
    TimelineEventType.FOLLOW_UP_SET -> Icons.Rounded.EventAvailable
}
