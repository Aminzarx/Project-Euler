package com.realestate.app.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromDealType(value: DealType): String = value.name

    @TypeConverter
    fun toDealType(value: String): DealType = DealType.valueOf(value)

    @TypeConverter
    fun fromPropertyType(value: PropertyType): String = value.name

    @TypeConverter
    fun toPropertyType(value: String): PropertyType = PropertyType.valueOf(value)
}
