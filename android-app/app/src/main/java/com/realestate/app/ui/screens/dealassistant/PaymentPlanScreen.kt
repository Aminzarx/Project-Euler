package com.realestate.app.ui.screens.dealassistant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.RestartAlt
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
import com.realestate.app.ui.components.ButtonTone
import com.realestate.app.ui.components.MoneyField
import com.realestate.app.ui.components.PrimaryButton
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.ui.theme.extendedColors
import kotlin.math.abs

private class PaymentPlanItemState(var label: String, var amount: String, var monthOffset: String, val id: Int)

private const val MAX_QUICK_SPLIT_COUNT = 60

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
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("ردیف‌های پرداخت", style = MaterialTheme.typography.titleSmall)
        Text("${items.size} ردیف", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Spacer(modifier = Modifier.height(Spacing.sm))

    if (items.isNotEmpty()) {
        LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
            itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
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
            }
        }
        Spacer(modifier = Modifier.height(Spacing.sm))
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PrimaryButton(
            text = "افزودن ردیف پرداخت",
            onClick = {
                items.add(PaymentPlanItemState(label = "قسط ${items.size + 1}", amount = "", monthOffset = "", id = nextId))
                nextId++
            },
            icon = Icons.Rounded.Add,
            modifier = Modifier.weight(1f)
        )
        if (items.isNotEmpty()) {
            PrimaryButton(
                text = "شروع دوباره",
                onClick = { items.clear() },
                icon = Icons.Rounded.RestartAlt,
                tone = ButtonTone.DANGER,
                modifier = Modifier.weight(1f)
            )
        }
    }

    Spacer(modifier = Modifier.height(Spacing.lg))
    Text("تقسیم خودکار باقی‌مانده", style = MaterialTheme.typography.titleSmall)
    Spacer(modifier = Modifier.height(Spacing.sm))
    val remainingBeforeSplit = (totalPrice.toAmount() - downPayment.toAmount() - items.sumOf { it.amount.toAmount() }).coerceAtLeast(0.0)
    val splitCount = quickSplitCount.toAmount().toInt()
    val splitCountValid = splitCount in 1..MAX_QUICK_SPLIT_COUNT
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        NumberFieldCompact("تعداد", quickSplitCount, modifier = Modifier.weight(1f)) { quickSplitCount = it }
        PrimaryButton(
            text = "تقسیم کن",
            enabled = splitCountValid && remainingBeforeSplit > 0,
            onClick = {
                repeat(splitCount) { i ->
                    val each = remainingBeforeSplit / splitCount
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
                quickSplitCount = ""
            },
            modifier = Modifier.weight(1f)
        )
    }
    if (quickSplitCount.isNotBlank() && splitCount > MAX_QUICK_SPLIT_COUNT) {
        Text(
            "حداکثر $MAX_QUICK_SPLIT_COUNT ردیف در یک تقسیم مجاز است.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.extendedColors.danger
        )
    }

    val totalAllocated = downPayment.toAmount() + items.sumOf { it.amount.toAmount() }
    val remaining = totalPrice.toAmount() - totalAllocated
    val remainingColor = when {
        remaining < 0 -> MaterialTheme.extendedColors.danger
        remaining == 0.0 && totalPrice.isNotBlank() -> MaterialTheme.extendedColors.success
        else -> MaterialTheme.colorScheme.onSurface
    }

    Spacer(modifier = Modifier.height(Spacing.md))
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "جمع پرداخت‌شده (پیش‌پرداخت + اقساط)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("${money(totalAllocated)} تومان", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    if (remaining < 0) "بیش از قیمت کل تخصیص داده شده" else "باقی‌مانده تا قیمت کل",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("${money(abs(remaining))} تومان", style = MaterialTheme.typography.titleMedium, color = remainingColor)
            }
        }
    }

    MethodologyNote(
        "جمع پرداخت‌شده = پیش‌پرداخت + مجموع مبلغ همه ردیف‌های پرداخت. باقی‌مانده تا قیمت کل = قیمت کل قرارداد − جمع پرداخت‌شده. " +
            "در تقسیم خودکار، ابتدا باقی‌مانده (قیمت کل منهای پیش‌پرداخت و ردیف‌های موجود) محاسبه و سپس به‌طور مساوی بین تعداد اقساط درخواستی تقسیم می‌شود. " +
            "برای جلوگیری از ساخت ناخواسته تعداد بسیار زیادی ردیف، تقسیم خودکار حداکثر تا $MAX_QUICK_SPLIT_COUNT ردیف در هر بار مجاز است."
    )
}
