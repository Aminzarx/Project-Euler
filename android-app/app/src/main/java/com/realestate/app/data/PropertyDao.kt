package com.realestate.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyDao {
    @Query("SELECT * FROM properties ORDER BY dateAdded DESC")
    fun getAllProperties(): Flow<List<Property>>

    @Query("SELECT * FROM properties WHERE isFavorite = 1 ORDER BY dateAdded DESC")
    fun getFavoriteProperties(): Flow<List<Property>>

    @Query("SELECT * FROM properties WHERE lastViewedAt IS NOT NULL ORDER BY lastViewedAt DESC LIMIT 5")
    fun getRecentlyViewedProperties(): Flow<List<Property>>

    @Query("SELECT * FROM properties WHERE id = :id")
    fun getPropertyById(id: Long): Flow<Property?>

    @Insert
    suspend fun insert(property: Property): Long

    @Insert
    suspend fun insertAll(properties: List<Property>)

    @Update
    suspend fun update(property: Property)

    @Delete
    suspend fun delete(property: Property)

    /** One transaction for the whole multi-select batch. Deleting them one at a time made Room
     *  open N transactions and fire N invalidation notifications, so every observing screen
     *  recomputed once per deleted row instead of once for the batch. */
    @Delete
    suspend fun deleteProperties(properties: List<Property>)

    @Query("DELETE FROM properties")
    suspend fun deleteAll()
}
