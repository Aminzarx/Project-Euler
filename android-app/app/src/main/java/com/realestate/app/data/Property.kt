package com.realestate.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DealType { SALE, RENT }

enum class PropertyType { APARTMENT, VILLA, LAND, OFFICE, SHOP }

enum class PropertyStatus { NEW, READY, ACTIVE, NEGOTIATING, RESERVED, SOLD, RENTED, ARCHIVED }

@Entity(tableName = "properties")
data class Property(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val price: Long,
    val area: Double,
    val rooms: Int,
    val city: String,
    val address: String,
    val ownerName: String = "",
    val ownerPhone: String,
    val dealType: DealType,
    val propertyType: PropertyType,
    val status: PropertyStatus = PropertyStatus.NEW,
    val tags: List<String> = emptyList(),
    val imageUri: String? = null,
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val favoriteFolder: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val lastModifiedAt: Long = System.currentTimeMillis(),
    val lastViewedAt: Long? = null,
    val lastSharedAt: Long? = null,
    val viewCount: Int = 0,
    val followUpAt: Long? = null
)

/** Derived, human-readable identifier - never stored, always computed from [Property.id]. */
val Property.code: String
    get() = "PR-%04d".format(id)
