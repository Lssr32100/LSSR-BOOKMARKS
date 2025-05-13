package com.bookmarkmanager.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subcategories",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("category_id")]
)
data class Subcategory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val category_id: Int
)