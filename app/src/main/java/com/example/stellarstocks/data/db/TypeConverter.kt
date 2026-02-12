package com.example.stellarstocks.data.db

import androidx.room.TypeConverter
import java.util.Date

class Converters { // Converts dates into long type which can be stored in room
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? { // converts long to date
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? { // converts date to long
        return date?.time
    }
}