package com.bookmarkmanager.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmarkmanager.util.DataStoreManager
import com.bookmarkmanager.util.ImportExportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val importExportManager: ImportExportManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()
    
    // Theme settings
    private val _themeMode = MutableStateFlow(0) // 0: System Default, 1: Dark, 2: Light
    val themeMode = _themeMode.asStateFlow()
    
    // Import/Export settings
    private val _exportFormat = MutableStateFlow(ExportFormat.JSON)
    val exportFormat = _exportFormat.asStateFlow()
    
    private val _importFormat = MutableStateFlow(ExportFormat.JSON)
    val importFormat = _importFormat.asStateFlow()
    
    init {
        loadThemeMode()
    }
    
    private fun loadThemeMode() {
        viewModelScope.launch {
            val savedThemeMode = dataStoreManager.themeMode.first()
            _themeMode.value = savedThemeMode
        }
    }
    
    fun updateThemeMode(mode: Int) {
        viewModelScope.launch {
            dataStoreManager.setThemeMode(mode)
            _themeMode.value = mode
        }
    }
    
    fun updateExportFormat(format: ExportFormat) {
        _exportFormat.value = format
    }
    
    fun updateImportFormat(format: ExportFormat) {
        _importFormat.value = format
    }
    
    fun exportData() {
        viewModelScope.launch {
            try {
                val result = when (_exportFormat.value) {
                    ExportFormat.JSON -> importExportManager.exportToJson()
                    ExportFormat.CSV -> importExportManager.exportToCsv()
                }
                
                if (result.isSuccess) {
                    _state.value = SettingsState(
                        exportSuccess = result.getOrNull()
                    )
                } else {
                    _state.value = SettingsState(
                        exportError = result.exceptionOrNull()?.message ?: "Unknown error occurred during export"
                    )
                }
            } catch (e: Exception) {
                _state.value = SettingsState(
                    exportError = e.message ?: "Unknown error occurred during export"
                )
            }
        }
    }
    
    fun importData(uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val result = when (_importFormat.value) {
                    ExportFormat.JSON -> importExportManager.importFromJson(uri)
                    ExportFormat.CSV -> importExportManager.importFromCsv(uri)
                }
                
                if (result.isSuccess) {
                    _state.value = SettingsState(
                        importSuccess = true
                    )
                } else {
                    _state.value = SettingsState(
                        importError = result.exceptionOrNull()?.message ?: "Unknown error occurred during import"
                    )
                }
            } catch (e: Exception) {
                _state.value = SettingsState(
                    importError = e.message ?: "Unknown error occurred during import"
                )
            }
        }
    }
    
    fun clearState() {
        _state.value = SettingsState()
    }
}

data class SettingsState(
    val isLoading: Boolean = false,
    val importSuccess: Boolean = false,
    val exportSuccess: String? = null,
    val importError: String? = null,
    val exportError: String? = null
)

enum class ExportFormat {
    JSON,
    CSV
}
