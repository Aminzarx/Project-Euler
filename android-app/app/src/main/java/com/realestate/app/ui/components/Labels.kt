package com.realestate.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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

@Composable
fun PropertyStatus.color(): Color = when (this) {
    PropertyStatus.NEW -> MaterialTheme.colorScheme.primary
    PropertyStatus.READY -> MaterialTheme.extendedColors.info
    PropertyStatus.ACTIVE -> MaterialTheme.extendedColors.success
    PropertyStatus.NEGOTIATING -> MaterialTheme.extendedColors.warning
    PropertyStatus.RESERVED -> MaterialTheme.extendedColors.warning
    PropertyStatus.SOLD -> MaterialTheme.colorScheme.tertiary
    PropertyStatus.RENTED -> MaterialTheme.colorScheme.tertiary
    PropertyStatus.ARCHIVED -> MaterialTheme.colorScheme.onSurfaceVariant
}
