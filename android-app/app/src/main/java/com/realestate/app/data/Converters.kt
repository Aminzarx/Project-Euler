package com.realestate.app.data

import androidx.room.TypeConverter
import com.realestate.app.data.property.TimelineEventType
import com.realestate.app.data.wallet.TransactionStatus
import com.realestate.app.data.wallet.TransactionType

class Converters {
    @TypeConverter
    fun fromDealType(value: DealType): String = value.name

    @TypeConverter
    fun toDealType(value: String): DealType = DealType.valueOf(value)

    @TypeConverter
    fun fromPropertyType(value: PropertyType): String = value.name

    @TypeConverter
    fun toPropertyType(value: String): PropertyType = PropertyType.valueOf(value)

    @TypeConverter
    fun fromPropertyStatus(value: PropertyStatus): String = value.name

    @TypeConverter
    fun toPropertyStatus(value: String): PropertyStatus = PropertyStatus.valueOf(value)

    @TypeConverter
    fun fromTagList(tags: List<String>): String = tags.joinToString(TAG_DELIMITER)

    @TypeConverter
    fun toTagList(value: String): List<String> =
        if (value.isBlank()) emptyList() else value.split(TAG_DELIMITER)

    @TypeConverter
    fun fromTimelineEventType(value: TimelineEventType): String = value.name

    @TypeConverter
    fun toTimelineEventType(value: String): TimelineEventType = TimelineEventType.valueOf(value)

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromTransactionStatus(value: TransactionStatus): String = value.name

    @TypeConverter
    fun toTransactionStatus(value: String): TransactionStatus = TransactionStatus.valueOf(value)

    companion object {
        private const val TAG_DELIMITER = "|||"
    }
}
