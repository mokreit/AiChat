package com.aichat.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.aichat.data.model.ModelConfigRepository
import com.aichat.data.settings.SettingsRepository
import com.aichat.data.voice.TtsProviderRegistry
import com.aichat.design.AiChatTypography
import com.aichat.design.SettingItem
import com.aichat.design.strings
import kotlinx.coroutines.launch
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
    val voiceApiHost by settingsRepository.voiceApiHost.collectAsState(initial = "")

    val ttsProviderName = ttsProviderRegistry.find(defaultTtsProviderId ?: "")?.displayName
        ?: defaultTtsProviderId?.ifBlank { null }
        ?: "OpenAI Compatible"

    var showSttDialog by remember { mutableStateOf(false) }
    var sttHost by remember { mutableStateOf("") }
    var sttKey by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Initialize STT fields from settings
    androidx.compose.runtime.LaunchedEffect(voiceApiHost) {
        sttHost = voiceApiHost ?: ""
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Surface(color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E7EB))) {
                Row(modifier = Modifier.fillMaxWidth().padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()).padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back, tint = Color(0xFF374151)) }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(s.modelConfig, style = AiChatTypography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Color(0xFF111827))
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Surface(shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 2.dp, border = BorderStroke(1.dp, Color(0xFFE5E7EB)), modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingItem(
                        title = s.textModel,
                        subtitle = "${apiHost?.take(30)?.ifBlank { s.notConfigured } ?: s.notConfigured}",
                        onClick = onTextModelClick,
                    )
                    HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                    SettingItem(
                        title = s.voiceModel,
                        subtitle = ttsProviderName,
                        onClick = onVoiceModelClick,
                    )
                    HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                    SettingItem(
                        title = "语音识别API",
                        subtitle = voiceApiHost?.ifBlank { s.notConfigured } ?: s.notConfigured,
                        onClick = {
                            sttHost = voiceApiHost ?: ""
                            sttKey = ""
                            showSttDialog = true
                        },
                    )
                }
            }
        }
    }

    if (showSttDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSttDialog = false },
            title = { Text("语音识别API (Whisper)") },
            text = {
                Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                    androidx.compose.material3.OutlinedTextField(
                         value = sttHost,
                         onValueChange = { sttHost = it },
                         label = { Text("API地址") },
                         placeholder = { Text("https://api.openai.com") },
                         modifier = Modifier.fillMaxWidth(),
                         singleLine = true,
                     )
                     androidx.compose.material3.OutlinedTextField(
                         value = sttKey,
                         onValueChange = { sttKey = it },
                         label = { Text("API Key") },
                         placeholder = { Text("sk-...") },
                         modifier = Modifier.fillMaxWidth(),
                         singleLine = true,
                     )
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    scope.launch {
                        settingsRepository.setVoiceApiHost(sttHost.trim())
                        settingsRepository.setVoiceApiKey(sttKey.trim())
                    }
                    showSttDialog = false
                }) { Text("保存") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showSttDialog = false }) { Text("取消") }
            },
        )
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
