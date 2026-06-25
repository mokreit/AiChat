package com.aichat.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val nickname by settingsRepository.nickname.collectAsState(initial = "")
    val avatarUri by settingsRepository.avatar.collectAsState(initial = "")

    var showThemeDialog by remember { mutableStateOf(false) }
    var showNicknameDialog by remember { mutableStateOf(false) }

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

    if (showNicknameDialog) {
        NicknameDialog(
            currentNickname = nickname ?: "",
            onDismiss = { showNicknameDialog = false },
            onSave = { name ->
                scope.launch { settingsRepository.setNickname(name) }
                showNicknameDialog = false
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.appearance) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            SettingItem(
                title = s.themeMode,
                subtitle = when (themeMode) {
                    "light" -> s.lightMode
                    "dark" -> s.darkMode
                    else -> s.followSystem
                },
                onClick = { showThemeDialog = true },
            )
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
            SettingItem(
                title = s.nickname,
                subtitle = nickname?.ifBlank { null } ?: s.notConfigured,
                onClick = { showNicknameDialog = true },
            )
            SettingItem(
                title = s.avatar,
                subtitle = avatarUri?.ifBlank { null } ?: s.notConfigured,
                onClick = {
                    scope.launch {
                        try {
                            val picker = com.aichat.platform.FilePicker()
                            val uri = picker.pickImage()
                            if (uri != null) {
                                settingsRepository.setAvatar(uri)
                            }
                        } catch (_: Exception) { }
                    }
                },
            )
        }
    }
}

@Composable
private fun NicknameDialog(
    currentNickname: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    val s = strings()
    var name by remember { mutableStateOf(currentNickname) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.nickname) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(s.nickname) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(name) }) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
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
