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

    @TypeConverter
    fun fromCaseType(value: CaseType): String = value.name

    @TypeConverter
    fun toCaseType(value: String): CaseType = CaseType.valueOf(value)

    @TypeConverter
    fun fromCaseTransactionType(value: CaseTransactionType?): String? = value?.name

    @TypeConverter
    fun toCaseTransactionType(value: String?): CaseTransactionType? = value?.let { CaseTransactionType.valueOf(it) }

    @TypeConverter
    fun fromCaseFlagList(flags: List<CaseFlag>): String = flags.joinToString(TAG_DELIMITER) { it.name }

    @TypeConverter
    fun toCaseFlagList(value: String): List<CaseFlag> =
        if (value.isBlank()) emptyList() else value.split(TAG_DELIMITER).map { CaseFlag.valueOf(it) }

    @TypeConverter
    fun fromMortgageStatus(value: MortgageStatus): String = value.name

    @TypeConverter
    fun toMortgageStatus(value: String): MortgageStatus = MortgageStatus.valueOf(value)

    @TypeConverter
    fun fromCasePriority(value: CasePriority): String = value.name

    @TypeConverter
    fun toCasePriority(value: String): CasePriority = CasePriority.valueOf(value)

    @TypeConverter
    fun fromRequestValidityType(value: RequestValidityType): String = value.name

    @TypeConverter
    fun toRequestValidityType(value: String): RequestValidityType = RequestValidityType.valueOf(value)

    companion object {
        private const val TAG_DELIMITER = "|||"
    }
}
