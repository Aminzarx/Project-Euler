package com.realestate.app.data.property

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TimelineDao {
    @Query("SELECT * FROM timeline_events WHERE propertyId = :propertyId ORDER BY createdAt DESC")
    fun getEventsForProperty(propertyId: Long): Flow<List<TimelineEvent>>

    @Query("SELECT * FROM timeline_events")
    fun getAllEvents(): Flow<List<TimelineEvent>>

    @Insert
    suspend fun insert(event: TimelineEvent): Long

    @Query("DELETE FROM timeline_events")
    suspend fun deleteAll()
}
