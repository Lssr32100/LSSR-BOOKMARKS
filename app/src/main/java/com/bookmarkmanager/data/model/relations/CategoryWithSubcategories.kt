package com.bookmarkmanager.data.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.bookmarkmanager.data.model.Category
import com.bookmarkmanager.data.model.Subcategory

data class CategoryWithSubcategories(
    @Embedded val category: Category,
    @Relation(
        parentColumn = "id",
        entityColumn = "category_id"
    )
    val subcategories: List<Subcategory>
)