package com.bookmarkmanager.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Main : Screen("main")
    object Home : Screen("home")
    object Categories : Screen("categories")
    object Settings : Screen("settings")
    object AddEditBookmark : Screen("add_edit_bookmark?bookmarkId={bookmarkId}") {
        fun createRoute(bookmarkId: Long? = null): String {
            return if (bookmarkId != null) {
                "add_edit_bookmark?bookmarkId=$bookmarkId"
            } else {
                "add_edit_bookmark"
            }
        }
    }
    object AddEditCategory : Screen("add_edit_category?categoryId={categoryId}") {
        fun createRoute(categoryId: Long? = null): String {
            return if (categoryId != null) {
                "add_edit_category?categoryId=$categoryId"
            } else {
                "add_edit_category"
            }
        }
    }
    object AddEditSubcategory : Screen("add_edit_subcategory?subcategoryId={subcategoryId}&categoryId={categoryId}") {
        fun createRoute(subcategoryId: Long? = null, categoryId: Long? = null): String {
            return when {
                subcategoryId != null -> "add_edit_subcategory?subcategoryId=$subcategoryId&categoryId=$categoryId"
                categoryId != null -> "add_edit_subcategory?categoryId=$categoryId"
                else -> "add_edit_subcategory"
            }
        }
    }
}
