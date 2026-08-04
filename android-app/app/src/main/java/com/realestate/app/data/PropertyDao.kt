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

    @Update
    suspend fun update(property: Property)

    @Delete
    suspend fun delete(property: Property)

    @Query("DELETE FROM properties")
    suspend fun deleteAll()
}
