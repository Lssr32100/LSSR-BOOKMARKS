// Global variables
let currentCategoryId = null;
let currentSort = 'name_asc';
let bookmarkModalMode = 'add';
let categoryModalMode = 'add';
let subcategoryModalMode = 'add';
let itemToDelete = null;
let deleteType = null;

// DOM elements
const elements = {
    // Navigation
    navHome: document.getElementById('nav-home'),
    navCategories: document.getElementById('nav-categories'),
    navSettings: document.getElementById('nav-settings'),
    
    // Screens
    homeScreen: document.getElementById('home-screen'),
    categoriesScreen: document.getElementById('categories-screen'),
    settingsScreen: document.getElementById('settings-screen'),
    
    // Search and filters
    searchInput: document.getElementById('search-input'),
    categoryFilter: document.getElementById('category-filter'),
    typeFilter: document.getElementById('type-filter'),
    sortButtons: document.querySelectorAll('.sort-buttons .btn'),
    
    // Bookmark elements
    bookmarksContainer: document.getElementById('bookmarks-container'),
    loadingIndicator: document.getElementById('loading-indicator'),
    noBookmarksMessage: document.getElementById('no-bookmarks-message'),
    addBookmarkBtn: document.getElementById('add-bookmark-btn'),
    
    // Category elements
    categoriesList: document.getElementById('categories-list'),
    subcategoriesList: document.getElementById('subcategories-list'),
    addCategoryBtn: document.getElementById('add-category-btn'),
    addSubcategoryBtn: document.getElementById('add-subcategory-btn'),
    
    // Settings elements
    exportBtn: document.getElementById('export-btn'),
    importBtn: document.getElementById('import-btn'),
    
    // Bookmark modal
    bookmarkModal: new bootstrap.Modal(document.getElementById('bookmark-modal')),
    bookmarkModalTitle: document.getElementById('bookmark-modal-title'),
    bookmarkForm: document.getElementById('bookmark-form'),
    bookmarkId: document.getElementById('bookmark-id'),
    bookmarkName: document.getElementById('bookmark-name'),
    bookmarkUrl: document.getElementById('bookmark-url'),
    bookmarkDescription: document.getElementById('bookmark-description'),
    bookmarkCategory: document.getElementById('bookmark-category'),
    bookmarkSubcategory: document.getElementById('bookmark-subcategory'),
    bookmarkTypeRadios: document.getElementsByName('bookmark-type'),
    saveBookmarkBtn: document.getElementById('save-bookmark-btn'),
    
    // Category modal
    categoryModal: new bootstrap.Modal(document.getElementById('category-modal')),
    categoryModalTitle: document.getElementById('category-modal-title'),
    categoryForm: document.getElementById('category-form'),
    categoryId: document.getElementById('category-id'),
    categoryName: document.getElementById('category-name'),
    saveCategoryBtn: document.getElementById('save-category-btn'),
    
    // Subcategory modal
    subcategoryModal: new bootstrap.Modal(document.getElementById('subcategory-modal')),
    subcategoryModalTitle: document.getElementById('subcategory-modal-title'),
    subcategoryForm: document.getElementById('subcategory-form'),
    subcategoryId: document.getElementById('subcategory-id'),
    subcategoryName: document.getElementById('subcategory-name'),
    subcategoryCategory: document.getElementById('subcategory-category'),
    saveSubcategoryBtn: document.getElementById('save-subcategory-btn'),
    
    // Delete confirmation modal
    deleteConfirmModal: new bootstrap.Modal(document.getElementById('delete-confirm-modal')),
    deleteConfirmText: document.getElementById('delete-confirm-text'),
    confirmDeleteBtn: document.getElementById('confirm-delete-btn')
};

// Initialize application
document.addEventListener('DOMContentLoaded', () => {
    // Set up event listeners
    setupEventListeners();
    
    // Load initial data
    loadCategories();
    loadBookmarks();
});

