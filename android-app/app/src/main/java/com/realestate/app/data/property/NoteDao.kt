package com.realestate.app.data.property

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE propertyId = :propertyId ORDER BY createdAt DESC")
    fun getNotesForProperty(propertyId: Long): Flow<List<Note>>

    @Query("SELECT * FROM notes")
    fun getAllNotes(): Flow<List<Note>>

    @Insert
    suspend fun insert(note: Note): Long

    @Insert
    suspend fun insertAll(notes: List<Note>)

    @Update
    suspend fun update(note: Note)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteById(noteId: Long)

    @Query("DELETE FROM notes WHERE propertyId = :propertyId")
    suspend fun deleteForProperty(propertyId: Long)

    @Query("DELETE FROM notes")
    suspend fun deleteAll()
}
