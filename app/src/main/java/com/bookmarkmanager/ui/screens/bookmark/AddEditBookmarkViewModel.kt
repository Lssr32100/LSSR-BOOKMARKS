package com.bookmarkmanager.ui.screens.bookmark

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditBookmarkUiState(
    val categories: List<Category> = emptyList(),
    val subcategories: List<Subcategory> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class AddEditBookmarkViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val categoryRepository: CategoryRepository,
    private val subcategoryRepository: SubcategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditBookmarkUiState(isLoading = true))
    val uiState: StateFlow<AddEditBookmarkUiState> = _uiState.asStateFlow()

    // Form fields
    var bookmarkName by mutableStateOf("")
        private set
    var bookmarkUrl by mutableStateOf("")
        private set
    var bookmarkDescription by mutableStateOf("")
        private set
    var selectedCategoryId by mutableStateOf<Int?>(null)
        private set
    var selectedSubcategoryId by mutableStateOf<Int?>(null)
        private set
    var selectedBookmarkType by mutableStateOf(BookmarkType.FREE)
        private set

    // Validation errors
    var nameError by mutableStateOf<String?>(null)
        private set
    var categoryError by mutableStateOf<String?>(null)
        private set
    var subcategoryError by mutableStateOf<String?>(null)
        private set

    // Tracks available subcategories for the selected category
    private var availableSubcategories = listOf<Subcategory>()

    fun loadBookmark(bookmarkId: Int) {
        if (bookmarkId == 0) {
            // This is a new bookmark, just load categories
            loadCategories()
            _uiState.value = _uiState.value.copy(isLoading = false)
            return
        }

        viewModelScope.launch {
            try {
                val bookmark = bookmarkRepository.getBookmarkById(bookmarkId).first()
                
                bookmark?.let {
                    // Populate form fields
                    bookmarkName = it.name
                    bookmarkUrl = it.url ?: ""
                    bookmarkDescription = it.description ?: ""
                    selectedCategoryId = it.categoryId
                    selectedBookmarkType = it.bookmarkType
                    
                    // Load categories and subcategories
                    loadCategories()
                    
                    // Load subcategories for the selected category
                    if (it.categoryId != null) {
                        loadSubcategoriesForCategory(it.categoryId)
                        selectedSubcategoryId = it.subcategoryId
                    }
                    
                    _uiState.value = _uiState.value.copy(isLoading = false)
                } ?: run {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Bookmark not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error loading bookmark"
                )
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val categories = categoryRepository.getAllCategories().first()
                _uiState.value = _uiState.value.copy(categories = categories)
                
                // If there's only one category, select it automatically
                if (categories.size == 1 && selectedCategoryId == null) {
                    updateSelectedCategory(categories[0].id)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error loading categories"
                )
            }
        }
    }

    private fun loadSubcategoriesForCategory(categoryId: Int) {
        viewModelScope.launch {
            try {
                availableSubcategories = subcategoryRepository.getSubcategoriesForCategory(categoryId).first()
                _uiState.value = _uiState.value.copy(subcategories = availableSubcategories)
                
                // If there's only one subcategory, select it automatically
                if (availableSubcategories.size == 1 && selectedSubcategoryId == null) {
                    updateSelectedSubcategory(availableSubcategories[0].id)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error loading subcategories"
                )
            }
        }
    }

    fun updateBookmarkName(name: String) {
        bookmarkName = name
        validateName()
    }

    fun updateBookmarkUrl(url: String) {
        bookmarkUrl = url
    }

    fun updateBookmarkDescription(description: String) {
        bookmarkDescription = description
    }

    fun updateSelectedCategory(categoryId: Int?) {
        if (categoryId == selectedCategoryId) return
        
        selectedCategoryId = categoryId
        selectedSubcategoryId = null
        validateCategory()
        
        // Load subcategories for the selected category
        if (categoryId != null) {
            loadSubcategoriesForCategory(categoryId)
        } else {
            _uiState.value = _uiState.value.copy(subcategories = emptyList())
        }
    }

    fun updateSelectedSubcategory(subcategoryId: Int?) {
        selectedSubcategoryId = subcategoryId
        validateSubcategory()
    }

    fun updateSelectedBookmarkType(bookmarkType: BookmarkType) {
        selectedBookmarkType = bookmarkType
    }

    private fun validateName(): Boolean {
        nameError = if (bookmarkName.isBlank()) {
            "Name is required"
        } else {
            null
        }
        return nameError == null
    }

    private fun validateCategory(): Boolean {
        categoryError = if (selectedCategoryId == null) {
            "Category is required"
        } else {
            null
        }
        return categoryError == null
    }

    private fun validateSubcategory(): Boolean {
        subcategoryError = if (selectedCategoryId != null && selectedSubcategoryId == null) {
            "Subcategory is required"
        } else {
            null
        }
        return subcategoryError == null
    }

    private fun validateForm(): Boolean {
        val isNameValid = validateName()
        val isCategoryValid = validateCategory()
        val isSubcategoryValid = validateSubcategory()
        
        return isNameValid && isCategoryValid && isSubcategoryValid
    }

    fun saveBookmark(bookmarkId: Int) {
        if (!validateForm()) {
            return
        }
        
        _uiState.value = _uiState.value.copy(isSaving = true)
        
        viewModelScope.launch {
            try {
                val bookmark = Bookmark(
                    id = if (bookmarkId == 0) 0 else bookmarkId,
                    name = bookmarkName,
                    url = bookmarkUrl.ifBlank { null },
                    description = bookmarkDescription.ifBlank { null },
                    categoryId = selectedCategoryId ?: 0, // Should never be null due to validation
                    subcategoryId = selectedSubcategoryId ?: 0, // Should never be null due to validation
                    bookmarkType = selectedBookmarkType
                )
                
                if (bookmarkId == 0) {
                    bookmarkRepository.insertBookmark(bookmark)
                } else {
                    bookmarkRepository.updateBookmark(bookmark)
                }
                
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isSaved = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Error saving bookmark"
                )
            }
        }
    }

    // Clear error messages
    fun clearErrors() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}