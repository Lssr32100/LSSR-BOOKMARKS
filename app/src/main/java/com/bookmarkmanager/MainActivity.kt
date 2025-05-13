package com.bookmarkmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.bookmarkmanager.ui.navigation.NavigationGraph
import com.bookmarkmanager.ui.theme.BookmarkManagerTheme
import com.bookmarkmanager.util.DataStoreManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        lifecycleScope.launch {
            val isFirstLaunch = dataStoreManager.isFirstLaunch.first()
            
            setContent {
                val themePreference by dataStoreManager.themeMode.collectAsState(initial = 0)
                
                val darkTheme = when (themePreference) {
                    1 -> true  // Dark theme
                    2 -> false // Light theme
                    else -> isSystemInDarkTheme() // System default
                }
                
                BookmarkManagerTheme(darkTheme = darkTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavigationGraph(
                            startDestination = if (isFirstLaunch) "onboarding" else "main",
                            onOnboardingComplete = {
                                lifecycleScope.launch {
                                    dataStoreManager.setFirstLaunch(false)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
