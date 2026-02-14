package com.serenesec.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.serenesec.BuildConfig
import com.serenesec.data.preferences.AccentColor
import com.serenesec.data.preferences.SyncFrequency
import com.serenesec.data.preferences.ThemeMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSourcesClick: () -> Unit,
    onWebsitesClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onTagsClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onCollectionsClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val syncFrequency by viewModel.syncFrequency.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val accentColor by viewModel.accentColor.collectAsState()
    val scope = rememberCoroutineScope()
    
    var showSyncDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var showCleanupDialog by remember { mutableStateOf(false) }
    var cleanupMessage by remember { mutableStateOf<String?>(null) }
    
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Content section
            SettingsSectionHeader(title = "Content")
            
            SettingsItem(
                icon = Icons.Default.RssFeed,
                title = "Manage Sources",
                subtitle = "Enable, disable, or add RSS feeds",
                onClick = onSourcesClick
            )
            
            SettingsItem(
                icon = Icons.Default.Language,
                title = "Saved Websites",
                subtitle = "Bookmarked sites without RSS",
                onClick = onWebsitesClick
            )
            
            SettingsItem(
                icon = Icons.Default.Label,
                title = "Custom Tags",
                subtitle = "Create and manage article tags",
                onClick = onTagsClick
            )
            
            SettingsItem(
                icon = Icons.Default.Folder,
                title = "Collections",
                subtitle = "Organize articles into folders",
                onClick = onCollectionsClick
            )
            
            SettingsItem(
                icon = Icons.Default.Sync,
                title = "Sync Frequency",
                subtitle = syncFrequency.displayName,
                onClick = { showSyncDialog = true }
            )
            
            SettingsItem(
                icon = Icons.Default.Delete,
                title = "Clean Up Old Articles",
                subtitle = "Remove articles older than 5 days",
                onClick = { showCleanupDialog = true }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Notifications section
            SettingsSectionHeader(title = "Notifications")
            
            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Push Notifications",
                subtitle = "Alerts, keywords, quiet hours",
                onClick = onNotificationsClick
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Analytics section
            SettingsSectionHeader(title = "Analytics")
            
            SettingsItem(
                icon = Icons.Default.Analytics,
                title = "Source Health & Stats",
                subtitle = "View feed performance and statistics",
                onClick = onAnalyticsClick
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Appearance section
            SettingsSectionHeader(title = "Appearance")
            
            SettingsItem(
                icon = Icons.Default.DarkMode,
                title = "Theme",
                subtitle = when (themeMode) {
                    ThemeMode.LIGHT -> "Light"
                    ThemeMode.DARK -> "Dark"
                    ThemeMode.SYSTEM -> "System default"
                },
                onClick = { showThemeDialog = true }
            )
            
            SettingsItemWithPreview(
                icon = Icons.Default.Palette,
                title = "Accent Color",
                subtitle = "${accentColor.emoji} ${accentColor.displayName}",
                previewColor = accentColor.getColor(isSystemInDarkTheme()),
                onClick = { showColorDialog = true }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // About section
            SettingsSectionHeader(title = "About")
            
            SettingsItem(
                icon = Icons.Default.Info,
                title = "SereneSec",
                subtitle = "Version ${BuildConfig.VERSION_NAME}",
                showChevron = false,
                onClick = {}
            )
            
            // Legal disclaimer
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "SereneSec displays content from third-party sources. All content remains property of original creators. This application does not host, redistribute, or modify content for distribution.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Licensed under AGPL v3",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Sync frequency dialog
    if (showSyncDialog) {
        SettingsRadioDialog(
            title = "Sync Frequency",
            options = SyncFrequency.entries.map { it to it.displayName },
            selectedOption = syncFrequency,
            onOptionSelected = { 
                scope.launch { viewModel.setSyncFrequency(it) }
            },
            onDismiss = { showSyncDialog = false }
        )
    }
    
    // Theme dialog
    if (showThemeDialog) {
        SettingsRadioDialog(
            title = "Theme",
            options = listOf(
                ThemeMode.SYSTEM to "System default",
                ThemeMode.DARK to "Dark",
                ThemeMode.LIGHT to "Light"
            ),
            selectedOption = themeMode,
            onOptionSelected = { 
                scope.launch { viewModel.setThemeMode(it) }
            },
            onDismiss = { showThemeDialog = false }
        )
    }
    
    // Accent color dialog
    if (showColorDialog) {
        AccentColorPickerDialog(
            selectedColor = accentColor,
            onColorSelected = { 
                scope.launch { viewModel.setAccentColor(it) }
            },
            onDismiss = { showColorDialog = false }
        )
    }
    
    // Cleanup confirmation dialog
    if (showCleanupDialog) {
        AlertDialog(
            onDismissRequest = { showCleanupDialog = false },
            title = { Text("Clean Up Old Articles?") },
            text = { 
                Text(
                    "This will remove articles older than 5 days.\n\n" +
                    "Articles that are saved, favorited, archived, or in collections will be kept."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val result = viewModel.cleanupOldArticles()
                            cleanupMessage = result
                            showCleanupDialog = false
                        }
                    }
                ) {
                    Text("Clean Up")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCleanupDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Cleanup result message
    cleanupMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { cleanupMessage = null },
            title = { Text("Cleanup Complete") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { cleanupMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccentColorPickerDialog(
    selectedColor: AccentColor,
    onColorSelected: (AccentColor) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Accent Color",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column {
                Text(
                    text = "Choose the primary color for the app",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))
                
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AccentColor.entries.forEach { color ->
                        ColorOption(
                            color = color.getColor(isDark),
                            emoji = color.emoji,
                            label = color.displayName,
                            isSelected = color == selectedColor,
                            onClick = {
                                onColorSelected(color)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ColorOption(
    color: Color,
    emoji: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.onBackground,
                            shape = CircleShape
                        )
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = emoji,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (showChevron) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsItemWithPreview(
    icon: ImageVector,
    title: String,
    subtitle: String,
    previewColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Color preview circle
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(previewColor)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun <T> SettingsRadioDialog(
    title: String,
    options: List<Pair<T, String>>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (option, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onOptionSelected(option)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == selectedOption,
                            onClick = {
                                onOptionSelected(option)
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = label)
                    }
                }
            }
        },
        confirmButton = {}
    )
}
