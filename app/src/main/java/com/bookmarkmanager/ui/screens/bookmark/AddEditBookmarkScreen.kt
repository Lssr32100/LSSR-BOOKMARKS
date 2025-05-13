package com.bookmarkmanager.ui.screens.bookmark

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bookmarkmanager.R
import com.bookmarkmanager.data.model.BookmarkType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBookmarkScreen(
    bookmarkId: Long? = null,
    onBack: () -> Unit,
    viewModel: AddEditBookmarkViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val name by viewModel.name.collectAsState()
    val url by viewModel.url.collectAsState()
    val description by viewModel.description.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val selectedSubcategoryId by viewModel.selectedSubcategoryId.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val filteredSubcategories by viewModel.filteredSubcategories.collectAsState()
    val nameError by viewModel.nameError.collectAsState()
    val categoryError by viewModel.categoryError.collectAsState()
    val subcategoryError by viewModel.subcategoryError.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Handle navigation on save
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onBack()
        }
    }
    
    // Show error message if any
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isEditMode)
                            stringResource(R.string.edit_bookmark)
                        else
                            stringResource(R.string.add_bookmark)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.cancel)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveBookmark() }) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = stringResource(R.string.save)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.updateName(it) },
                    label = { Text(stringResource(R.string.bookmark_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // URL field (optional)
                OutlinedTextField(
                    value = url,
                    onValueChange = { viewModel.updateUrl(it) },
                    label = { Text(stringResource(R.string.bookmark_url)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Description field (optional)
                OutlinedTextField(
                    value = description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = { Text(stringResource(R.string.bookmark_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Category dropdown
                CategoryDropdown(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onCategorySelected = { viewModel.updateSelectedCategory(it) },
                    isError = categoryError != null,
                    errorMessage = categoryError
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Subcategory dropdown
                SubcategoryDropdown(
                    subcategories = filteredSubcategories,
                    selectedSubcategoryId = selectedSubcategoryId,
                    onSubcategorySelected = { viewModel.updateSelectedSubcategory(it) },
                    isError = subcategoryError != null,
                    errorMessage = subcategoryError,
                    enabled = selectedCategoryId != null
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Type selection
                Text(
                    text = stringResource(R.string.bookmark_type),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BookmarkType.values().forEach { type ->
                        val label = when (type) {
                            BookmarkType.FREE -> stringResource(R.string.type_free)
                            BookmarkType.PAID -> stringResource(R.string.type_paid)
                            BookmarkType.FREEMIUM -> stringResource(R.string.type_freemium)
                        }
                        
                        FilterChip(
                            selected = type == selectedType,
                            onClick = { viewModel.updateSelectedType(type) },
                            label = { Text(label) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { viewModel.saveBookmark() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long) -> Unit,
    isError: Boolean,
    errorMessage: String?
) {
    var expanded by remember { mutableStateOf(false) }
    
    val selectedCategory = categories.find { it.id == selectedCategoryId }
    
    Column {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedCategory?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.bookmark_category)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                isError = isError
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            onCategorySelected(category.id)
                            expanded = false
                        }
                    )
                }
            }
        }
        
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubcategoryDropdown(
    subcategories: List<Subcategory>,
    selectedSubcategoryId: Long?,
    onSubcategorySelected: (Long) -> Unit,
    isError: Boolean,
    errorMessage: String?,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    
    val selectedSubcategory = subcategories.find { it.id == selectedSubcategoryId }
    
    Column {
        ExposedDropdownMenuBox(
            expanded = expanded && enabled,
            onExpandedChange = { if (enabled) expanded = it }
        ) {
            OutlinedTextField(
                value = selectedSubcategory?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.bookmark_subcategory)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                isError = isError,
                enabled = enabled
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                subcategories.forEach { subcategory ->
                    DropdownMenuItem(
                        text = { Text(subcategory.name) },
                        onClick = {
                            onSubcategorySelected(subcategory.id)
                            expanded = false
                        }
                    )
                }
            }
        }
        
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
