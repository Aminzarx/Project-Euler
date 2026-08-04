package com.realestate.app.data.property

import kotlinx.coroutines.flow.Flow

/** Notes and timeline are keyed by propertyId, so one repository covers both concerns. */
class PropertyExtrasRepository(
    private val noteDao: NoteDao,
    private val timelineDao: TimelineDao
) {
    fun getNotesForProperty(propertyId: Long): Flow<List<Note>> = noteDao.getNotesForProperty(propertyId)

    fun getTimelineForProperty(propertyId: Long): Flow<List<TimelineEvent>> =
        timelineDao.getEventsForProperty(propertyId)

    suspend fun addNote(propertyId: Long, content: String): Long = noteDao.insert(
        Note(propertyId = propertyId, content = content)
    )

    suspend fun logEvent(propertyId: Long, type: TimelineEventType, description: String) {
        timelineDao.insert(TimelineEvent(propertyId = propertyId, type = type, description = description))
    }
}
