package com.bookmarkmanager.data.model

enum class BookmarkType {
    FREE,
    PAID,
    FREEMIUM;
    
    companion object {
        fun fromString(value: String): BookmarkType {
            return when (value.uppercase()) {
                "FREE" -> FREE
                "PAID" -> PAID
                "FREEMIUM" -> FREEMIUM
                else -> FREE // Default
            }
        }
    }
}
