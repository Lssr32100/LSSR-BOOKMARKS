package com.bookmarkmanager.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Subcategory::class,
            parentColumns = ["id"],
            childColumns = ["subcategory_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("category_id"),
        Index("subcategory_id")
    ]
)
data class Bookmark(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val url: String? = null,
    val description: String? = null,
    val category_id: Int,
    val subcategory_id: Int,
    val type: BookmarkType,
    val created_at: Date = Date(),
    val updated_at: Date = Date()
)