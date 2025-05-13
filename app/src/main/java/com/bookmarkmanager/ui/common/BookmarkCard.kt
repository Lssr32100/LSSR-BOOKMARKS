package com.bookmarkmanager.ui.common

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Chip
import androidx.compose.material3.ChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bookmarkmanager.R
import com.bookmarkmanager.data.model.Bookmark
import com.bookmarkmanager.data.model.BookmarkType
import com.bookmarkmanager.ui.theme.FreeColor
import com.bookmarkmanager.ui.theme.FreemiumColor
import com.bookmarkmanager.ui.theme.PaidColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkCard(
    bookmark: Bookmark,
    categoryName: String,
    subcategoryName: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with name and actions
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bookmark.name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                
                // Type chip
                BookmarkTypeChip(type = bookmark.type)
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Edit button
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Delete button
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Category and subcategory
            Row(
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "$categoryName > $subcategoryName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // URL (if available)
            bookmark.url?.let { url ->
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    TextButton(
                        onClick = {
                            try {
                                uriHandler.openUri(url)
                            } catch (e: Exception) {
                                // Handle error opening URL
                            }
                        }
                    ) {
                        Text(
                            text = url,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            // Expand/collapse button if there's a description
            if (!bookmark.description.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (expanded) stringResource(R.string.clear) else stringResource(R.string.bookmark_description),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                }
                
                // Description (if expanded)
                if (expanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = bookmark.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_bookmark_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    }
                ) {
                    Text(
                        text = stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun BookmarkTypeChip(type: BookmarkType) {
    val (text, color) = when (type) {
        BookmarkType.FREE -> Pair(stringResource(R.string.type_free), FreeColor)
        BookmarkType.PAID -> Pair(stringResource(R.string.type_paid), PaidColor)
        BookmarkType.FREEMIUM -> Pair(stringResource(R.string.type_freemium), FreemiumColor)
    }
    
    Chip(
        onClick = { /* Read-only */ },
        colors = ChipDefaults.chipColors(
            containerColor = color.copy(alpha = 0.1f),
            labelColor = color
        ),
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall
            )
        }
    )
}
