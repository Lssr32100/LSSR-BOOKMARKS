package com.bookmarkmanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Category model representing a category for bookmarks.
 * Each category has a unique ID, name and color for visual identification.
 * The color is stored as a Long representing the ARGB color value.
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val color: Long = DEFAULT_COLOR
) {
    companion object {
        // Default color - Material Blue
        const val DEFAULT_COLOR: Long = 0xFF2196F3
        
        // Predefined color palette for categories
        val COLOR_PALETTE = listOf(
            0xFF2196F3, // Blue
            0xFF4CAF50, // Green
            0xFFF44336, // Red
            0xFF9C27B0, // Purple
            0xFFFF9800, // Orange
            0xFF00BCD4, // Cyan
            0xFF795548, // Brown
            0xFF607D8B, // Blue Grey
            0xFFFFC107, // Amber
            0xFFE91E63, // Pink
            0xFF673AB7, // Deep Purple
            0xFF009688, // Teal
            0xFFCDDC39, // Lime
            0xFF3F51B5  // Indigo
        )
        
        /**
         * Get a suggested color based on an index.
         * This helps to assign different colors to categories when creating them.
         */
        fun getSuggestedColor(index: Int): Long {
            return COLOR_PALETTE[index % COLOR_PALETTE.size]
        }
    }
}