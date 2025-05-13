package com.bookmarkmanager.ui.screens.categories

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmarkmanager.data.model.Category
import com.bookmarkmanager.data.model.Subcategory
import com.bookmarkmanager.data.model.relations.CategoryWithSubcategories
import com.bookmarkmanager.data.repository.CategoryRepository
import com.bookmarkmanager.data.repository.SubcategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoriesUiState(
    val categoriesWithSubcategories: List<CategoryWithSubcategories> = emptyList(),
    val selectedCategory: Category? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddingCategory: Boolean = false,
    val isEditingCategory: Boolean = false,
    val isAddingSubcategory: Boolean = false,
    val isEditingSubcategory: Boolean = false,
    val isDeleteDialogShown: Boolean = false,
    val deleteType: DeleteType = DeleteType.NONE,
    val categoryToDelete: Category? = null,
    val subcategoryToDelete: Subcategory? = null
)

enum class DeleteType {
    NONE,
    CATEGORY,
    SUBCATEGORY
}

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val subcategoryRepository: SubcategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState(isLoading = true))
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()
    
    // Form fields for adding/editing categories and subcategories
    var categoryName by mutableStateOf("")
        private set
    var selectedCategoryColor by mutableStateOf(Category.DEFAULT_COLOR)
        private set
    var subcategoryName by mutableStateOf("")
        private set
    
    // Form validation errors
    var categoryNameError by mutableStateOf<String?>(null)
        private set
    var subcategoryNameError by mutableStateOf<String?>(null)
        private set
    
    // Category or subcategory being edited
    private var editingCategoryId = 0
    private var editingSubcategoryId = 0
    
    init {
        loadCategories()
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val categories = categoryRepository.getCategoriesWithSubcategories().first()
                _uiState.value = _uiState.value.copy(
                    categoriesWithSubcategories = categories,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error loading categories"
                )
            }
        }
    }
    
    fun selectCategory(category: Category?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }
    
    // Category operations
    fun showAddCategoryDialog() {
        categoryName = ""
        categoryNameError = null
        editingCategoryId = 0
        // Generate a suggested color based on the number of existing categories
        val nextColorIndex = _uiState.value.categoriesWithSubcategories.size
        selectedCategoryColor = Category.getSuggestedColor(nextColorIndex)
        _uiState.value = _uiState.value.copy(isAddingCategory = true)
    }
    
    fun showEditCategoryDialog(category: Category) {
        categoryName = category.name
        categoryNameError = null
        selectedCategoryColor = category.color
        editingCategoryId = category.id
        _uiState.value = _uiState.value.copy(isEditingCategory = true)
    }
    
    fun updateSelectedCategoryColor(color: Long) {
        selectedCategoryColor = color
    }
    
    fun hideAddEditCategoryDialog() {
        _uiState.value = _uiState.value.copy(isAddingCategory = false, isEditingCategory = false)
    }
    
    fun updateCategoryName(name: String) {
        categoryName = name
        validateCategoryName()
    }
    
    private fun validateCategoryName(): Boolean {
        categoryNameError = if (categoryName.isBlank()) {
            "Category name is required"
        } else {
            null
        }
        return categoryNameError == null
    }
    
    fun saveCategory() {
        if (!validateCategoryName()) {
            return
        }
        
        viewModelScope.launch {
            try {
                val category = Category(
                    id = if (_uiState.value.isAddingCategory) 0 else editingCategoryId,
                    name = categoryName
                )
                
                if (_uiState.value.isAddingCategory) {
                    categoryRepository.insertCategory(category)
                    _uiState.value = _uiState.value.copy(isAddingCategory = false)
                } else {
                    categoryRepository.updateCategory(category)
                    _uiState.value = _uiState.value.copy(isEditingCategory = false)
                }
                
                // Reload categories
                loadCategories()
            } catch (e: Exception) {
                // Handle error
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error saving category"
                )
            }
        }
    }
    
    fun showDeleteCategoryDialog(category: Category) {
        _uiState.value = _uiState.value.copy(
            isDeleteDialogShown = true,
            deleteType = DeleteType.CATEGORY,
            categoryToDelete = category
        )
    }
    
    // Subcategory operations
    fun showAddSubcategoryDialog() {
        subcategoryName = ""
        subcategoryNameError = null
        editingSubcategoryId = 0
        _uiState.value = _uiState.value.copy(isAddingSubcategory = true)
    }
    
    fun showEditSubcategoryDialog(subcategory: Subcategory) {
        subcategoryName = subcategory.name
        subcategoryNameError = null
        editingSubcategoryId = subcategory.id
        _uiState.value = _uiState.value.copy(isEditingSubcategory = true)
    }
    
    fun hideAddEditSubcategoryDialog() {
        _uiState.value = _uiState.value.copy(isAddingSubcategory = false, isEditingSubcategory = false)
    }
    
    fun updateSubcategoryName(name: String) {
        subcategoryName = name
        validateSubcategoryName()
    }
    
    private fun validateSubcategoryName(): Boolean {
        subcategoryNameError = if (subcategoryName.isBlank()) {
            "Subcategory name is required"
        } else {
            null
        }
        return subcategoryNameError == null
    }
    
    fun saveSubcategory() {
        if (!validateSubcategoryName() || _uiState.value.selectedCategory == null) {
            return
        }
        
        viewModelScope.launch {
            try {
                val subcategory = Subcategory(
                    id = if (_uiState.value.isAddingSubcategory) 0 else editingSubcategoryId,
                    name = subcategoryName,
                    categoryId = _uiState.value.selectedCategory!!.id
                )
                
                if (_uiState.value.isAddingSubcategory) {
                    subcategoryRepository.insertSubcategory(subcategory)
                    _uiState.value = _uiState.value.copy(isAddingSubcategory = false)
                } else {
                    subcategoryRepository.updateSubcategory(subcategory)
                    _uiState.value = _uiState.value.copy(isEditingSubcategory = false)
                }
                
                // Reload categories
                loadCategories()
            } catch (e: Exception) {
                // Handle error
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error saving subcategory"
                )
            }
        }
    }
    
    fun showDeleteSubcategoryDialog(subcategory: Subcategory) {
        _uiState.value = _uiState.value.copy(
            isDeleteDialogShown = true,
            deleteType = DeleteType.SUBCATEGORY,
            subcategoryToDelete = subcategory
        )
    }
    
    // Delete operations
    fun hideDeleteDialog() {
        _uiState.value = _uiState.value.copy(
            isDeleteDialogShown = false,
            deleteType = DeleteType.NONE,
            categoryToDelete = null,
            subcategoryToDelete = null
        )
    }
    
    fun confirmDelete() {
        viewModelScope.launch {
            try {
                when (_uiState.value.deleteType) {
                    DeleteType.CATEGORY -> {
                        _uiState.value.categoryToDelete?.let {
                            categoryRepository.deleteCategory(it.id)
                            
                            // If deleting the selected category, deselect it
                            if (_uiState.value.selectedCategory?.id == it.id) {
                                selectCategory(null)
                            }
                        }
                    }
                    DeleteType.SUBCATEGORY -> {
                        _uiState.value.subcategoryToDelete?.let {
                            subcategoryRepository.deleteSubcategory(it.id)
                        }
                    }
                    else -> {}
                }
                
                // Reload categories and hide dialog
                loadCategories()
                hideDeleteDialog()
            } catch (e: Exception) {
                // Handle error
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error deleting item"
                )
                hideDeleteDialog()
            }
        }
    }
    
    // Clear error messages
    fun clearErrors() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}