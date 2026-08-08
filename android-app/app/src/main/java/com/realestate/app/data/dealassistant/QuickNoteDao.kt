package com.realestate.app.data.dealassistant

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuickNoteDao {
    @Query("SELECT * FROM quick_notes ORDER BY createdAt DESC")
    fun getAll(): Flow<List<QuickNote>>

    @Insert
    suspend fun insert(note: QuickNote): Long

    @Delete
    suspend fun delete(note: QuickNote)
}
