package com.bookmarkmanager.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bookmarkmanager.R
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val exportFormat by viewModel.exportFormat.collectAsState()
    val importFormat by viewModel.importFormat.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Import file launcher
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.importData(it)
        }
    }
    
    // Handle success and error messages
    LaunchedEffect(state) {
        when {
            state.importSuccess -> {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.import_success),
                    duration = SnackbarDuration.Short
                )
                viewModel.clearState()
            }
            state.exportSuccess != null -> {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.export_success, state.exportSuccess),
                    duration = SnackbarDuration.Short
                )
                viewModel.clearState()
            }
            state.importError != null -> {
                snackbarHostState.showSnackbar(
                    message = state.importError ?: context.getString(R.string.import_error),
                    duration = SnackbarDuration.Short
                )
                viewModel.clearState()
            }
            state.exportError != null -> {
                snackbarHostState.showSnackbar(
                    message = state.exportError ?: context.getString(R.string.export_error),
                    duration = SnackbarDuration.Short
                )
                viewModel.clearState()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Theme Settings
            SettingsSectionCard(
                title = stringResource(R.string.theme),
                icon = Icons.Default.DarkMode
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup()
                ) {
                    ThemeOption(
                        text = stringResource(R.string.theme_system),
                        selected = themeMode == 0,
                        onClick = { viewModel.updateThemeMode(0) }
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    ThemeOption(
                        text = stringResource(R.string.theme_dark),
                        selected = themeMode == 1,
                        onClick = { viewModel.updateThemeMode(1) }
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    ThemeOption(
                        text = stringResource(R.string.theme_light),
                        selected = themeMode == 2,
                        onClick = { viewModel.updateThemeMode(2) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Import/Export Settings
            SettingsSectionCard(
                title = stringResource(R.string.import_export),
                icon = Icons.Default.ImportExport
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Export Section
                    Text(
                        text = stringResource(R.string.export_format),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectableGroup()
                    ) {
                        ExportFormatOption(
                            text = stringResource(R.string.format_json),
                            selected = exportFormat == ExportFormat.JSON,
                            onClick = { viewModel.updateExportFormat(ExportFormat.JSON) },
                            modifier = Modifier.weight(1f)
                        )
                        
                        ExportFormatOption(
                            text = stringResource(R.string.format_csv),
                            selected = exportFormat == ExportFormat.CSV,
                            onClick = { viewModel.updateExportFormat(ExportFormat.CSV) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    OutlinedButton(
                        onClick = { viewModel.exportData() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(stringResource(R.string.export_data))
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    // Import Section
                    Text(
                        text = stringResource(R.string.import_format),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectableGroup()
                    ) {
                        ExportFormatOption(
                            text = stringResource(R.string.format_json),
                            selected = importFormat == ExportFormat.JSON,
                            onClick = { viewModel.updateImportFormat(ExportFormat.JSON) },
                            modifier = Modifier.weight(1f)
                        )
                        
                        ExportFormatOption(
                            text = stringResource(R.string.format_csv),
                            selected = importFormat == ExportFormat.CSV,
                            onClick = { viewModel.updateImportFormat(ExportFormat.CSV) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    OutlinedButton(
                        onClick = {
                            val mimeType = when (importFormat) {
                                ExportFormat.JSON -> "application/json"
                                ExportFormat.CSV -> "text/csv"
                            }
                            importFileLauncher.launch(mimeType)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(stringResource(R.string.import_data))
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            
            content()
        }
    }
}

@Composable
fun ThemeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null // null because the parent is already clickable
        )
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
fun ExportFormatOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null // null because the parent is already clickable
        )
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
