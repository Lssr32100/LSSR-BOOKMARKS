package com.bookmarkmanager.ui.common

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bookmarkmanager.data.model.BookmarkType
import com.bookmarkmanager.data.model.Category
import com.bookmarkmanager.ui.theme.TypeFree
import com.bookmarkmanager.ui.theme.TypeFreemium
import com.bookmarkmanager.ui.theme.TypePaid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypeFilterChips(
    selectedType: BookmarkType?,
    onTypeSelected: (BookmarkType?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // All types chip
        ElevatedFilterChip(
            selected = selectedType == null,
            onClick = { onTypeSelected(null) },
            label = { Text("All") }
        )
        
        // Free chip
        ElevatedFilterChip(
            selected = selectedType == BookmarkType.FREE,
            onClick = { onTypeSelected(BookmarkType.FREE) },
            label = { Text("Free") },
            colors = FilterChipDefaults.elevatedFilterChipColors(
                selectedContainerColor = TypeFree.copy(alpha = 0.2f),
                selectedLabelColor = TypeFree
            )
        )
        
        // Paid chip
        ElevatedFilterChip(
            selected = selectedType == BookmarkType.PAID,
            onClick = { onTypeSelected(BookmarkType.PAID) },
            label = { Text("Paid") },
            colors = FilterChipDefaults.elevatedFilterChipColors(
                selectedContainerColor = TypePaid.copy(alpha = 0.2f),
                selectedLabelColor = TypePaid
            )
        )
        
        // Freemium chip
        ElevatedFilterChip(
            selected = selectedType == BookmarkType.FREEMIUM,
            onClick = { onTypeSelected(BookmarkType.FREEMIUM) },
            label = { Text("Freemium") },
            colors = FilterChipDefaults.elevatedFilterChipColors(
                selectedContainerColor = TypeFreemium.copy(alpha = 0.2f),
                selectedLabelColor = TypeFreemium
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterChips(
    categories: List<Category>,
    selectedCategoryId: Int?,
    onCategorySelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // All categories chip
        ElevatedFilterChip(
            selected = selectedCategoryId == null,
            onClick = { onCategorySelected(null) },
            label = { Text("All") }
        )
        
        // Individual category chips with color indicators
        categories.forEach { category ->
            CategoryChip(
                category = category,
                selected = selectedCategoryId == category.id,
                onClick = { onCategorySelected(category.id) }
            )
        }
    }
}