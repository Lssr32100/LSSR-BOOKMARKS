package com.bookmarkmanager.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bookmarkmanager.R
import com.bookmarkmanager.ui.screens.home.SortOption

@Composable
fun SortingOptions(
    selectedSortOption: SortOption,
    onSortOptionSelected: (SortOption) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp)
        ) {
            Text(
                text = stringResource(R.string.sort_by),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(8.dp)
            )
            
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            
            SortItem(
                title = stringResource(R.string.sort_name_asc),
                selected = selectedSortOption == SortOption.NAME_ASC,
                onClick = { onSortOptionSelected(SortOption.NAME_ASC) }
            )
            
            SortItem(
                title = stringResource(R.string.sort_name_desc),
                selected = selectedSortOption == SortOption.NAME_DESC,
                onClick = { onSortOptionSelected(SortOption.NAME_DESC) }
            )
            
            SortItem(
                title = stringResource(R.string.sort_category),
                selected = selectedSortOption == SortOption.CATEGORY,
                onClick = { onSortOptionSelected(SortOption.CATEGORY) }
            )
            
            SortItem(
                title = stringResource(R.string.sort_subcategory),
                selected = selectedSortOption == SortOption.SUBCATEGORY,
                onClick = { onSortOptionSelected(SortOption.SUBCATEGORY) }
            )
            
            SortItem(
                title = stringResource(R.string.sort_type),
                selected = selectedSortOption == SortOption.TYPE,
                onClick = { onSortOptionSelected(SortOption.TYPE) }
            )
        }
    }
}

@Composable
fun SortItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick
            )
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
        } else {
            Spacer(modifier = Modifier.width(40.dp))
        }
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
