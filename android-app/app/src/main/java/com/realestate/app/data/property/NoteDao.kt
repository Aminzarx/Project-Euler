package com.realestate.app.data.property

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE propertyId = :propertyId ORDER BY createdAt DESC")
    fun getNotesForProperty(propertyId: Long): Flow<List<Note>>

    @Query("SELECT * FROM notes")
    fun getAllNotes(): Flow<List<Note>>

    @Insert
    suspend fun insert(note: Note): Long

    @Query("DELETE FROM notes")
    suspend fun deleteAll()
}
