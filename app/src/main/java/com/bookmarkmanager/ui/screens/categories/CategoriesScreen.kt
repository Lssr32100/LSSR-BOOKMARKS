package com.bookmarkmanager.ui.screens.categories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bookmarkmanager.R
import com.bookmarkmanager.data.model.Category
import com.bookmarkmanager.data.model.Subcategory
import com.bookmarkmanager.data.model.relations.CategoryWithSubcategories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onAddCategory: () -> Unit,
    onEditCategory: (Long) -> Unit,
    onAddSubcategory: (Long) -> Unit,
    onEditSubcategory: (Long, Long) -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val categoriesWithSubcategories by viewModel.categoriesWithSubcategories.collectAsState()
    val state by viewModel.state.collectAsState()
    
    // Add Category Dialog state
    val showAddCategoryDialog by viewModel.showAddCategoryDialog.collectAsState()
    val newCategoryName by viewModel.newCategoryName.collectAsState()
    val categoryNameError by viewModel.categoryNameError.collectAsState()
    
    // Add Subcategory Dialog state
    val showAddSubcategoryDialog by viewModel.showAddSubcategoryDialog.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val newSubcategoryName by viewModel.newSubcategoryName.collectAsState()
    val subcategoryNameError by viewModel.subcategoryNameError.collectAsState()
    
    // Edit Category Dialog state
    val showEditCategoryDialog by viewModel.showEditCategoryDialog.collectAsState()
    val categoryToEdit by viewModel.categoryToEdit.collectAsState()
    val editCategoryName by viewModel.editCategoryName.collectAsState()
    
    // Edit Subcategory Dialog state
    val showEditSubcategoryDialog by viewModel.showEditSubcategoryDialog.collectAsState()
    val subcategoryToEdit by viewModel.subcategoryToEdit.collectAsState()
    val editSubcategoryName by viewModel.editSubcategoryName.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.categories)) }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showAddCategoryDialog() },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add_category)) },
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (categoriesWithSubcategories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_categories),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(categoriesWithSubcategories) { categoryWithSubcategories ->
                    CategoryItem(
                        categoryWithSubcategories = categoryWithSubcategories,
                        onEditCategory = { viewModel.showEditCategoryDialog(categoryWithSubcategories.category) },
                        onAddSubcategory = { viewModel.showAddSubcategoryDialog(categoryWithSubcategories.category.id) },
                        onEditSubcategory = { subcategory -> viewModel.showEditSubcategoryDialog(subcategory) }
                    )
                }
                
                // Add bottom space for FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
    
    // Add Category Dialog
    if (showAddCategoryDialog) {
        CategoryDialog(
            title = stringResource(R.string.add_category),
            name = newCategoryName,
            onNameChange = { viewModel.updateNewCategoryName(it) },
            nameError = categoryNameError,
            onConfirm = { viewModel.addCategory() },
            onDismiss = { viewModel.hideAddCategoryDialog() },
            showDeleteButton = false,
            onDelete = {}
        )
    }
    
    // Edit Category Dialog
    if (showEditCategoryDialog && categoryToEdit != null) {
        CategoryDialog(
            title = stringResource(R.string.edit_category),
            name = editCategoryName,
            onNameChange = { viewModel.updateEditCategoryName(it) },
            nameError = categoryNameError,
            onConfirm = { viewModel.updateCategory() },
            onDismiss = { viewModel.hideEditCategoryDialog() },
            showDeleteButton = true,
            onDelete = {
                // Show confirmation dialog before deleting
                viewModel.deleteCategory()
            }
        )
    }
    
    // Add Subcategory Dialog
    if (showAddSubcategoryDialog) {
        val categoryName = categoriesWithSubcategories
            .find { it.category.id == selectedCategoryId }
            ?.category?.name ?: ""
        
        SubcategoryDialog(
            title = stringResource(R.string.add_subcategory),
            categoryName = categoryName,
            name = newSubcategoryName,
            onNameChange = { viewModel.updateNewSubcategoryName(it) },
            nameError = subcategoryNameError,
            onConfirm = { viewModel.addSubcategory() },
            onDismiss = { viewModel.hideAddSubcategoryDialog() },
            showDeleteButton = false,
            onDelete = {}
        )
    }
    
    // Edit Subcategory Dialog
    if (showEditSubcategoryDialog && subcategoryToEdit != null) {
        val categoryName = categoriesWithSubcategories
            .find { it.category.id == subcategoryToEdit?.categoryId }
            ?.category?.name ?: ""
        
        SubcategoryDialog(
            title = stringResource(R.string.edit_subcategory),
            categoryName = categoryName,
            name = editSubcategoryName,
            onNameChange = { viewModel.updateEditSubcategoryName(it) },
            nameError = subcategoryNameError,
            onConfirm = { viewModel.updateSubcategory() },
            onDismiss = { viewModel.hideEditSubcategoryDialog() },
            showDeleteButton = true,
            onDelete = {
                // Show confirmation dialog before deleting
                viewModel.deleteSubcategory()
            }
        )
    }
}

@Composable
fun CategoryItem(
    categoryWithSubcategories: CategoryWithSubcategories,
    onEditCategory: () -> Unit,
    onAddSubcategory: () -> Unit,
    onEditSubcategory: (Subcategory) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val category = categoryWithSubcategories.category
    val subcategories = categoryWithSubcategories.subcategories
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Category Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowRight,
                    contentDescription = null
                )
                
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )
                
                IconButton(onClick = onEditCategory) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit)
                    )
                }
                
                IconButton(onClick = onAddSubcategory) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_subcategory)
                    )
                }
            }
            
            // Subcategory List
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 8.dp)
                ) {
                    if (subcategories.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_subcategories),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        subcategories.forEach { subcategory ->
                            SubcategoryItem(
                                subcategory = subcategory,
                                onEditSubcategory = { onEditSubcategory(subcategory) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubcategoryItem(
    subcategory: Subcategory,
    onEditSubcategory: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bullet point
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                )
        )
        
        Text(
            text = subcategory.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        )
        
        IconButton(onClick = onEditSubcategory) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = stringResource(R.string.edit),
                modifier = Modifier.size(20.dp)
            )
        }
    }
    
    Divider(
        modifier = Modifier.padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
fun CategoryDialog(
    title: String,
    name: String,
    onNameChange: (String) -> Unit,
    nameError: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    showDeleteButton: Boolean,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.category_name)) },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (showDeleteButton) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = onDelete,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = stringResource(R.string.delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun SubcategoryDialog(
    title: String,
    categoryName: String,
    name: String,
    onNameChange: (String) -> Unit,
    nameError: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    showDeleteButton: Boolean,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.bookmark_category) + ": $categoryName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.subcategory_name)) },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (showDeleteButton) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = onDelete,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = stringResource(R.string.delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
