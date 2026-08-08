package com.realestate.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.realestate.app.data.AppDatabase
import com.realestate.app.data.wallet.MockPaymentGateway
import com.realestate.app.data.wallet.PaymentGateway
import com.realestate.app.data.wallet.PaymentResult
import com.realestate.app.data.wallet.TransactionStatus
import com.realestate.app.data.wallet.TransactionType
import com.realestate.app.data.wallet.WalletRepository
import com.realestate.app.data.wallet.WalletTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class RechargeStatus { IDLE, PROCESSING, SUCCESS, FAILED }

class WalletViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WalletRepository(AppDatabase.getInstance(application).walletDao())
    private val paymentGateway: PaymentGateway = MockPaymentGateway()

    val transactions: StateFlow<List<WalletTransaction>> = repository.transactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val balance: StateFlow<Long> = transactions.map { list ->
        list.filter { it.status == TransactionStatus.SUCCESS }
            .sumOf { if (it.type == TransactionType.RECHARGE) it.amount else -it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val _rechargeStatus = MutableStateFlow(RechargeStatus.IDLE)
    val rechargeStatus: StateFlow<RechargeStatus> = _rechargeStatus

    fun recharge(amount: Long) {
        viewModelScope.launch {
            _rechargeStatus.value = RechargeStatus.PROCESSING
            val pendingId = repository.insert(
                WalletTransaction(
                    amount = amount,
                    type = TransactionType.RECHARGE,
                    status = TransactionStatus.PENDING,
                    description = "شارژ کیف پول"
                )
            )
            when (val result = paymentGateway.charge(amount)) {
                is PaymentResult.Success -> {
                    repository.update(
                        WalletTransaction(
                            id = pendingId,
                            amount = amount,
                            type = TransactionType.RECHARGE,
                            status = TransactionStatus.SUCCESS,
                            description = "شارژ کیف پول",
                            referenceId = result.referenceId
                        )
                    )
                    _rechargeStatus.value = RechargeStatus.SUCCESS
                }

                is PaymentResult.Failure -> {
                    repository.update(
                        WalletTransaction(
                            id = pendingId,
                            amount = amount,
                            type = TransactionType.RECHARGE,
                            status = TransactionStatus.FAILED,
                            description = result.message
                        )
                    )
                    _rechargeStatus.value = RechargeStatus.FAILED
                }
            }
        }
    }

    fun resetRechargeStatus() {
        _rechargeStatus.value = RechargeStatus.IDLE
    }
}
