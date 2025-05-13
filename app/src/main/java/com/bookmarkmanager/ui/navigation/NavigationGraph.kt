package com.bookmarkmanager.ui.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bookmarkmanager.ui.screens.bookmark.AddEditBookmarkScreen
import com.bookmarkmanager.ui.screens.bookmark.AddEditBookmarkViewModel
import com.bookmarkmanager.ui.screens.categories.CategoriesScreen
import com.bookmarkmanager.ui.screens.categories.CategoriesViewModel
import com.bookmarkmanager.ui.screens.home.HomeScreen
import com.bookmarkmanager.ui.screens.home.HomeViewModel
import com.bookmarkmanager.ui.screens.settings.SettingsScreen
import com.bookmarkmanager.ui.screens.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationGraph() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onNavigateToAddBookmark = {
                    navController.navigate(Screen.AddEditBookmark.createRoute())
                },
                onNavigateToEditBookmark = { bookmarkId ->
                    navController.navigate(Screen.AddEditBookmark.createRoute(bookmarkId))
                },
                onNavigateToCategories = {
                    navController.navigate(Screen.Categories.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(route = Screen.Categories.route) {
            val viewModel: CategoriesViewModel = hiltViewModel()
            CategoriesScreen(
                viewModel = viewModel,
                onNavigateUp = {
                    navController.navigateUp()
                }
            )
        }
        
        composable(route = Screen.Settings.route) {
            val viewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = viewModel,
                onNavigateUp = {
                    navController.navigateUp()
                }
            )
        }
        
        composable(
            route = Screen.AddEditBookmark.route,
            arguments = listOf(
                navArgument("bookmarkId") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val bookmarkId = backStackEntry.arguments?.getInt("bookmarkId") ?: 0
            val isEditing = bookmarkId != 0
            
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Home.route)
            }
            
            val viewModel: AddEditBookmarkViewModel = hiltViewModel(parentEntry)
            
            AddEditBookmarkScreen(
                viewModel = viewModel,
                isEditing = isEditing,
                bookmarkId = bookmarkId,
                onNavigateUp = {
                    navController.navigateUp()
                }
            )
        }
    }
}