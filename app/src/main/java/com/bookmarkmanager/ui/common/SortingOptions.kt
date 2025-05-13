package com.bookmarkmanager.ui.common

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bookmarkmanager.R

enum class SortOption(val displayNameRes: Int) {
    NAME_ASC(R.string.sort_name_az),
    NAME_DESC(R.string.sort_name_za),
    CATEGORY(R.string.sort_category),
    TYPE(R.string.sort_type)
}

@Composable
fun SortingOptions(
    selectedOption: SortOption,
    onOptionSelected: (SortOption) -> Unit,
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
        SortOption.values().forEach { option ->
            val selected = option == selectedOption
            OutlinedButton(
                onClick = { onOptionSelected(option) },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                    contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                ),
                border = if (selected) 
                    ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp)
                else 
                    ButtonDefaults.outlinedButtonBorder
            ) {
                Text(stringResource(option.displayNameRes))
            }
        }
    }
}