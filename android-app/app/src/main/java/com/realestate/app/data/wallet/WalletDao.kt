package com.realestate.app.data.wallet

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet_transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<WalletTransaction>>

    @Insert
    suspend fun insert(transaction: WalletTransaction): Long

    @Update
    suspend fun update(transaction: WalletTransaction)

    @Query("DELETE FROM wallet_transactions")
    suspend fun deleteAll()
}
