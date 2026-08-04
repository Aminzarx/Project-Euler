package com.realestate.app.data.wallet

import kotlinx.coroutines.flow.Flow

class WalletRepository(private val dao: WalletDao) {
    val transactions: Flow<List<WalletTransaction>> = dao.getAllTransactions()

    suspend fun insert(transaction: WalletTransaction): Long = dao.insert(transaction)

    suspend fun update(transaction: WalletTransaction) = dao.update(transaction)
}
