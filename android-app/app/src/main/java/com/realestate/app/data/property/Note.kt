package com.realestate.app.data.property

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "notes", indices = [Index(value = ["propertyId"])])
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val propertyId: Long,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)
