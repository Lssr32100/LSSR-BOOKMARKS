package com.bookmarkmanager.data.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.bookmarkmanager.data.model.Bookmark
import com.bookmarkmanager.data.model.Subcategory

data class SubcategoryWithBookmarks(
    @Embedded val subcategory: Subcategory,
    @Relation(
        parentColumn = "id",
        entityColumn = "subcategory_id"
    )
    val bookmarks: List<Bookmark>
)