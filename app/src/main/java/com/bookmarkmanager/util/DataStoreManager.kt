package com.bookmarkmanager.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore
    
    // Keys
    private val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
    private val THEME_MODE = intPreferencesKey("theme_mode")
    private val LAST_EXPORT_FORMAT = intPreferencesKey("last_export_format")
    
    // Properties
    val isFirstLaunch: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_FIRST_LAUNCH] ?: true
    }
    
    val themeMode: Flow<Int> = dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: 0 // 0: System Default, 1: Dark, 2: Light
    }
    
    val lastExportFormat: Flow<Int> = dataStore.data.map { preferences ->
        preferences[LAST_EXPORT_FORMAT] ?: 0 // 0: JSON, 1: CSV
    }
    
    // Set methods
    suspend fun setFirstLaunch(isFirstLaunch: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_FIRST_LAUNCH] = isFirstLaunch
        }
    }
    
    suspend fun setThemeMode(themeMode: Int) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = themeMode
        }
    }
    
    suspend fun setLastExportFormat(format: Int) {
        dataStore.edit { preferences ->
            preferences[LAST_EXPORT_FORMAT] = format
        }
    }
}
