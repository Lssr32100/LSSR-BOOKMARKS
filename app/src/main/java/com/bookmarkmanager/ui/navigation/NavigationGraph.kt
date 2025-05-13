package com.bookmarkmanager.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bookmarkmanager.R
import com.bookmarkmanager.ui.screens.bookmark.AddEditBookmarkScreen
import com.bookmarkmanager.ui.screens.categories.CategoriesScreen
import com.bookmarkmanager.ui.screens.home.HomeScreen
import com.bookmarkmanager.ui.screens.onboarding.OnboardingScreen
import com.bookmarkmanager.ui.screens.settings.SettingsScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings

@Composable
fun NavigationGraph(
    startDestination: String,
    onOnboardingComplete: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val items = listOf(
        Screen.Home to Icons.Default.Home,
        Screen.Categories to Icons.Default.Category,
        Screen.Settings to Icons.Default.Settings
    )
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val showBottomBar = when (currentDestination?.route) {
        Screen.Home.route, Screen.Categories.route, Screen.Settings.route -> true
        else -> false
    }
    
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    items.forEach { (screen, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = null) },
                            label = { 
                                Text(
                                    when (screen) {
                                        Screen.Home -> stringResource(R.string.nav_home)
                                        Screen.Categories -> stringResource(R.string.nav_categories)
                                        Screen.Settings -> stringResource(R.string.nav_settings)
                                        else -> ""
                                    }
                                ) 
                            },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onComplete = {
                        onOnboardingComplete()
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(Screen.Main.route) {
                // Redirect to Home screen
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Main.route) { inclusive = true }
                }
            }
            
            composable(Screen.Home.route) {
                HomeScreen(
                    onAddBookmark = { navController.navigate(Screen.AddEditBookmark.createRoute()) },
                    onEditBookmark = { bookmarkId -> navController.navigate(Screen.AddEditBookmark.createRoute(bookmarkId)) }
                )
            }
            
            composable(Screen.Categories.route) {
                CategoriesScreen(
                    onAddCategory = { navController.navigate(Screen.AddEditCategory.createRoute()) },
                    onEditCategory = { categoryId -> navController.navigate(Screen.AddEditCategory.createRoute(categoryId)) },
                    onAddSubcategory = { categoryId -> navController.navigate(Screen.AddEditSubcategory.createRoute(categoryId = categoryId)) },
                    onEditSubcategory = { subcategoryId, categoryId -> 
                        navController.navigate(Screen.AddEditSubcategory.createRoute(subcategoryId, categoryId))
                    }
                )
            }
            
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            
            composable(
                route = Screen.AddEditBookmark.route,
                arguments = listOf(
                    navArgument("bookmarkId") {
                        type = NavType.LongType
                        defaultValue = -1L
                        nullable = false
                    }
                )
            ) { backStackEntry ->
                val bookmarkId = backStackEntry.arguments?.getLong("bookmarkId") ?: -1L
                AddEditBookmarkScreen(
                    bookmarkId = if (bookmarkId != -1L) bookmarkId else null,
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable(
                route = Screen.AddEditCategory.route,
                arguments = listOf(
                    navArgument("categoryId") {
                        type = NavType.LongType
                        defaultValue = -1L
                        nullable = false
                    }
                )
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: -1L
                // Add/Edit Category Screen implementation will go here
                // For now, just navigate back
                navController.popBackStack()
            }
            
            composable(
                route = Screen.AddEditSubcategory.route,
                arguments = listOf(
                    navArgument("subcategoryId") {
                        type = NavType.LongType
                        defaultValue = -1L
                        nullable = false
                    },
                    navArgument("categoryId") {
                        type = NavType.LongType
                        defaultValue = -1L
                        nullable = false
                    }
                )
            ) { backStackEntry ->
                val subcategoryId = backStackEntry.arguments?.getLong("subcategoryId") ?: -1L
                val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: -1L
                // Add/Edit Subcategory Screen implementation will go here
                // For now, just navigate back
                navController.popBackStack()
            }
        }
    }
}
