package com.aichat.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aichat.data.settings.SettingsRepository
import com.aichat.design.AiChatTypography
import com.aichat.design.SettingItem
import com.aichat.design.strings
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    onBack: () -> Unit,
    settingsRepository: SettingsRepository = koinInject(),
) {
    val s = strings()
    val scope = rememberCoroutineScope()
    val isDarkTheme by settingsRepository.darkTheme.collectAsState(initial = true)
    val themeMode by settingsRepository.themeMode.collectAsState(initial = "system")

    var showThemeDialog by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        ThemeModeDialog(
            currentMode = themeMode,
            onDismiss = { showThemeDialog = false },
            onSelect = { mode ->
                scope.launch {
                    settingsRepository.setThemeMode(mode)
                    settingsRepository.setDarkTheme(
                        when (mode) {
                            "light" -> false
                            "dark" -> true
                            else -> true
                        }
                    )
                }
                showThemeDialog = false
            },
        )
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Surface(color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E7EB))) {
                Row(modifier = Modifier.fillMaxWidth().padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()).padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back, tint = Color(0xFF374151)) }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(s.appearance, style = AiChatTypography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Color(0xFF111827))
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.size(40.dp))
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            Surface(shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 2.dp, border = BorderStroke(1.dp, Color(0xFFE5E7EB)), modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingItem(
                        title = s.themeMode,
                        subtitle = when (themeMode) {
                            "light" -> s.lightMode
                            "dark" -> s.darkMode
                            else -> s.followSystem
                        },
                        onClick = { showThemeDialog = true },
                    )
                    HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                    SettingItem(
                        title = s.darkTheme,
                        subtitle = if (isDarkTheme) s.on else s.off,
                        onClick = { scope.launch { settingsRepository.setDarkTheme(!isDarkTheme) } },
                        trailing = {
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = { scope.launch { settingsRepository.setDarkTheme(it) } },
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeModeDialog(
    currentMode: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    val s = strings()
    val options = listOf(
        "system" to s.followSystem,
        "light" to s.lightMode,
        "dark" to s.darkMode,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.themeMode) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                options.forEach { (mode, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (mode == currentMode) {
                                    Modifier.background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.shapes.small,
                                    )
                                } else {
                                    Modifier
                                }
                            )
                            .clickable { onSelect(mode) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = label,
                            style = AiChatTypography.bodyLarge,
                            color = if (mode == currentMode) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}
