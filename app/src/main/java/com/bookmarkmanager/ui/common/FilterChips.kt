package com.bookmarkmanager.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bookmarkmanager.R
import com.bookmarkmanager.data.model.BookmarkType
import com.bookmarkmanager.data.model.Category
import com.bookmarkmanager.ui.screens.home.FilterOption
import com.bookmarkmanager.ui.theme.FreeColor
import com.bookmarkmanager.ui.theme.FreemiumColor
import com.bookmarkmanager.ui.theme.PaidColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChips(
    categories: List<Category>,
    selectedFilter: FilterOption,
    onFilterSelected: (FilterOption) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // All filter
        item {
            FilterChip(
                selected = selectedFilter is FilterOption.All,
                onClick = { onFilterSelected(FilterOption.All) },
                label = { Text(stringResource(R.string.filter_all)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
        
        // Type filters
        items(BookmarkType.values()) { type ->
            val (label, color) = when (type) {
                BookmarkType.FREE -> Pair(stringResource(R.string.type_free), FreeColor)
                BookmarkType.PAID -> Pair(stringResource(R.string.type_paid), PaidColor)
                BookmarkType.FREEMIUM -> Pair(stringResource(R.string.type_freemium), FreemiumColor)
            }
            
            FilterChip(
                selected = selectedFilter is FilterOption.ByType && selectedFilter.type == type,
                onClick = { onFilterSelected(FilterOption.ByType(type)) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color.copy(alpha = 0.2f),
                    selectedLabelColor = color
                )
            )
        }
        
        // Category filters
        items(categories) { category ->
            FilterChip(
                selected = selectedFilter is FilterOption.ByCategory && selectedFilter.categoryId == category.id,
                onClick = { onFilterSelected(FilterOption.ByCategory(category.id)) },
                label = { Text(category.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            )
        }
    }
}
