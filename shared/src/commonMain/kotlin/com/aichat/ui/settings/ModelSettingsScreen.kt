package com.aichat.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aichat.data.model.ModelConfigRepository
import com.aichat.data.settings.SettingsRepository
import com.aichat.data.voice.TtsProviderRegistry
import com.aichat.design.AiChatTypography
import com.aichat.design.SettingItem
import com.aichat.design.strings
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSettingsScreen(
    onBack: () -> Unit,
    onTextModelClick: () -> Unit,
    onVoiceModelClick: () -> Unit,
    settingsRepository: SettingsRepository = koinInject(),
    ttsProviderRegistry: TtsProviderRegistry = koinInject(),
) {
    val s = strings()
    val defaultTtsProviderId by settingsRepository.defaultTtsProviderId.collectAsState(initial = "")
    val apiHost by settingsRepository.apiHost.collectAsState(initial = "")
    val apiKey by settingsRepository.apiKey.collectAsState(initial = "")

    val ttsProviderName = ttsProviderRegistry.find(defaultTtsProviderId ?: "")?.displayName
        ?: defaultTtsProviderId?.ifBlank { null }
        ?: "OpenAI Compatible"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.modelConfig) },
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
            SectionHeader(s.provider)
            SettingItem(
                title = s.textModel,
                subtitle = "${apiHost?.take(30)?.ifBlank { s.notConfigured } ?: s.notConfigured}",
                onClick = onTextModelClick,
            )
            SettingItem(
                title = s.voiceModel,
                subtitle = ttsProviderName,
                onClick = onVoiceModelClick,
            )
        }
    }
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
