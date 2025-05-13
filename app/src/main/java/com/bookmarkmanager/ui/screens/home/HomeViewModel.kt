package com.bookmarkmanager.ui.screens.home

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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val categoryRepository: CategoryRepository,
    private val subcategoryRepository: SubcategoryRepository
) : ViewModel() {

    // State for the home screen
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    
    private val _sortOption = MutableStateFlow(SortOption.NAME_ASC)
    val sortOption = _sortOption.asStateFlow()
    
    private val _filterOption = MutableStateFlow<FilterOption>(FilterOption.All)
    val filterOption = _filterOption.asStateFlow()
    
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    private val _subcategories = MutableStateFlow<List<Subcategory>>(emptyList())
    
    // Bookmarks flow that combines the repository data with filter, sort, and search
    val bookmarksState: StateFlow<BookmarksState> = combine(
        _sortOption,
        _filterOption,
        _searchQuery,
        _categories,
        _subcategories,
        getBookmarksFlow()
    ) { sortOption, filterOption, searchQuery, categories, subcategories, bookmarks ->
        val filteredBookmarks = applyFilters(bookmarks, filterOption, searchQuery)
        val sortedBookmarks = applySorting(filteredBookmarks, sortOption)
        
        BookmarksState(
            bookmarks = sortedBookmarks,
            categories = categories,
            subcategories = subcategories,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BookmarksState(isLoading = true)
    )
    
    init {
        loadCategories()
        loadSubcategories()
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
            }
        }
    }
    
    private fun getBookmarksFlow() = when (_sortOption.value) {
        SortOption.NAME_ASC -> bookmarkRepository.getAllBookmarksOrderByNameAsc()
        SortOption.NAME_DESC -> bookmarkRepository.getAllBookmarksOrderByNameDesc()
        SortOption.CATEGORY -> bookmarkRepository.getAllBookmarksOrderByCategory()
        SortOption.SUBCATEGORY -> bookmarkRepository.getAllBookmarksOrderBySubcategory()
        SortOption.TYPE -> bookmarkRepository.getAllBookmarksOrderByType()
    }
    
    private fun applyFilters(
        bookmarks: List<Bookmark>,
        filterOption: FilterOption,
        searchQuery: String
    ): List<Bookmark> {
        var result = bookmarks
        
        // Apply type filter
        if (filterOption is FilterOption.ByType) {
            result = result.filter { it.type == filterOption.type }
        }
        
        // Apply category filter
        if (filterOption is FilterOption.ByCategory) {
            result = result.filter { it.categoryId == filterOption.categoryId }
        }
        
        // Apply search filter
        if (searchQuery.isNotBlank()) {
            result = result.filter { bookmark ->
                bookmark.name.contains(searchQuery, ignoreCase = true) ||
                _categories.value.firstOrNull { it.id == bookmark.categoryId }?.name?.contains(searchQuery, ignoreCase = true) == true ||
                _subcategories.value.firstOrNull { it.id == bookmark.subcategoryId }?.name?.contains(searchQuery, ignoreCase = true) == true
            }
        }
        
        return result
    }
    
    private fun applySorting(bookmarks: List<Bookmark>, sortOption: SortOption): List<Bookmark> {
        return when (sortOption) {
            SortOption.NAME_ASC -> bookmarks.sortedBy { it.name }
            SortOption.NAME_DESC -> bookmarks.sortedByDescending { it.name }
            SortOption.CATEGORY -> bookmarks.sortedWith(
                compareBy(
                    { category ->
                        _categories.value.firstOrNull { it.id == category.categoryId }?.name ?: ""
                    },
                    { subcategory ->
                        _subcategories.value.firstOrNull { it.id == subcategory.subcategoryId }?.name ?: ""
                    },
                    { it.name }
                )
            )
            SortOption.SUBCATEGORY -> bookmarks.sortedWith(
                compareBy(
                    { subcategory ->
                        _subcategories.value.firstOrNull { it.id == subcategory.subcategoryId }?.name ?: ""
                    },
                    { it.name }
                )
            )
            SortOption.TYPE -> bookmarks.sortedWith(
                compareBy(
                    { it.type },
                    { it.name }
                )
            )
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun updateSortOption(option: SortOption) {
        _sortOption.value = option
    }
    
    fun updateFilterOption(option: FilterOption) {
        _filterOption.value = option
    }
    
    fun deleteBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            bookmarkRepository.deleteBookmark(bookmark)
        }
    }
    
    fun getCategoryName(categoryId: Long): String {
        return _categories.value.firstOrNull { it.id == categoryId }?.name ?: ""
    }
    
    fun getSubcategoryName(subcategoryId: Long): String {
        return _subcategories.value.firstOrNull { it.id == subcategoryId }?.name ?: ""
    }
}

// State representing the current state of bookmarks
data class BookmarksState(
    val bookmarks: List<Bookmark> = emptyList(),
    val categories: List<Category> = emptyList(),
    val subcategories: List<Subcategory> = emptyList(),
    val isLoading: Boolean = false
)

// Enum for sorting options
enum class SortOption {
    NAME_ASC,
    NAME_DESC,
    CATEGORY,
    SUBCATEGORY,
    TYPE
}

// Sealed class for filter options
sealed class FilterOption {
    object All : FilterOption()
    data class ByType(val type: BookmarkType) : FilterOption()
    data class ByCategory(val categoryId: Long) : FilterOption()
}
