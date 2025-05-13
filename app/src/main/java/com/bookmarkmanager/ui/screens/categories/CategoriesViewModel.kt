package com.bookmarkmanager.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmarkmanager.data.model.Category
import com.bookmarkmanager.data.model.Subcategory
import com.bookmarkmanager.data.model.relations.CategoryWithSubcategories
import com.bookmarkmanager.data.repository.CategoryRepository
import com.bookmarkmanager.data.repository.SubcategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val subcategoryRepository: SubcategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CategoriesState())
    val state: StateFlow<CategoriesState> = _state.asStateFlow()
    
    val categoriesWithSubcategories = categoryRepository.getAllCategoriesWithSubcategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Add Category Dialog
    private val _showAddCategoryDialog = MutableStateFlow(false)
    val showAddCategoryDialog = _showAddCategoryDialog.asStateFlow()
    
    private val _newCategoryName = MutableStateFlow("")
    val newCategoryName = _newCategoryName.asStateFlow()
    
    private val _categoryNameError = MutableStateFlow<String?>(null)
    val categoryNameError = _categoryNameError.asStateFlow()
    
    // Add Subcategory Dialog
    private val _showAddSubcategoryDialog = MutableStateFlow(false)
    val showAddSubcategoryDialog = _showAddSubcategoryDialog.asStateFlow()
    
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId = _selectedCategoryId.asStateFlow()
    
    private val _newSubcategoryName = MutableStateFlow("")
    val newSubcategoryName = _newSubcategoryName.asStateFlow()
    
    private val _subcategoryNameError = MutableStateFlow<String?>(null)
    val subcategoryNameError = _subcategoryNameError.asStateFlow()
    
    // Edit Category Dialog
    private val _showEditCategoryDialog = MutableStateFlow(false)
    val showEditCategoryDialog = _showEditCategoryDialog.asStateFlow()
    
    private val _categoryToEdit = MutableStateFlow<Category?>(null)
    val categoryToEdit = _categoryToEdit.asStateFlow()
    
    private val _editCategoryName = MutableStateFlow("")
    val editCategoryName = _editCategoryName.asStateFlow()
    
    // Edit Subcategory Dialog
    private val _showEditSubcategoryDialog = MutableStateFlow(false)
    val showEditSubcategoryDialog = _showEditSubcategoryDialog.asStateFlow()
    
    private val _subcategoryToEdit = MutableStateFlow<Subcategory?>(null)
    val subcategoryToEdit = _subcategoryToEdit.asStateFlow()
    
    private val _editSubcategoryName = MutableStateFlow("")
    val editSubcategoryName = _editSubcategoryName.asStateFlow()
    
    // Functions for Add Category Dialog
    fun showAddCategoryDialog() {
        _newCategoryName.value = ""
        _categoryNameError.value = null
        _showAddCategoryDialog.value = true
    }
    
    fun hideAddCategoryDialog() {
        _showAddCategoryDialog.value = false
    }
    
    fun updateNewCategoryName(name: String) {
        _newCategoryName.value = name
        validateCategoryName(name)
    }
    
    fun addCategory() {
        val name = _newCategoryName.value.trim()
        if (validateCategoryName(name)) {
            viewModelScope.launch {
                val category = Category(name = name)
                categoryRepository.insertCategory(category)
                _showAddCategoryDialog.value = false
                _newCategoryName.value = ""
            }
        }
    }
    
    private fun validateCategoryName(name: String): Boolean {
        return when {
            name.isBlank() -> {
                _categoryNameError.value = "Category name is required"
                false
            }
            else -> {
                _categoryNameError.value = null
                true
            }
        }
    }
    
    // Functions for Add Subcategory Dialog
    fun showAddSubcategoryDialog(categoryId: Long) {
        _selectedCategoryId.value = categoryId
        _newSubcategoryName.value = ""
        _subcategoryNameError.value = null
        _showAddSubcategoryDialog.value = true
    }
    
    fun hideAddSubcategoryDialog() {
        _showAddSubcategoryDialog.value = false
    }
    
    fun updateNewSubcategoryName(name: String) {
        _newSubcategoryName.value = name
        validateSubcategoryName(name)
    }
    
    fun addSubcategory() {
        val name = _newSubcategoryName.value.trim()
        val categoryId = _selectedCategoryId.value
        
        if (validateSubcategoryName(name) && categoryId != null) {
            viewModelScope.launch {
                val subcategory = Subcategory(
                    name = name,
                    categoryId = categoryId
                )
                subcategoryRepository.insertSubcategory(subcategory)
                _showAddSubcategoryDialog.value = false
                _newSubcategoryName.value = ""
            }
        }
    }
    
    private fun validateSubcategoryName(name: String): Boolean {
        return when {
            name.isBlank() -> {
                _subcategoryNameError.value = "Subcategory name is required"
                false
            }
            else -> {
                _subcategoryNameError.value = null
                true
            }
        }
    }
    
    // Functions for Edit Category Dialog
    fun showEditCategoryDialog(category: Category) {
        _categoryToEdit.value = category
        _editCategoryName.value = category.name
        _categoryNameError.value = null
        _showEditCategoryDialog.value = true
    }
    
    fun hideEditCategoryDialog() {
        _showEditCategoryDialog.value = false
    }
    
    fun updateEditCategoryName(name: String) {
        _editCategoryName.value = name
        validateCategoryName(name)
    }
    
    fun updateCategory() {
        val name = _editCategoryName.value.trim()
        val category = _categoryToEdit.value
        
        if (validateCategoryName(name) && category != null) {
            viewModelScope.launch {
                val updatedCategory = category.copy(name = name)
                categoryRepository.updateCategory(updatedCategory)
                _showEditCategoryDialog.value = false
            }
        }
    }
    
    fun deleteCategory() {
        val category = _categoryToEdit.value
        
        if (category != null) {
            viewModelScope.launch {
                categoryRepository.deleteCategory(category)
                _showEditCategoryDialog.value = false
            }
        }
    }
    
    // Functions for Edit Subcategory Dialog
    fun showEditSubcategoryDialog(subcategory: Subcategory) {
        _subcategoryToEdit.value = subcategory
        _editSubcategoryName.value = subcategory.name
        _subcategoryNameError.value = null
        _showEditSubcategoryDialog.value = true
    }
    
    fun hideEditSubcategoryDialog() {
        _showEditSubcategoryDialog.value = false
    }
    
    fun updateEditSubcategoryName(name: String) {
        _editSubcategoryName.value = name
        validateSubcategoryName(name)
    }
    
    fun updateSubcategory() {
        val name = _editSubcategoryName.value.trim()
        val subcategory = _subcategoryToEdit.value
        
        if (validateSubcategoryName(name) && subcategory != null) {
            viewModelScope.launch {
                val updatedSubcategory = subcategory.copy(name = name)
                subcategoryRepository.updateSubcategory(updatedSubcategory)
                _showEditSubcategoryDialog.value = false
            }
        }
    }
    
    fun deleteSubcategory() {
        val subcategory = _subcategoryToEdit.value
        
        if (subcategory != null) {
            viewModelScope.launch {
                subcategoryRepository.deleteSubcategory(subcategory)
                _showEditSubcategoryDialog.value = false
            }
        }
    }
}

data class CategoriesState(
    val isLoading: Boolean = false,
    val error: String? = null
)
