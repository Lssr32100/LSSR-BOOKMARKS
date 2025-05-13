package com.bookmarkmanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bookmarkmanager.data.local.dao.BookmarkDao
import com.bookmarkmanager.data.local.dao.CategoryDao
import com.bookmarkmanager.data.local.dao.SubcategoryDao
import com.bookmarkmanager.data.model.Bookmark
import com.bookmarkmanager.data.model.Category
import com.bookmarkmanager.data.model.Subcategory

@Database(
    entities = [Bookmark::class, Category::class, Subcategory::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BookmarkDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun categoryDao(): CategoryDao
    abstract fun subcategoryDao(): SubcategoryDao
}

import androidx.room.TypeConverter
import com.bookmarkmanager.data.model.BookmarkType

class Converters {
    @TypeConverter
    fun fromBookmarkType(type: BookmarkType): String {
        return type.name
    }

    @TypeConverter
    fun toBookmarkType(value: String): BookmarkType {
        return BookmarkType.valueOf(value)
    }
}
