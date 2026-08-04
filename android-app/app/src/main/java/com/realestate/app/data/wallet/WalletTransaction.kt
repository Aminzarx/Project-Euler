package com.realestate.app.data.wallet

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType { RECHARGE, PURCHASE }

enum class TransactionStatus { PENDING, SUCCESS, FAILED }

@Entity(tableName = "wallet_transactions")
data class WalletTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Long,
    val type: TransactionType,
    val status: TransactionStatus,
    val description: String,
    val referenceId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
