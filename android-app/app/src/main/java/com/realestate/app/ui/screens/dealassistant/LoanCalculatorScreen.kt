package com.realestate.app.ui.screens.dealassistant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.realestate.app.data.dealassistant.RepaymentMethod
import com.realestate.app.data.dealassistant.calculateLoanSchedule
import com.realestate.app.data.dealassistant.simulateEarlyRepayment
import com.realestate.app.ui.components.AppCard
import com.realestate.app.ui.components.CollapsibleSection
import com.realestate.app.ui.components.MoneyField
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.ui.theme.extendedColors

@Composable
internal fun LoanCalculatorFull(prefillPrincipal: Long?) {
    var principal by remember { mutableStateOf(prefillPrincipal?.toString() ?: "") }
    var annualRate by remember { mutableStateOf("23") }
    var months by remember { mutableStateOf("60") }
    var gracePeriod by remember { mutableStateOf("0") }
    var method by remember { mutableStateOf(RepaymentMethod.EQUAL_INSTALLMENT) }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RepaymentMethod.entries.forEach { option ->
            FilterChip(selected = method == option, onClick = { method = option }, label = { Text(option.label) })
        }
    }
    Spacer(modifier = Modifier.height(Spacing.sm))
    MoneyField("مبلغ وام (تومان)", principal, { principal = it }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(Spacing.sm))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NumberField("نرخ سود سالانه", annualRate, { annualRate = it }, "٪", modifier = Modifier.weight(1f))
        NumberField("مدت بازپرداخت", months, { months = it }, "ماه", modifier = Modifier.weight(1f))
    }
    NumberField("دوره تنفس (اختیاری)", gracePeriod, { gracePeriod = it }, "ماه")

    val principalValue = principal.toAmount()
    val monthsRaw = months.toAmount().toInt()
    val monthsValue = monthsRaw.coerceAtLeast(1)
    val graceValue = gracePeriod.toAmount().toInt().coerceIn(0, monthsValue - 1)

    if (monthsRaw <= 0) {
        Text(
            "مدت بازپرداخت وارد نشده یا نامعتبر بود؛ برای محاسبه مقدار ۱ ماه در نظر گرفته شد.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.extendedColors.warning
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
    }
    val schedule = calculateLoanSchedule(principalValue, annualRate.toAmount(), monthsValue, graceValue, method)

    ResultsCard(
        listOf(
            if (method == RepaymentMethod.EQUAL_INSTALLMENT) "قسط ماهانه" to "${money(schedule.firstPayment)} تومان"
            else "اولین قسط (نزولی)" to "${money(schedule.firstPayment)} تومان",
            "کل بازپرداخت" to "${money(schedule.totalPayment)} تومان",
            "کل سود" to "${money(schedule.totalInterest)} تومان"
        )
    )

    MethodologyNote(
        if (method == RepaymentMethod.EQUAL_INSTALLMENT)
            "در روش اقساط مساوی، قسط ماهانه با فرمول استاندارد وام‌های بانکی محاسبه می‌شود: قسط = اصل وام × نرخ ماهانه × (۱ + نرخ ماهانه)^تعداد اقساط ÷ ((۱ + نرخ ماهانه)^تعداد اقساط − ۱). " +
                "در هر ماه بخشی از قسط صرف سود مانده بدهی و بقیه صرف کاهش اصل وام می‌شود. در صورت وجود دوره تنفس، در آن ماه‌ها فقط سود مانده کامل پرداخت می‌شود و قسط اصلی پس از پایان تنفس روی مدت باقی‌مانده محاسبه می‌گردد."
        else
            "در روش اصل ثابت، هر ماه مبلغ ثابتی از اصل وام (اصل وام ÷ تعداد اقساط) پرداخت می‌شود و سود آن ماه روی مانده باقی‌مانده محاسبه می‌شود؛ به همین دلیل قسط‌ها با گذر زمان کاهش می‌یابند."
    )

    if (schedule.schedule.isNotEmpty()) {
        Spacer(modifier = Modifier.height(Spacing.lg))
        Text("جدول اقساط (۱۲ ماه اول)", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(Spacing.sm))
        AppCard(modifier = Modifier.fillMaxWidth()) {
            LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                items(schedule.schedule.take(12), key = { it.period }) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ماه ${row.period}", style = MaterialTheme.typography.bodySmall)
                        Text("${money(row.payment)}", style = MaterialTheme.typography.bodySmall)
                        Text("مانده: ${money(row.balance)}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        if (monthsValue > 12) {
            Spacer(modifier = Modifier.height(Spacing.sm))
            val yearlyRows = schedule.schedule.chunked(12).mapIndexed { index, chunk ->
                val yearInterest = chunk.sumOf { it.interest }
                val yearPaid = chunk.sumOf { it.payment }
                val endBalance = chunk.last().balance
                Triple(index + 1, yearPaid to yearInterest, endBalance)
            }
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("خلاصه سالانه", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    yearlyRows.forEach { (year, paidAndInterest, endBalance) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("سال $year", style = MaterialTheme.typography.bodySmall)
                            Text("سود: ${money(paidAndInterest.second)}", style = MaterialTheme.typography.bodySmall)
                            Text("مانده: ${money(endBalance)}", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }

    var extraMonth by remember { mutableStateOf("") }
    var extraAmount by remember { mutableStateOf("") }
    Spacer(modifier = Modifier.height(Spacing.lg))
    CollapsibleSection(title = "شبیه‌سازی پیش‌پرداخت زودهنگام", subtitle = "تأثیر یک پرداخت اضافه بر کل سود") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField("در ماه شماره", extraMonth, { extraMonth = it }, modifier = Modifier.weight(1f))
        }
        MoneyField("مبلغ پیش‌پرداخت (تومان)", extraAmount, { extraAmount = it }, modifier = Modifier.fillMaxWidth())

        if (extraMonth.isNotBlank() && extraAmount.isNotBlank()) {
            val comparison = simulateEarlyRepayment(
                principalValue,
                annualRate.toAmount(),
                monthsValue,
                extraMonth.toAmount().toInt(),
                extraAmount.toAmount(),
                graceValue,
                method
            )
            ResultsCard(
                listOf(
                    "سود بدون پیش‌پرداخت" to "${money(comparison.originalTotalInterest)} تومان",
                    "سود با پیش‌پرداخت" to "${money(comparison.newTotalInterest)} تومان",
                    "صرفه‌جویی در سود" to "${money(comparison.interestSaved)} تومان",
                    "کاهش مدت وام" to "${comparison.monthsSaved} ماه"
                )
            )
            MethodologyNote(
                "شبیه‌سازی با پرداخت مبلغ اضافه در ماه انتخاب‌شده، مستقیماً از مانده اصل وام کسر می‌شود؛ سپس با همان مبلغ قسط ثابت اولیه، جدول بازپرداخت دوباره محاسبه می‌شود تا ببیند وام زودتر از موعد تسویه می‌شود یا نه. صرفه‌جویی در سود = سود کل بدون پیش‌پرداخت − سود کل با پیش‌پرداخت."
            )
        }
    }
}
