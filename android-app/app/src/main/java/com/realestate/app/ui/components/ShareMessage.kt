package com.realestate.app.ui.components

import com.realestate.app.data.Property
import com.realestate.app.data.code
import java.text.NumberFormat
import java.util.Locale

fun buildShareMessage(property: Property): String {
    val price = NumberFormat.getNumberInstance(Locale.US).format(property.price)
    return buildString {
        appendLine("🏠 ${property.title}")
        appendLine()
        appendLine("نوع: ${property.propertyType.label()} (${property.dealType.label()})")
        appendLine("منطقه: ${property.city}")
        appendLine("متراژ: ${property.area.toInt()} متر")
        appendLine("تعداد اتاق: ${property.rooms}")
        appendLine("قیمت: $price تومان")
        appendLine()
        appendLine("کد ملک: ${property.code}")
        appendLine("تماس: ${property.ownerPhone}")
    }
}