// Set up event listeners
function setupEventListeners() {
    // Navigation
    elements.navHome.addEventListener('click', (e) => {
        e.preventDefault();
        showScreen('home');
    });
    
    elements.navCategories.addEventListener('click', (e) => {
        e.preventDefault();
        showScreen('categories');
    });
    
    elements.navSettings.addEventListener('click', (e) => {
        e.preventDefault();
        showScreen('settings');
    });
    
    // Search and filters
    elements.searchInput.addEventListener('input', () => {
        loadBookmarks();
    });
    
    elements.categoryFilter.addEventListener('change', () => {
        loadBookmarks();
    });
    
    elements.typeFilter.addEventListener('change', () => {
        loadBookmarks();
    });
    
    // Sort buttons
    elements.sortButtons.forEach(button => {
        button.addEventListener('click', (e) => {
            elements.sortButtons.forEach(btn => btn.classList.remove('active'));
            e.target.classList.add('active');
            currentSort = e.target.getAttribute('data-sort');
            loadBookmarks();
        });
    });
    
    // Bookmark actions
    elements.addBookmarkBtn.addEventListener('click', () => {
        openAddBookmarkModal();
    });
    
    elements.saveBookmarkBtn.addEventListener('click', () => {
        saveBookmark();
    });
    
    // Category actions
    elements.addCategoryBtn.addEventListener('click', () => {
        openAddCategoryModal();
    });
    
    elements.saveCategoryBtn.addEventListener('click', () => {
        saveCategory();
    });
    
    // Subcategory actions
    elements.addSubcategoryBtn.addEventListener('click', () => {
        openAddSubcategoryModal();
    });
    
    elements.saveSubcategoryBtn.addEventListener('click', () => {
        saveSubcategory();
    });
    
    // Delete confirmation
    elements.confirmDeleteBtn.addEventListener('click', () => {
        deleteItem();
    });
    
    // Bookmark category change event
    elements.bookmarkCategory.addEventListener('change', () => {
        loadSubcategoriesForBookmarkModal();
    });
    
    // Export/Import
    elements.exportBtn.addEventListener('click', exportData);
    elements.importBtn.addEventListener('click', importData);
}

// Show selected screen
function showScreen(screen) {
    // Hide all screens
    elements.homeScreen.classList.add('d-none');
    elements.categoriesScreen.classList.add('d-none');
    elements.settingsScreen.classList.add('d-none');
    
    // Remove active class from all nav items
    elements.navHome.classList.remove('active');
    elements.navCategories.classList.remove('active');
    elements.navSettings.classList.remove('active');
    
    // Show selected screen and mark nav item as active
    if (screen === 'home') {
        elements.homeScreen.classList.remove('d-none');
        elements.navHome.classList.add('active');
        loadBookmarks();
    } else if (screen === 'categories') {
        elements.categoriesScreen.classList.remove('d-none');
        elements.navCategories.classList.add('active');
        loadCategories();
        loadSubcategories();
    } else if (screen === 'settings') {
        elements.settingsScreen.classList.remove('d-none');
        elements.navSettings.classList.add('active');
    }
}

// API Functions
async function fetchBookmarks() {
    const searchQuery = elements.searchInput.value;
    const categoryFilter = elements.categoryFilter.value;
    const typeFilter = elements.typeFilter.value;
    
    let url = `/api/bookmarks?sort=${currentSort}`;
    
    if (searchQuery) {
        url += `&search=${encodeURIComponent(searchQuery)}`;
    }
    
    if (categoryFilter !== 'ALL') {
        url += `&category=${categoryFilter}`;
    }
    
    if (typeFilter !== 'ALL') {
        url += `&type=${typeFilter}`;
    }
    
    const response = await fetch(url);
    if (!response.ok) {
        throw new Error('Failed to fetch bookmarks');
    }
    
    return response.json();
}

async function fetchCategories() {
    const response = await fetch('/api/categories');
    if (!response.ok) {
        throw new Error('Failed to fetch categories');
    }
    
    return response.json();
}

async function fetchSubcategories(categoryId = null) {
    let url = '/api/subcategories';
    if (categoryId) {
        url += `?category_id=${categoryId}`;
    }
    
    const response = await fetch(url);
    if (!response.ok) {
        throw new Error('Failed to fetch subcategories');
    }
    
    return response.json();
}

async function fetchBookmark(id) {
    const response = await fetch(`/api/bookmarks/${id}`);
    if (!response.ok) {
        throw new Error('Failed to fetch bookmark');
    }
    
    return response.json();
}

async function createBookmark(bookmark) {
    const response = await fetch('/api/bookmarks', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(bookmark)
    });
    
    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || 'Failed to create bookmark');
    }
    
    return response.json();
}

