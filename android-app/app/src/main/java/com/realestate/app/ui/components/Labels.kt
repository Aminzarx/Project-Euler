package com.realestate.app.ui.components

import com.realestate.app.data.DealType
import com.realestate.app.data.PropertyType

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
