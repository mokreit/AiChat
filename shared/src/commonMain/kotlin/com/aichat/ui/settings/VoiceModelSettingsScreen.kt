package com.aichat.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.aichat.data.settings.SettingsRepository
import com.aichat.data.voice.TtsProvider
import com.aichat.data.voice.TtsProviderRegistry
import com.aichat.design.AiChatTypography
import com.aichat.design.SettingItem
import com.aichat.design.strings
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

data class VoiceProviderInfo(
    val id: String,
    val displayName: String,
    val defaultEndpoint: String,
    val needApiKey: Boolean,
    val needEndpoint: Boolean,
    val needModel: Boolean,
)

val VOICE_PROVIDERS = listOf(
    VoiceProviderInfo("openai-compat-tts", "OpenAI TTS", "https://api.openai.com/v1", true, true, true),
    VoiceProviderInfo("edge-tts", "Edge TTS", "", false, true, false),
    VoiceProviderInfo("aliyun-tts", "Aliyun TTS", "https://nls-gateway-cn-shanghai.aliyuncs.com", true, true, false),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceModelSettingsScreen(
    onBack: () -> Unit,
    settingsRepository: SettingsRepository = koinInject(),
    ttsProviderRegistry: TtsProviderRegistry = koinInject(),
) {
    val s = strings()
    val scope = rememberCoroutineScope()
    val defaultTtsProviderId by settingsRepository.defaultTtsProviderId.collectAsState(initial = "")
    val autoPlayVoice by settingsRepository.autoPlayVoice.collectAsState(initial = true)
    val savedApiHost by settingsRepository.voiceApiHost.collectAsState(initial = "")
    val savedApiKey by settingsRepository.voiceApiKey.collectAsState(initial = "")
    val savedModel by settingsRepository.voiceModel.collectAsState(initial = "")

    var showTtsProviderDialog by remember { mutableStateOf(false) }

    // Resolve display name from registry
    val registeredProvider = ttsProviderRegistry.find(defaultTtsProviderId ?: "")
    val providerDisplayName = registeredProvider?.displayName
        ?: defaultTtsProviderId?.ifBlank { null }
        ?: "OpenAI TTS"

    // Detect current voice provider info for endpoint defaults
    val currentProviderInfo = VOICE_PROVIDERS.find { it.id == (defaultTtsProviderId ?: "") }
        ?: VOICE_PROVIDERS.first()

    // Form state (initialized from saved values or defaults from provider)
    var formApiHost by remember(savedApiHost, defaultTtsProviderId) {
        mutableStateOf(savedApiHost?.ifBlank { currentProviderInfo.defaultEndpoint } ?: currentProviderInfo.defaultEndpoint)
    }
    var formApiKey by remember(savedApiKey) { mutableStateOf(savedApiKey ?: "") }
    var formModel by remember(savedModel) { mutableStateOf(savedModel ?: "") }

    if (showTtsProviderDialog) {
        TtsProviderSelectionDialog(
            currentProviderId = defaultTtsProviderId ?: "",
            providers = ttsProviderRegistry.allProviders(),
            onDismiss = { showTtsProviderDialog = false },
            onSelect = { providerId ->
                scope.launch {
                    settingsRepository.setDefaultTtsProviderId(providerId)
                    // Auto-fill API endpoint from provider defaults
                    val info = VOICE_PROVIDERS.find { it.id == providerId }
                    if (info != null && info.defaultEndpoint.isNotBlank()) {
                        settingsRepository.setVoiceApiHost(info.defaultEndpoint)
                    }
                }
                showTtsProviderDialog = false
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.voiceModel) },
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
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            // Provider selection
            SectionHeader(s.provider)
            SettingItem(
                title = providerDisplayName,
                subtitle = s.ttsProvider,
                onClick = { showTtsProviderDialog = true },
            )

            // API config section
            if (currentProviderInfo.needEndpoint || currentProviderInfo.needApiKey) {
                SectionHeader(s.configure)
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    if (currentProviderInfo.needEndpoint) {
                        OutlinedTextField(
                            value = formApiHost,
                            onValueChange = { formApiHost = it },
                            label = { Text(s.apiHost) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (currentProviderInfo.needApiKey) {
                        OutlinedTextField(
                            value = formApiKey,
                            onValueChange = { formApiKey = it },
                            label = { Text(s.apiKey) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (currentProviderInfo.needModel) {
                        OutlinedTextField(
                            value = formModel,
                            onValueChange = { formModel = it },
                            label = { Text(s.modelName) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Action buttons
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                settingsRepository.setVoiceApiHost(formApiHost)
                                settingsRepository.setVoiceApiKey(formApiKey)
                                settingsRepository.setVoiceModel(formModel)
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(s.save)
                    }
                    OutlinedButton(
                        onClick = {
                            formApiHost = currentProviderInfo.defaultEndpoint
                            formApiKey = ""
                            formModel = ""
                            scope.launch {
                                settingsRepository.setVoiceApiHost("")
                                settingsRepository.setVoiceApiKey("")
                                settingsRepository.setVoiceModel("")
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(s.clear)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Auto-play voice
            SectionHeader(s.voice)
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
        }
    }
}

@Composable
private fun TtsProviderSelectionDialog(
    currentProviderId: String,
    providers: List<TtsProvider>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    val s = strings()
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.ttsProvider) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
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

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = AiChatTypography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}
