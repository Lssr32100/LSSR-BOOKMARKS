package com.bookmarkmanager.ui.screens.bookmark

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmarkmanager.data.model.Bookmark
import com.bookmarkmanager.data.model.BookmarkType
import com.bookmarkmanager.data.model.Category
import com.bookmarkmanager.data.model.Subcategory
import com.bookmarkmanager.data.repository.BookmarkRepository
import com.bookmarkmanager.data.repository.CategoryRepository
import com.bookmarkmanager.data.repository.SubcategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditBookmarkViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val categoryRepository: CategoryRepository,
    private val subcategoryRepository: SubcategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _state = MutableStateFlow(AddEditBookmarkState())
    val state: StateFlow<AddEditBookmarkState> = _state.asStateFlow()
    
    // Form data
    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()
    
    private val _url = MutableStateFlow("")
    val url = _url.asStateFlow()
    
    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()
    
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId = _selectedCategoryId.asStateFlow()
    
    private val _selectedSubcategoryId = MutableStateFlow<Long?>(null)
    val selectedSubcategoryId = _selectedSubcategoryId.asStateFlow()
    
    private val _selectedType = MutableStateFlow(BookmarkType.FREE)
    val selectedType = _selectedType.asStateFlow()
    
    // All categories and subcategories
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()
    
    private val _subcategories = MutableStateFlow<List<Subcategory>>(emptyList())
    val subcategories = _subcategories.asStateFlow()
    
    // Subcategories filtered by selected category
    private val _filteredSubcategories = MutableStateFlow<List<Subcategory>>(emptyList())
    val filteredSubcategories = _filteredSubcategories.asStateFlow()
    
    // Validation
    private val _nameError = MutableStateFlow<String?>(null)
    val nameError = _nameError.asStateFlow()
    
    private val _categoryError = MutableStateFlow<String?>(null)
    val categoryError = _categoryError.asStateFlow()
    
    private val _subcategoryError = MutableStateFlow<String?>(null)
    val subcategoryError = _subcategoryError.asStateFlow()
    
    // Get bookmarkId from SavedStateHandle
    private val bookmarkId: Long? = savedStateHandle.get<Long>("bookmarkId")?.let {
        if (it == -1L) null else it
    }
    
    init {
        loadCategories()
        loadSubcategories()
        
        if (bookmarkId != null) {
            loadBookmark(bookmarkId)
        } else {
            _state.update { it.copy(isLoading = false) }
        }
    }
    
    private fun loadBookmark(id: Long) {
        viewModelScope.launch {
            val bookmark = bookmarkRepository.getBookmarkById(id)
            if (bookmark != null) {
                _name.value = bookmark.name
                _url.value = bookmark.url ?: ""
                _description.value = bookmark.description ?: ""
                _selectedCategoryId.value = bookmark.categoryId
                _selectedSubcategoryId.value = bookmark.subcategoryId
                _selectedType.value = bookmark.type
                
                // Update filtered subcategories based on selected category
                updateFilteredSubcategories(bookmark.categoryId)
                
                _state.update {
                    it.copy(
                        isLoading = false,
                        isEditMode = true
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Bookmark not found"
                    )
                }
            }
        }
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categories ->
                _categories.value = categories
            }
        }
    }
    
    private fun loadSubcategories() {
        viewModelScope.launch {
            subcategoryRepository.getAllSubcategories().collect { subcategories ->
                _subcategories.value = subcategories
                
                // If a category is selected, update filtered subcategories
                _selectedCategoryId.value?.let { updateFilteredSubcategories(it) }
            }
        }
    }
    
    fun updateName(name: String) {
        _name.value = name
        validateName()
    }
    
    fun updateUrl(url: String) {
        _url.value = url
    }
    
    fun updateDescription(description: String) {
        _description.value = description
    }
    
    fun updateSelectedCategory(categoryId: Long) {
        _selectedCategoryId.value = categoryId
        updateFilteredSubcategories(categoryId)
        validateCategory()
        
        // Clear selected subcategory when category changes
        _selectedSubcategoryId.value = null
        _subcategoryError.value = null
    }
    
    fun updateSelectedSubcategory(subcategoryId: Long) {
        _selectedSubcategoryId.value = subcategoryId
        validateSubcategory()
    }
    
    fun updateSelectedType(type: BookmarkType) {
        _selectedType.value = type
    }
    
    private fun updateFilteredSubcategories(categoryId: Long) {
        _filteredSubcategories.value = _subcategories.value.filter { it.categoryId == categoryId }
    }
    
    fun saveBookmark() {
        if (!validateForm()) return
        
        viewModelScope.launch {
            val categoryId = _selectedCategoryId.value!!
            val subcategoryId = _selectedSubcategoryId.value!!
            
            val bookmark = if (bookmarkId != null) {
                Bookmark(
                    id = bookmarkId,
                    name = _name.value,
                    url = _url.value.takeIf { it.isNotBlank() },
                    description = _description.value.takeIf { it.isNotBlank() },
                    categoryId = categoryId,
                    subcategoryId = subcategoryId,
                    type = _selectedType.value,
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                Bookmark(
                    name = _name.value,
                    url = _url.value.takeIf { it.isNotBlank() },
                    description = _description.value.takeIf { it.isNotBlank() },
                    categoryId = categoryId,
                    subcategoryId = subcategoryId,
                    type = _selectedType.value
                )
            }
            
            try {
                if (bookmarkId != null) {
                    bookmarkRepository.updateBookmark(bookmark)
                } else {
                    bookmarkRepository.insertBookmark(bookmark)
                }
                
                _state.update {
                    it.copy(isSaved = true)
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = "Failed to save bookmark: ${e.message}")
                }
            }
        }
    }
    
    private fun validateForm(): Boolean {
        validateName()
        validateCategory()
        validateSubcategory()
        
        return _nameError.value == null &&
                _categoryError.value == null &&
                _subcategoryError.value == null
    }
    
    private fun validateName() {
        _nameError.value = if (_name.value.isBlank()) {
            "Name is required"
        } else {
            null
        }
    }
    
    private fun validateCategory() {
        _categoryError.value = if (_selectedCategoryId.value == null) {
            "Category is required"
        } else {
            null
        }
    }
    
    private fun validateSubcategory() {
        _subcategoryError.value = if (_selectedSubcategoryId.value == null) {
            "Subcategory is required"
        } else {
            null
        }
    }
}

data class AddEditBookmarkState(
    val isLoading: Boolean = true,
    val isEditMode: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)
