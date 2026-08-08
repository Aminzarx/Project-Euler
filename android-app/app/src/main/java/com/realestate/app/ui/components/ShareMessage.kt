package com.realestate.app.ui.components

import com.realestate.app.data.CaseType
import com.realestate.app.data.Property
import com.realestate.app.data.code
import java.text.NumberFormat
import java.util.Locale

/**
 * agentPhone is the agent's own contact number, not [Property.ownerPhone]. Sharing the owner's
 * number directly with a prospective buyer would let them bypass the agent entirely (and their
 * commission) — the Story Card feature already got this right by design; this brings the plain
 * text share message shared from list/detail/favorites screens in line with it.
 *
 * A Client Request case has no listing to describe — sharing one (e.g. between colleagues, to
 * hand off a lead) builds a request summary instead of an owner-style price/area/room pitch.
 */
fun buildShareMessage(property: Property, agentPhone: String): String =
    if (property.caseType == CaseType.CLIENT_REQUEST) buildRequestShareMessage(property, agentPhone)
    else buildListingShareMessage(property, agentPhone)

private fun buildListingShareMessage(property: Property, agentPhone: String): String {
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

private fun buildRequestShareMessage(property: Property, agentPhone: String): String {
    val numberFormat = NumberFormat.getNumberInstance(Locale.US)
    val budget = when {
        property.budgetMin != null && property.budgetMax != null ->
            "${numberFormat.format(property.budgetMin)} تا ${numberFormat.format(property.budgetMax)} تومان"
        property.budgetMax != null -> "تا ${numberFormat.format(property.budgetMax)} تومان"
        property.budgetMin != null -> "از ${numberFormat.format(property.budgetMin)} تومان"
        else -> "مشخص نشده"
    }
    return buildString {
        appendLine("🔍 درخواست: ${property.title}")
        appendLine()
        appendLine("نوع: ${property.propertyType.label()} (${property.dealType.label()})")
        appendLine("منطقه: ${property.city}")
        if (property.desiredMinArea != null || property.desiredMaxArea != null) {
            val min = property.desiredMinArea?.toInt()
            val max = property.desiredMaxArea?.toInt()
            appendLine("متراژ مورد نظر: ${if (min != null && max != null) "$min تا $max" else (min ?: max)} متر")
        }
        if (property.desiredBedrooms != null) {
            appendLine("تعداد اتاق مورد نظر: ${property.desiredBedrooms}")
        }
        if (property.preferredAreas.isNotEmpty()) {
            appendLine("محله‌های مدنظر: ${property.preferredAreas.joinToString("، ")}")
        }
        appendLine("بودجه: $budget")
        appendLine()
        appendLine("کد پرونده: ${property.code}")
        if (agentPhone.isNotBlank()) {
            appendLine("تماس: $agentPhone")
        }
    }
}