async function updateBookmark(id, bookmark) {
    const response = await fetch(`/api/bookmarks/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(bookmark)
    });
    
    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || 'Failed to update bookmark');
    }
    
    return response.json();
}

async function deleteBookmark(id) {
    const response = await fetch(`/api/bookmarks/${id}`, {
        method: 'DELETE'
    });
    
    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || 'Failed to delete bookmark');
    }
    
    return response.json();
}

async function createCategory(category) {
    const response = await fetch('/api/categories', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(category)
    });
    
    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || 'Failed to create category');
    }
    
    return response.json();
}

async function updateCategory(id, category) {
    const response = await fetch(`/api/categories/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(category)
    });
    
    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || 'Failed to update category');
    }
    
    return response.json();
}

async function deleteCategory(id) {
    const response = await fetch(`/api/categories/${id}`, {
        method: 'DELETE'
    });
    
    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || 'Failed to delete category');
    }
    
    return response.json();
}

async function createSubcategory(subcategory) {
    const response = await fetch('/api/subcategories', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(subcategory)
    });
    
    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || 'Failed to create subcategory');
    }
    
    return response.json();
}

async function updateSubcategory(id, subcategory) {
    const response = await fetch(`/api/subcategories/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(subcategory)
    });
    
    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || 'Failed to update subcategory');
    }
    
    return response.json();
}

async function deleteSubcategory(id) {
    const response = await fetch(`/api/subcategories/${id}`, {
        method: 'DELETE'
    });
    
    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || 'Failed to delete subcategory');
    }
    
    return response.json();
}

async function exportDataAPI() {
    const response = await fetch('/api/export');
    if (!response.ok) {
        throw new Error('Failed to export data');
    }
    
    return response.json();
}

// Utility functions
function getSelectedBookmarkType() {
    for (const radio of elements.bookmarkTypeRadios) {
        if (radio.checked) {
            return radio.value;
        }
    }
    return 'FREE';
}

function setBookmarkType(type) {
    for (const radio of elements.bookmarkTypeRadios) {
        if (radio.value === type) {
            radio.checked = true;
            break;
        }
    }
}

// Main functions
async function loadBookmarks() {
    // Show loading indicator
    elements.loadingIndicator.classList.remove('d-none');
    elements.noBookmarksMessage.classList.add('d-none');
    elements.bookmarksContainer.querySelectorAll('.bookmark-card').forEach(card => card.remove());
    
    try {
        const bookmarks = await fetchBookmarks();
        
        // Hide loading indicator
        elements.loadingIndicator.classList.add('d-none');
        
        if (bookmarks.length === 0) {
            elements.noBookmarksMessage.classList.remove('d-none');
            return;
        }
        
        // Create bookmark cards
        bookmarks.forEach(bookmark => {
            const bookmarkCard = createBookmarkCard(bookmark);
            elements.bookmarksContainer.appendChild(bookmarkCard);
        });
    } catch (error) {
        console.error('Error loading bookmarks:', error);
        elements.loadingIndicator.classList.add('d-none');
        elements.noBookmarksMessage.classList.remove('d-none');
        elements.noBookmarksMessage.querySelector('p').textContent = 'Error loading bookmarks. Please try again.';
    }
}

function createBookmarkCard(bookmark) {
    const col = document.createElement('div');
    col.className = 'col-md-6 col-lg-4';
    
    // Get badge class based on bookmark type
    let badgeClass = 'badge-secondary';
    if (bookmark.type === 'FREE') {
        badgeClass = 'badge-free';
    } else if (bookmark.type === 'PAID') {
        badgeClass = 'badge-paid';
    } else if (bookmark.type === 'FREEMIUM') {
        badgeClass = 'badge-freemium';
    }
    
    // Create card HTML
    col.innerHTML = `
        <div class="card bookmark-card" data-id="${bookmark.id}">
            <div class="card-header">
                <h5 class="card-title">${escapeHtml(bookmark.name)}</h5>
                <span class="badge text-bg-${badgeClass === 'badge-free' ? 'success' : badgeClass === 'badge-paid' ? 'danger' : 'warning'} bookmark-type-badge">${bookmark.type}</span>
            </div>
            <div class="card-body">
                <div class="bookmark-category">
                    <i class="fas fa-folder me-1"></i> ${escapeHtml(bookmark.category_name)} > ${escapeHtml(bookmark.subcategory_name)}
                </div>
                ${bookmark.url ? `<div class="bookmark-url"><i class="fas fa-link me-1"></i> <a href="${bookmark.url}" target="_blank">${escapeHtml(bookmark.url)}</a></div>` : ''}
                ${bookmark.description ? `<div class="bookmark-description">${escapeHtml(bookmark.description)}</div>` : ''}
                <div class="bookmark-actions">
                    <button class="btn btn-sm btn-outline-primary edit-bookmark-btn" data-id="${bookmark.id}">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger delete-bookmark-btn" data-id="${bookmark.id}">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
        </div>
    `;
    
    // Add event listeners
    const editBtn = col.querySelector('.edit-bookmark-btn');
    const deleteBtn = col.querySelector('.delete-bookmark-btn');
    
    editBtn.addEventListener('click', () => {
        openEditBookmarkModal(bookmark.id);
    });
    
    deleteBtn.addEventListener('click', () => {
        openDeleteBookmarkConfirmation(bookmark.id, bookmark.name);
    });
    
    return col;
}

async function loadCategories() {
    try {
        const categories = await fetchCategories();
        
        // Update categories filter dropdown
        elements.categoryFilter.innerHTML = '<option value="ALL">All Categories</option>';
        elements.bookmarkCategory.innerHTML = '<option value="">Select Category</option>';
        elements.subcategoryCategory.innerHTML = '<option value="">Select Category</option>';
        
        // Update categories list
        elements.categoriesList.innerHTML = '';
        
        categories.forEach(category => {
            // Add to filter dropdown
            const filterOption = document.createElement('option');
            filterOption.value = category.id;
            filterOption.textContent = category.name;
            elements.categoryFilter.appendChild(filterOption);
            
            // Add to bookmark modal dropdown
            const bookmarkOption = document.createElement('option');
            bookmarkOption.value = category.id;
            bookmarkOption.textContent = category.name;
            elements.bookmarkCategory.appendChild(bookmarkOption);
            
            // Add to subcategory modal dropdown
            const subcategoryOption = document.createElement('option');
            subcategoryOption.value = category.id;
            subcategoryOption.textContent = category.name;
            elements.subcategoryCategory.appendChild(subcategoryOption);
            
            // Add to categories list
            const listItem = document.createElement('li');
            listItem.className = 'list-group-item category-item';
            listItem.setAttribute('data-id', category.id);
            listItem.innerHTML = `
                <span class="item-name">${escapeHtml(category.name)}</span>
                <div class="item-actions">
                    <button class="btn btn-sm btn-outline-primary edit-category-btn">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger delete-category-btn">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            `;
            
            // Add event listeners
            const editBtn = listItem.querySelector('.edit-category-btn');
            const deleteBtn = listItem.querySelector('.delete-category-btn');
            
            editBtn.addEventListener('click', () => {
                openEditCategoryModal(category.id, category.name);
            });
            
            deleteBtn.addEventListener('click', () => {
                openDeleteCategoryConfirmation(category.id, category.name);
            });
            
            // Add selection functionality
            listItem.addEventListener('click', (e) => {
                if (!e.target.closest('.item-actions')) {
                    selectCategory(category.id);
                }
            });
            
            elements.categoriesList.appendChild(listItem);
        });
    } catch (error) {
        console.error('Error loading categories:', error);
    }
}

