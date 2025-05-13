package com.bookmarkmanager.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmarkmanager.data.model.Bookmark
import com.bookmarkmanager.data.model.BookmarkType
import com.bookmarkmanager.data.model.Category
import com.bookmarkmanager.data.model.Subcategory
import com.bookmarkmanager.data.model.relations.SubcategoryWithBookmarks
import com.bookmarkmanager.data.repository.BookmarkRepository
import com.bookmarkmanager.data.repository.CategoryRepository
import com.bookmarkmanager.data.repository.SubcategoryRepository
import com.bookmarkmanager.ui.common.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookmarkWithDetails(
    val bookmark: Bookmark,
    val categoryName: String,
    val subcategoryName: String
)

data class HomeUiState(
    val bookmarks: List<BookmarkWithDetails> = emptyList(),
    val categories: List<Category> = emptyList(),
    val subcategories: List<Subcategory> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val categoryRepository: CategoryRepository,
    private val subcategoryRepository: SubcategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Search and filtering state
    var searchQuery by mutableStateOf("")
        private set
    var selectedCategoryId by mutableStateOf<Int?>(null)
        private set
    var selectedSubcategoryId by mutableStateOf<Int?>(null)
        private set
    var selectedBookmarkType by mutableStateOf<BookmarkType?>(null)
        private set
    var sortOption by mutableStateOf(SortOption.NAME_ASC)
        private set

    // Filter parameters as StateFlow for reactive updates
    private val _searchQueryFlow = MutableStateFlow("")
    private val _selectedCategoryIdFlow = MutableStateFlow<Int?>(null)
    private val _selectedSubcategoryIdFlow = MutableStateFlow<Int?>(null)
    private val _selectedBookmarkTypeFlow = MutableStateFlow<BookmarkType?>(null)
    private val _sortOptionFlow = MutableStateFlow(SortOption.NAME_ASC)

    // Combine and observe categories
    private val categoriesFlow = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Observe subcategories based on selected category
    @OptIn(ExperimentalCoroutinesApi::class)
    private val subcategoriesFlow = _selectedCategoryIdFlow
        .flatMapLatest { categoryId ->
            if (categoryId != null) {
                subcategoryRepository.getSubcategoriesForCategory(categoryId)
            } else {
                subcategoryRepository.getAllSubcategories()
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val filteredBookmarksFlow = combine(
        _searchQueryFlow,
        _selectedCategoryIdFlow,
        _selectedSubcategoryIdFlow,
        _selectedBookmarkTypeFlow,
        _sortOptionFlow,
        categoriesFlow,
        subcategoriesFlow
    ) { searchQuery, categoryId, subcategoryId, bookmarkType, sortOption, categories, subcategories ->
        FilterParams(
            searchQuery = searchQuery,
            categoryId = categoryId,
            subcategoryId = subcategoryId,
            bookmarkType = bookmarkType,
            sortOption = sortOption,
            categories = categories,
            subcategories = subcategories
        )
    }.flatMapLatest { params ->
        getFilteredBookmarks(params)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        // Initialize the UI state flow by combining bookmarks, categories, and subcategories
        viewModelScope.launch {
            combine(
                filteredBookmarksFlow,
                categoriesFlow,
                subcategoriesFlow
            ) { bookmarks, categories, subcategories ->
                HomeUiState(
                    bookmarks = bookmarks,
                    categories = categories,
                    subcategories = subcategories,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
        _searchQueryFlow.value = query
    }

    fun updateSelectedCategory(categoryId: Int?) {
        selectedCategoryId = categoryId
        _selectedCategoryIdFlow.value = categoryId
        // Reset subcategory when category changes
        selectedSubcategoryId = null
        _selectedSubcategoryIdFlow.value = null
    }

    fun updateSelectedSubcategory(subcategoryId: Int?) {
        selectedSubcategoryId = subcategoryId
        _selectedSubcategoryIdFlow.value = subcategoryId
    }

    fun updateSelectedBookmarkType(bookmarkType: BookmarkType?) {
        selectedBookmarkType = bookmarkType
        _selectedBookmarkTypeFlow.value = bookmarkType
    }

    fun updateSortOption(option: SortOption) {
        sortOption = option
        _sortOptionFlow.value = option
    }

    fun deleteBookmark(bookmarkId: Int) {
        viewModelScope.launch {
            bookmarkRepository.deleteBookmark(bookmarkId)
        }
    }

    // Helper class for passing filter parameters
    private data class FilterParams(
        val searchQuery: String,
        val categoryId: Int?,
        val subcategoryId: Int?,
        val bookmarkType: BookmarkType?,
        val sortOption: SortOption,
        val categories: List<Category>,
        val subcategories: List<Subcategory>
    )

    private fun getFilteredBookmarks(params: FilterParams) = bookmarkRepository.getAllBookmarks().map { bookmarks ->
        // Apply search filter
        var filteredBookmarks = if (params.searchQuery.isNotEmpty()) {
            bookmarks.filter { bookmark ->
                bookmark.name.contains(params.searchQuery, ignoreCase = true) ||
                        (bookmark.description?.contains(params.searchQuery, ignoreCase = true) ?: false) ||
                        (bookmark.url?.contains(params.searchQuery, ignoreCase = true) ?: false)
            }
        } else {
            bookmarks
        }

        // Apply category filter
        if (params.categoryId != null) {
            filteredBookmarks = filteredBookmarks.filter { it.categoryId == params.categoryId }
        }

        // Apply subcategory filter
        if (params.subcategoryId != null) {
            filteredBookmarks = filteredBookmarks.filter { it.subcategoryId == params.subcategoryId }
        }

        // Apply bookmark type filter
        if (params.bookmarkType != null) {
            filteredBookmarks = filteredBookmarks.filter { it.bookmarkType == params.bookmarkType }
        }

        // Create bookmark with details objects
        val bookmarksWithDetails = filteredBookmarks.map { bookmark ->
            val category = params.categories.find { it.id == bookmark.categoryId }
            val subcategory = params.subcategories.find { it.id == bookmark.subcategoryId }
            BookmarkWithDetails(
                bookmark = bookmark,
                categoryName = category?.name ?: "Unknown",
                subcategoryName = subcategory?.name ?: "Unknown"
            )
        }

        // Apply sorting
        when (params.sortOption) {
            SortOption.NAME_ASC -> bookmarksWithDetails.sortedBy { it.bookmark.name }
            SortOption.NAME_DESC -> bookmarksWithDetails.sortedByDescending { it.bookmark.name }
            SortOption.CATEGORY -> bookmarksWithDetails.sortedWith(
                compareBy(
                    { it.categoryName },
                    { it.subcategoryName },
                    { it.bookmark.name }
                )
            )
            SortOption.TYPE -> bookmarksWithDetails.sortedWith(
                compareBy(
                    { it.bookmark.bookmarkType.name },
                    { it.bookmark.name }
                )
            )
        }
    }
}