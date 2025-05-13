package com.bookmarkmanager.data.local

import androidx.room.TypeConverter
import com.bookmarkmanager.data.model.BookmarkType
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun fromBookmarkType(value: BookmarkType): String {
        return value.name
    }
    
    @TypeConverter
    fun toBookmarkType(value: String): BookmarkType {
        return try {
            BookmarkType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            BookmarkType.FREE // Default value
        }
    }
}