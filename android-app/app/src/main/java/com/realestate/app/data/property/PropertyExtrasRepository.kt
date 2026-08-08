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

    fun getRecentEvents(limit: Int): Flow<List<TimelineEvent>> = timelineDao.getRecentEvents(limit)

    suspend fun addNote(propertyId: Long, content: String): Long = noteDao.insert(
        Note(propertyId = propertyId, content = content)
    )

    suspend fun updateNote(note: Note) = noteDao.update(note)

    suspend fun deleteNote(noteId: Long) = noteDao.deleteById(noteId)

    /** Re-inserts a just-deleted note as-is (same id/content/timestamp) — used for undo. */
    suspend fun restoreNote(note: Note): Long = noteDao.insert(note)

    /** Called when a property itself is deleted, so its notes/timeline never become orphaned rows. */
    suspend fun deleteAllForProperty(propertyId: Long) {
        noteDao.deleteForProperty(propertyId)
        timelineDao.deleteForProperty(propertyId)
    }

    suspend fun logEvent(propertyId: Long, type: TimelineEventType, description: String) {
        timelineDao.insert(TimelineEvent(propertyId = propertyId, type = type, description = description))
    }
}
