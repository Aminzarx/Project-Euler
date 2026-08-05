package com.realestate.app.ui.components

import com.realestate.app.data.Property
import com.realestate.app.data.code
import java.text.NumberFormat
import java.util.Locale

/**
 * agentPhone is the agent's own contact number, not [Property.ownerPhone]. Sharing the owner's
 * number directly with a prospective buyer would let them bypass the agent entirely (and their
 * commission) — the Story Card feature already got this right by design; this brings the plain
 * text share message shared from list/detail/favorites screens in line with it.
 */
fun buildShareMessage(property: Property, agentPhone: String): String {
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
        if (agentPhone.isNotBlank()) {
            appendLine("تماس: $agentPhone")
        }
    }
}
