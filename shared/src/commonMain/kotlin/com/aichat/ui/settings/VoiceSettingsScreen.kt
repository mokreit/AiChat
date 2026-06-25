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
import com.aichat.data.voice.TtsProviderRegistry
import com.aichat.design.AiChatTypography
import com.aichat.design.SettingItem
import com.aichat.design.strings
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceSettingsScreen(
    onBack: () -> Unit,
    settingsRepository: SettingsRepository = koinInject(),
    ttsProviderRegistry: TtsProviderRegistry = koinInject(),
) {
    val s = strings()
    val scope = rememberCoroutineScope()
    val autoPlayVoice by settingsRepository.autoPlayVoice.collectAsState(initial = true)
    val defaultTtsProvider by settingsRepository.defaultTtsProviderId.collectAsState(initial = "")

    var showTtsProviderDialog by remember { mutableStateOf(false) }

    if (showTtsProviderDialog) {
        TtsProviderSelectionDialog(
            currentProviderId = defaultTtsProvider ?: "",
            providers = ttsProviderRegistry.allProviders(),
            onDismiss = { showTtsProviderDialog = false },
            onSelect = { providerId ->
                scope.launch { settingsRepository.setDefaultTtsProviderId(providerId) }
                showTtsProviderDialog = false
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.voice) },
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
                title = s.autoPlayVoice,
                subtitle = if (autoPlayVoice) s.on else s.off,
                onClick = { scope.launch { settingsRepository.setAutoPlayVoice(!autoPlayVoice) } },
                trailing = {
                    Switch(
                        checked = autoPlayVoice,
                        onCheckedChange = { scope.launch { settingsRepository.setAutoPlayVoice(it) } },
                    )
                },
            )
            SettingItem(
                title = s.ttsProvider,
                subtitle = ttsProviderRegistry.find(defaultTtsProvider ?: "")?.displayName
                    ?: defaultTtsProvider?.ifBlank { null }
                    ?: "OpenAI Compatible",
                onClick = { showTtsProviderDialog = true },
            )
        }
    }
}

@Composable
private fun TtsProviderSelectionDialog(
    currentProviderId: String,
    providers: List<com.aichat.data.voice.TtsProvider>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    val s = strings()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.ttsProvider) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                providers.forEach { provider ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (provider.id == currentProviderId) {
                                    Modifier.background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.shapes.small,
                                    )
                                } else {
                                    Modifier
                                }
                            )
                            .clickable { onSelect(provider.id) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = provider.displayName,
                            style = AiChatTypography.bodyLarge,
                            color = if (provider.id == currentProviderId) {
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
