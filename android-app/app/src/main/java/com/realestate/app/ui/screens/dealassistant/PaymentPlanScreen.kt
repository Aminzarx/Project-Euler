package com.realestate.app.ui.screens.dealassistant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.realestate.app.ui.components.AppCard
import com.realestate.app.ui.components.MoneyField
import com.realestate.app.ui.components.PrimaryButton
import com.realestate.app.ui.theme.Spacing

private class PaymentPlanItemState(var label: String, var amount: String, var monthOffset: String, val id: Int)

@Composable
internal fun PaymentPlanBuilder(prefillTotalPrice: Long?) {
    var totalPrice by remember { mutableStateOf(prefillTotalPrice?.toString() ?: "") }
    var downPayment by remember { mutableStateOf("") }
    val items = remember { mutableStateListOf<PaymentPlanItemState>() }
    var nextId by remember { mutableStateOf(0) }
    var quickSplitCount by remember { mutableStateOf("") }

    Text(
        "برای هر نوع برنامه پرداخت (اقساط سازنده، خریدار یا فروشنده) ردیف‌های دلخواه اضافه کنید.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(Spacing.md))
    MoneyField("قیمت کل قرارداد (تومان)", totalPrice, { totalPrice = it }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(Spacing.sm))
    MoneyField("پیش‌پرداخت (تومان)", downPayment, { downPayment = it }, modifier = Modifier.fillMaxWidth())

    Spacer(modifier = Modifier.height(Spacing.lg))
    Text("ردیف‌های پرداخت", style = MaterialTheme.typography.titleSmall)
    Spacer(modifier = Modifier.height(Spacing.sm))

    items.forEachIndexed { index, item ->
        AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = item.label,
                        onValueChange = { items[index] = PaymentPlanItemState(it, item.amount, item.monthOffset, item.id) },
                        label = { Text("عنوان قسط") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MoneyField(
                            "مبلغ (تومان)",
                            item.amount,
                            { items[index] = PaymentPlanItemState(item.label, it, item.monthOffset, item.id) },
                            modifier = Modifier.weight(1f)
                        )
                        NumberFieldCompact(
                            "ماه",
                            item.monthOffset,
                            modifier = Modifier.weight(1f)
                        ) { items[index] = PaymentPlanItemState(item.label, item.amount, it, item.id) }
                    }
                }
                IconButton(onClick = { items.removeAt(index) }) {
                    Icon(Icons.Rounded.Delete, contentDescription = "حذف ردیف")
                }
            }
        }
        Spacer(modifier = Modifier.height(Spacing.sm))
    }

    PrimaryButton(
        text = "افزودن ردیف پرداخت",
        onClick = {
            items.add(PaymentPlanItemState(label = "قسط ${items.size + 1}", amount = "", monthOffset = "", id = nextId))
            nextId++
        },
        icon = Icons.Rounded.Add,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(Spacing.lg))
    Text("تقسیم خودکار باقی‌مانده", style = MaterialTheme.typography.titleSmall)
    Spacer(modifier = Modifier.height(Spacing.sm))
    val remainingBeforeSplit = (totalPrice.toAmount() - downPayment.toAmount() - items.sumOf { it.amount.toAmount() }).coerceAtLeast(0.0)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        NumberFieldCompact("تعداد", quickSplitCount, modifier = Modifier.weight(1f)) { quickSplitCount = it }
        PrimaryButton(
            text = "تقسیم کن",
            onClick = {
                val count = quickSplitCount.toAmount().toInt()
                if (count > 0) {
                    val each = remainingBeforeSplit / count
                    repeat(count) { i ->
                        items.add(
                            PaymentPlanItemState(
                                label = "قسط ${items.size + 1}",
                                amount = each.toLong().toString(),
                                monthOffset = (i + 1).toString(),
                                id = nextId
                            )
                        )
                        nextId++
                    }
                }
            },
            modifier = Modifier.weight(1f)
        )
    }

    val totalAllocated = downPayment.toAmount() + items.sumOf { it.amount.toAmount() }
    val remaining = totalPrice.toAmount() - totalAllocated
    ResultsCard(
        listOf(
            "جمع پرداخت‌شده (پیش‌پرداخت + اقساط)" to "${money(totalAllocated)} تومان",
            "باقی‌مانده تا قیمت کل" to "${money(remaining)} تومان"
        )
    )
}