async function loadSubcategories(categoryId = null) {
    try {
        const subcategories = await fetchSubcategories(categoryId);
        
        // Update subcategories list
        elements.subcategoriesList.innerHTML = '';
        
        subcategories.forEach(subcategory => {
            const listItem = document.createElement('li');
            listItem.className = 'list-group-item subcategory-item';
            listItem.setAttribute('data-id', subcategory.id);
            listItem.setAttribute('data-category-id', subcategory.category_id);
            listItem.innerHTML = `
                <span class="item-name">${escapeHtml(subcategory.name)}</span>
                <small class="text-muted me-2">${escapeHtml(subcategory.category_name)}</small>
                <div class="item-actions">
                    <button class="btn btn-sm btn-outline-primary edit-subcategory-btn">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger delete-subcategory-btn">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            `;
            
            // Add event listeners
            const editBtn = listItem.querySelector('.edit-subcategory-btn');
            const deleteBtn = listItem.querySelector('.delete-subcategory-btn');
            
            editBtn.addEventListener('click', () => {
                openEditSubcategoryModal(subcategory.id, subcategory.name, subcategory.category_id);
            });
            
            deleteBtn.addEventListener('click', () => {
                openDeleteSubcategoryConfirmation(subcategory.id, subcategory.name);
            });
            
            elements.subcategoriesList.appendChild(listItem);
        });
    } catch (error) {
        console.error('Error loading subcategories:', error);
    }
}

