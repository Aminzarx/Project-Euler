package com.realestate.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.realestate.app.data.wallet.TransactionStatus
import com.realestate.app.data.wallet.TransactionType
import com.realestate.app.data.wallet.WalletTransaction
import com.realestate.app.ui.components.AppCard
import com.realestate.app.ui.components.PrimaryButton
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.ui.theme.extendedColors
import com.realestate.app.ui.theme.heroGradient
import com.realestate.app.viewmodel.RechargeStatus
import com.realestate.app.viewmodel.WalletViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val quickAmounts = listOf(50_000L, 100_000L, 200_000L, 500_000L)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(viewModel: WalletViewModel, onBack: () -> Unit) {
    val balance by viewModel.balance.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val rechargeStatus by viewModel.rechargeStatus.collectAsStateWithLifecycle()
    var showRechargeSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("کیف پول") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Box(
                modifier = Modifier
                    .padding(Spacing.screen)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(heroGradient())
                    .padding(Spacing.cardPadding)
            ) {
                Column {
                    Text(
                        "موجودی کیف پول",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        formatToman(balance),
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PrimaryButton(
                        text = "شارژ کیف پول",
                        onClick = { showRechargeSheet = true },
                        icon = Icons.Rounded.Add,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Text(
                "تاریخچه تراکنش‌ها",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = Spacing.screen)
            )
            Spacer(modifier = Modifier.height(Spacing.md))

            if (transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("هنوز تراکنشی ثبت نشده", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(Spacing.screen),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    items(transactions, key = { it.id }) { tx ->
                        TransactionRow(tx)
                    }
                }
            }
        }
    }

    if (showRechargeSheet) {
        RechargeSheet(
            onDismiss = {
                showRechargeSheet = false
                viewModel.resetRechargeStatus()
            },
            onConfirm = { amount -> viewModel.recharge(amount) },
            rechargeStatus = rechargeStatus
        )
    }
}

@Composable
private fun TransactionRow(tx: WalletTransaction) {
    AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (icon, tint) = when (tx.status) {
                TransactionStatus.SUCCESS -> Icons.Rounded.CheckCircle to MaterialTheme.extendedColors.success
                TransactionStatus.FAILED -> Icons.Rounded.Error to MaterialTheme.extendedColors.danger
                TransactionStatus.PENDING -> Icons.Rounded.HourglassEmpty to MaterialTheme.extendedColors.warning
            }
            Icon(icon, contentDescription = null, tint = tint)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(tx.description, style = MaterialTheme.typography.titleSmall)
                Text(
                    SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(tx.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val sign = if (tx.type == TransactionType.RECHARGE) "+" else "-"
            Text(
                "$sign${formatToman(tx.amount)}",
                style = MaterialTheme.typography.titleSmall,
                color = if (tx.type == TransactionType.RECHARGE) {
                    MaterialTheme.extendedColors.success
                } else {
                    MaterialTheme.extendedColors.danger
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RechargeSheet(
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
    rechargeStatus: RechargeStatus
) {
    var selectedAmount by remember { mutableStateOf<Long?>(null) }
    var customAmount by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
            when (rechargeStatus) {
                RechargeStatus.IDLE -> {
                    Text("مبلغ شارژ را انتخاب کنید", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    QuickAmountGrid(selectedAmount) {
                        selectedAmount = it
                        customAmount = ""
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customAmount,
                        onValueChange = {
                            customAmount = it.filter { c -> c.isDigit() }
                            selectedAmount = null
                        },
                        label = { Text("مبلغ دلخواه (تومان)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    val amount = selectedAmount ?: customAmount.toLongOrNull()
                    PrimaryButton(
                        text = "پرداخت و شارژ",
                        onClick = { amount?.let(onConfirm) },
                        enabled = amount != null && amount > 0,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                RechargeStatus.PROCESSING -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("در حال پردازش پرداخت...")
                        }
                    }
                }

                RechargeStatus.SUCCESS -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.extendedColors.success,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("کیف پول با موفقیت شارژ شد")
                            Spacer(modifier = Modifier.height(16.dp))
                            PrimaryButton(text = "باشه", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }

                RechargeStatus.FAILED -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Rounded.Error,
                                contentDescription = null,
                                tint = MaterialTheme.extendedColors.danger,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("پرداخت ناموفق بود")
                            Spacer(modifier = Modifier.height(16.dp))
                            PrimaryButton(text = "باشه", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuickAmountGrid(selected: Long?, onSelect: (Long) -> Unit) {
    Column {
        quickAmounts.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { amount ->
                    FilterChip(
                        selected = selected == amount,
                        onClick = { onSelect(amount) },
                        label = { Text(formatToman(amount)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

private fun formatToman(amount: Long): String {
    return NumberFormat.getNumberInstance(Locale.US).format(amount) + " تومان"
}
