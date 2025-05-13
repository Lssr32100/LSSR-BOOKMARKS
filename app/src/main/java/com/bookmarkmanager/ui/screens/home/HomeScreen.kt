package com.bookmarkmanager.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bookmarkmanager.R
import com.bookmarkmanager.data.model.BookmarkType
import com.bookmarkmanager.ui.common.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToAddBookmark: () -> Unit,
    onNavigateToEditBookmark: (Int) -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var bookmarkToDelete by remember { mutableStateOf<BookmarkWithDetails?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_my_bookmarks)) },
                actions = {
                    IconButton(onClick = onNavigateToCategories) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = stringResource(R.string.nav_categories)
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.nav_settings)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddBookmark,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.btn_add_bookmark)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            SearchBar(
                query = viewModel.searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) }
            )
            
            // Type filters
            TypeFilterChips(
                selectedType = viewModel.selectedBookmarkType,
                onTypeSelected = { viewModel.updateSelectedBookmarkType(it) }
            )
            
            // Category filters (scrollable row)
            if (uiState.categories.isNotEmpty()) {
                CategoryFilterChips(
                    categories = uiState.categories,
                    selectedCategoryId = viewModel.selectedCategoryId,
                    onCategorySelected = { viewModel.updateSelectedCategory(it) }
                )
            }
            
            // Sorting options
            SortingOptions(
                selectedOption = viewModel.sortOption,
                onOptionSelected = { viewModel.updateSortOption(it) }
            )
            
            // Bookmark list
            Box(modifier = Modifier.weight(1f)) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (uiState.bookmarks.isEmpty()) {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_bookmarks_found),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateToAddBookmark) {
                            Text(stringResource(R.string.btn_add_bookmark))
                        }
                    }
                } else {
                    // Bookmark list
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(uiState.bookmarks) { bookmarkWithDetails ->
                            val bookmark = bookmarkWithDetails.bookmark
                            BookmarkCard(
                                name = bookmark.name,
                                url = bookmark.url,
                                description = bookmark.description,
                                categoryName = bookmarkWithDetails.categoryName,
                                subcategoryName = bookmarkWithDetails.subcategoryName,
                                bookmarkType = bookmark.bookmarkType,
                                onEdit = { onNavigateToEditBookmark(bookmark.id) },
                                onDelete = {
                                    bookmarkToDelete = bookmarkWithDetails
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog && bookmarkToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                bookmarkToDelete = null
            },
            title = { Text(stringResource(R.string.dialog_confirm_delete)) },
            text = { Text(stringResource(R.string.dialog_delete_bookmark_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        bookmarkToDelete?.let {
                            scope.launch {
                                viewModel.deleteBookmark(it.bookmark.id)
                                showDeleteDialog = false
                                bookmarkToDelete = null
                            }
                        }
                    }
                ) {
                    Text(
                        stringResource(R.string.btn_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        bookmarkToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}