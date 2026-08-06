package com.realestate.app.data.property

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TimelineEventType { CREATED, EDITED, PRICE_CHANGED, SHARED, ARCHIVED, RESTORED, NOTE_ADDED, FOLLOW_UP_SET }

@Entity(
    tableName = "timeline_events",
    indices = [Index(value = ["propertyId"]), Index(value = ["createdAt"])]
)
data class TimelineEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val propertyId: Long,
    val type: TimelineEventType,
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)
