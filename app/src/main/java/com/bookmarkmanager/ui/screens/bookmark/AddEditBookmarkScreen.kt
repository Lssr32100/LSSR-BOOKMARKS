package com.bookmarkmanager.ui.screens.bookmark

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bookmarkmanager.R
import com.bookmarkmanager.data.model.BookmarkType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBookmarkScreen(
    viewModel: AddEditBookmarkViewModel,
    isEditing: Boolean,
    bookmarkId: Int,
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    
    // Effect to load the bookmark if we're editing
    LaunchedEffect(bookmarkId) {
        viewModel.loadBookmark(bookmarkId)
    }
    
    // Effect to navigate back when saved
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateUp()
        }
    }
    
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) {
                            stringResource(R.string.dialog_edit_bookmark)
                        } else {
                            stringResource(R.string.dialog_add_bookmark)
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Loading state
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // Form content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Error message
                    if (uiState.error != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = uiState.error ?: "",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    
                    // Name field
                    OutlinedTextField(
                        value = viewModel.bookmarkName,
                        onValueChange = { viewModel.updateBookmarkName(it) },
                        label = { Text(stringResource(R.string.label_name)) },
                        isError = viewModel.nameError != null,
                        supportingText = { 
                            if (viewModel.nameError != null) {
                                Text(viewModel.nameError ?: "")
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // URL field
                    OutlinedTextField(
                        value = viewModel.bookmarkUrl,
                        onValueChange = { viewModel.updateBookmarkUrl(it) },
                        label = { Text(stringResource(R.string.label_url)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Description field
                    OutlinedTextField(
                        value = viewModel.bookmarkDescription,
                        onValueChange = { viewModel.updateBookmarkDescription(it) },
                        label = { Text(stringResource(R.string.label_description)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Category dropdown
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        var expandedCategory by remember { mutableStateOf(false) }
                        
                        ExposedDropdownMenuBox(
                            expanded = expandedCategory,
                            onExpandedChange = { expandedCategory = it }
                        ) {
                            OutlinedTextField(
                                value = uiState.categories.find { it.id == viewModel.selectedCategoryId }?.name ?: "",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text(stringResource(R.string.label_category)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                                isError = viewModel.categoryError != null,
                                supportingText = { 
                                    if (viewModel.categoryError != null) {
                                        Text(viewModel.categoryError ?: "")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = expandedCategory,
                                onDismissRequest = { expandedCategory = false }
                            ) {
                                uiState.categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.name) },
                                        onClick = {
                                            viewModel.updateSelectedCategory(category.id)
                                            expandedCategory = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // Subcategory dropdown - only show if category is selected
                    if (viewModel.selectedCategoryId != null && uiState.subcategories.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            var expandedSubcategory by remember { mutableStateOf(false) }
                            
                            ExposedDropdownMenuBox(
                                expanded = expandedSubcategory,
                                onExpandedChange = { expandedSubcategory = it }
                            ) {
                                OutlinedTextField(
                                    value = uiState.subcategories.find { it.id == viewModel.selectedSubcategoryId }?.name ?: "",
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text(stringResource(R.string.label_subcategory)) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSubcategory) },
                                    isError = viewModel.subcategoryError != null,
                                    supportingText = { 
                                        if (viewModel.subcategoryError != null) {
                                            Text(viewModel.subcategoryError ?: "")
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = expandedSubcategory,
                                    onDismissRequest = { expandedSubcategory = false }
                                ) {
                                    uiState.subcategories.forEach { subcategory ->
                                        DropdownMenuItem(
                                            text = { Text(subcategory.name) },
                                            onClick = {
                                                viewModel.updateSelectedSubcategory(subcategory.id)
                                                expandedSubcategory = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Bookmark type
                    Text(
                        text = stringResource(R.string.label_type),
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BookmarkTypeOption(
                            isSelected = viewModel.selectedBookmarkType == BookmarkType.FREE,
                            type = BookmarkType.FREE,
                            label = stringResource(R.string.type_free),
                            onClick = { viewModel.updateSelectedBookmarkType(BookmarkType.FREE) },
                            modifier = Modifier.weight(1f)
                        )
                        
                        BookmarkTypeOption(
                            isSelected = viewModel.selectedBookmarkType == BookmarkType.PAID,
                            type = BookmarkType.PAID,
                            label = stringResource(R.string.type_paid),
                            onClick = { viewModel.updateSelectedBookmarkType(BookmarkType.PAID) },
                            modifier = Modifier.weight(1f)
                        )
                        
                        BookmarkTypeOption(
                            isSelected = viewModel.selectedBookmarkType == BookmarkType.FREEMIUM,
                            type = BookmarkType.FREEMIUM,
                            label = stringResource(R.string.type_freemium),
                            onClick = { viewModel.updateSelectedBookmarkType(BookmarkType.FREEMIUM) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Save button
                    Button(
                        onClick = { 
                            scope.launch {
                                viewModel.saveBookmark(bookmarkId)
                            }
                        },
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(stringResource(R.string.btn_save))
                    }
                }
            }
        }
    }
}

@Composable
fun BookmarkTypeOption(
    isSelected: Boolean,
    type: BookmarkType,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, contentColor) = when (type) {
        BookmarkType.FREE -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        BookmarkType.PAID -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        BookmarkType.FREEMIUM -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
    }
    
    val alpha = if (isSelected) 1f else 0.6f
    
    Card(
        modifier = modifier.height(56.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = alpha)
        ),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder
        } else {
            null
        },
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}