package com.bookmarkmanager.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bookmarkmanager.R
import com.bookmarkmanager.data.model.Bookmark
import com.bookmarkmanager.ui.common.BookmarkCard
import com.bookmarkmanager.ui.common.FilterChips
import com.bookmarkmanager.ui.common.SearchBar
import com.bookmarkmanager.ui.common.SortingOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddBookmark: () -> Unit,
    onEditBookmark: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val bookmarksState by viewModel.bookmarksState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val filterOption by viewModel.filterOption.collectAsState()
    
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    
    var showSortingOptions by remember { mutableStateOf(false) }
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddBookmark,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add_bookmark)) },
                expanded = scrollBehavior.state.collapsedFraction < 0.5f
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search and sort/filter
            SearchBar(
                value = searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                onSortClick = { showSortingOptions = !showSortingOptions }
            )
            
            AnimatedVisibility(
                visible = showSortingOptions,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                SortingOptions(
                    selectedSortOption = sortOption,
                    onSortOptionSelected = {
                        viewModel.updateSortOption(it)
                        showSortingOptions = false
                    }
                )
            }
            
            // Filter chips
            FilterChips(
                categories = bookmarksState.categories,
                selectedFilter = filterOption,
                onFilterSelected = viewModel::updateFilterOption
            )
            
            // Bookmarks list
            if (bookmarksState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (bookmarksState.bookmarks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_bookmarks),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = bookmarksState.bookmarks,
                        key = { it.id }
                    ) { bookmark ->
                        BookmarkCard(
                            bookmark = bookmark,
                            categoryName = viewModel.getCategoryName(bookmark.categoryId),
                            subcategoryName = viewModel.getSubcategoryName(bookmark.subcategoryId),
                            onEditClick = { onEditBookmark(bookmark.id) },
                            onDeleteClick = { viewModel.deleteBookmark(bookmark) }
                        )
                    }
                    
                    // Add bottom space for FAB
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}