async function loadSubcategoriesForBookmarkModal() {
    const categoryId = elements.bookmarkCategory.value;
    if (!categoryId) {
        elements.bookmarkSubcategory.innerHTML = '<option value="">Select Subcategory</option>';
        return;
    }
    
    try {
        const subcategories = await fetchSubcategories(categoryId);
        
        elements.bookmarkSubcategory.innerHTML = '<option value="">Select Subcategory</option>';
        
        subcategories.forEach(subcategory => {
            const option = document.createElement('option');
            option.value = subcategory.id;
            option.textContent = subcategory.name;
            elements.bookmarkSubcategory.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading subcategories for bookmark modal:', error);
    }
}

// Modal functions
function openAddBookmarkModal() {
    bookmarkModalMode = 'add';
    elements.bookmarkModalTitle.textContent = 'Add Bookmark';
    elements.bookmarkForm.reset();
    elements.bookmarkId.value = '';
    elements.bookmarkModal.show();
}

async function openEditBookmarkModal(id) {
    bookmarkModalMode = 'edit';
    elements.bookmarkModalTitle.textContent = 'Edit Bookmark';
    elements.bookmarkForm.reset();
    
    try {
        const bookmark = await fetchBookmark(id);
        
        elements.bookmarkId.value = bookmark.id;
        elements.bookmarkName.value = bookmark.name;
        elements.bookmarkUrl.value = bookmark.url || '';
        elements.bookmarkDescription.value = bookmark.description || '';
        elements.bookmarkCategory.value = bookmark.category_id;
        
        // Load subcategories for selected category
        await loadSubcategoriesForBookmarkModal();
        elements.bookmarkSubcategory.value = bookmark.subcategory_id;
        
        setBookmarkType(bookmark.type);
        
        elements.bookmarkModal.show();
    } catch (error) {
        console.error('Error opening edit bookmark modal:', error);
        alert('Failed to load bookmark details.');
    }
}

async function saveBookmark() {
    const name = elements.bookmarkName.value.trim();
    const url = elements.bookmarkUrl.value.trim();
    const description = elements.bookmarkDescription.value.trim();
    const categoryId = elements.bookmarkCategory.value;
    const subcategoryId = elements.bookmarkSubcategory.value;
    const type = getSelectedBookmarkType();
    
    if (!name) {
        alert('Name is required.');
        return;
    }
    
    if (!categoryId) {
        alert('Category is required.');
        return;
    }
    
    if (!subcategoryId) {
        alert('Subcategory is required.');
        return;
    }
    
    const bookmark = {
        name,
        url: url || null,
        description: description || null,
        category_id: parseInt(categoryId),
        subcategory_id: parseInt(subcategoryId),
        type
    };
    
    try {
        if (bookmarkModalMode === 'add') {
            await createBookmark(bookmark);
        } else {
            const id = parseInt(elements.bookmarkId.value);
            await updateBookmark(id, bookmark);
        }
        
        elements.bookmarkModal.hide();
        loadBookmarks();
    } catch (error) {
        console.error('Error saving bookmark:', error);
        alert('Failed to save bookmark: ' + error.message);
    }
}

function openAddCategoryModal() {
    categoryModalMode = 'add';
    elements.categoryModalTitle.textContent = 'Add Category';
    elements.categoryForm.reset();
    elements.categoryId.value = '';
    elements.categoryModal.show();
}

function openEditCategoryModal(id, name) {
    categoryModalMode = 'edit';
    elements.categoryModalTitle.textContent = 'Edit Category';
    elements.categoryForm.reset();
    
    elements.categoryId.value = id;
    elements.categoryName.value = name;
    
    elements.categoryModal.show();
}

async function saveCategory() {
    const name = elements.categoryName.value.trim();
    
    if (!name) {
        alert('Name is required.');
        return;
    }
    
    const category = { name };
    
    try {
        if (categoryModalMode === 'add') {
            await createCategory(category);
        } else {
            const id = parseInt(elements.categoryId.value);
            await updateCategory(id, category);
        }
        
        elements.categoryModal.hide();
        loadCategories();
        loadBookmarks();
    } catch (error) {
        console.error('Error saving category:', error);
        alert('Failed to save category: ' + error.message);
    }
}

function openAddSubcategoryModal() {
    subcategoryModalMode = 'add';
    elements.subcategoryModalTitle.textContent = 'Add Subcategory';
    elements.subcategoryForm.reset();
    elements.subcategoryId.value = '';
    
    // Preselect category if one is selected
    if (currentCategoryId) {
        elements.subcategoryCategory.value = currentCategoryId;
    }
    
    elements.subcategoryModal.show();
}

function openEditSubcategoryModal(id, name, categoryId) {
    subcategoryModalMode = 'edit';
    elements.subcategoryModalTitle.textContent = 'Edit Subcategory';
    elements.subcategoryForm.reset();
    
    elements.subcategoryId.value = id;
    elements.subcategoryName.value = name;
    elements.subcategoryCategory.value = categoryId;
    
    elements.subcategoryModal.show();
}

async function saveSubcategory() {
    const name = elements.subcategoryName.value.trim();
    const categoryId = elements.subcategoryCategory.value;
    
    if (!name) {
        alert('Name is required.');
        return;
    }
    
    if (!categoryId) {
        alert('Category is required.');
        return;
    }
    
    const subcategory = {
        name,
        category_id: parseInt(categoryId)
    };
    
    try {
        if (subcategoryModalMode === 'add') {
            await createSubcategory(subcategory);
        } else {
            const id = parseInt(elements.subcategoryId.value);
            await updateSubcategory(id, subcategory);
        }
        
        elements.subcategoryModal.hide();
        loadSubcategories(currentCategoryId);
        loadBookmarks();
    } catch (error) {
        console.error('Error saving subcategory:', error);
        alert('Failed to save subcategory: ' + error.message);
    }
}

function openDeleteBookmarkConfirmation(id, name) {
    itemToDelete = id;
    deleteType = 'bookmark';
    elements.deleteConfirmText.textContent = `Are you sure you want to delete the bookmark "${name}"?`;
    elements.deleteConfirmModal.show();
}

function openDeleteCategoryConfirmation(id, name) {
    itemToDelete = id;
    deleteType = 'category';
    elements.deleteConfirmText.textContent = `Are you sure you want to delete the category "${name}"? This will also delete all associated subcategories and bookmarks.`;
    elements.deleteConfirmModal.show();
}

function openDeleteSubcategoryConfirmation(id, name) {
    itemToDelete = id;
    deleteType = 'subcategory';
    elements.deleteConfirmText.textContent = `Are you sure you want to delete the subcategory "${name}"? This will also delete all associated bookmarks.`;
    elements.deleteConfirmModal.show();
}

async function deleteItem() {
    if (!itemToDelete || !deleteType) {
        elements.deleteConfirmModal.hide();
        return;
    }
    
    try {
        if (deleteType === 'bookmark') {
            await deleteBookmark(itemToDelete);
            loadBookmarks();
        } else if (deleteType === 'category') {
            await deleteCategory(itemToDelete);
            loadCategories();
            loadSubcategories();
            loadBookmarks();
        } else if (deleteType === 'subcategory') {
            await deleteSubcategory(itemToDelete);
            loadSubcategories(currentCategoryId);
            loadBookmarks();
        }
        
        elements.deleteConfirmModal.hide();
    } catch (error) {
        console.error(`Error deleting ${deleteType}:`, error);
        alert(`Failed to delete ${deleteType}: ` + error.message);
    }
}

// Helper function for selecting a category
function selectCategory(categoryId) {
    // Clear previous selection
    document.querySelectorAll('.category-item').forEach(item => {
        item.classList.remove('active');
    });
    
    if (categoryId) {
        const selectedItem = document.querySelector(`.category-item[data-id="${categoryId}"]`);
        if (selectedItem) {
            selectedItem.classList.add('active');
        }
    }
    
    currentCategoryId = categoryId;
    loadSubcategories(categoryId);
}

// Export/Import functions
async function exportData() {
    try {
        const data = await exportDataAPI();
        
        // Convert to JSON string
        const jsonString = JSON.stringify(data, null, 2);
        
        // Create a blob and download link
        const blob = new Blob([jsonString], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        
        const a = document.createElement('a');
        a.href = url;
        a.download = 'bookmark_export.json';
        document.body.appendChild(a);
        a.click();
        
        // Clean up
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
    } catch (error) {
        console.error('Error exporting data:', error);
        alert('Failed to export data.');
    }
}

function importData() {
    alert('Import functionality would import data from a JSON file.');
}

// Helper function to escape HTML
function escapeHtml(str) {
    if (!str) return '';
    return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}