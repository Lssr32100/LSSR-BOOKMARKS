package com.bookmarkmanager.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Categories : Screen("categories")
    object Settings : Screen("settings")
    object AddEditBookmark : Screen("bookmark?id={bookmarkId}") {
        fun createRoute(bookmarkId: Int? = null): String {
            return if (bookmarkId != null) {
                "bookmark?id=$bookmarkId"
            } else {
                "bookmark?id=0"
            }
        }
    }
}