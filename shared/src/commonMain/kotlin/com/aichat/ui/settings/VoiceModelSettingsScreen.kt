package com.aichat.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aichat.data.database.entity.VoiceConfigEntity
import com.aichat.data.voice.VoiceConfigRepository
import com.aichat.design.AiChatTypography
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
    voiceConfigRepository: VoiceConfigRepository = koinInject(),
) {
    val s = strings()
    val scope = rememberCoroutineScope()
    val configs by voiceConfigRepository.getAllConfigs().collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var editingConfig by remember { mutableStateOf<VoiceConfigEntity?>(null) }

    if (showAddDialog || editingConfig != null) {
        VoiceModelEditDialog(
            existing = editingConfig,
            onDismiss = { showAddDialog = false; editingConfig = null },
            onSave = { config ->
                scope.launch {
                    if (editingConfig != null) {
                        voiceConfigRepository.updateConfig(config)
                    } else {
                        voiceConfigRepository.insertConfig(config)
                    }
                }
                showAddDialog = false
                editingConfig = null
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.voiceModel, style = AiChatTypography.titleLarge.copy(fontSize = 20.sp)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back, tint = Color(0xFF374151))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = s.addModel)
            }
        },
    ) { padding ->
        if (configs.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(s.notConfigured, style = AiChatTypography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { showAddDialog = true }) {
                    Text(s.addModel)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(configs, key = { it.id }) { config ->
                    VoiceConfigCard(
                        config = config,
                        onClick = { editingConfig = config },
                        onDelete = {
                            scope.launch { voiceConfigRepository.deleteConfig(config.id) }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun VoiceConfigCard(
    config: VoiceConfigEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val s = strings()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(config.provider.hashCode() or 0xFF000000.toInt()).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = config.name.take(1).uppercase(),
                    style = AiChatTypography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = config.name.ifBlank { config.provider },
                    style = AiChatTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = listOfNotNull(
                        config.voiceId.ifBlank { null },
                        config.modelName.ifBlank { null },
                    ).joinToString(" · ").ifBlank { config.baseUrl },
                    style = AiChatTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = s.delete,
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun VoiceModelEditDialog(
    existing: VoiceConfigEntity?,
    onDismiss: () -> Unit,
    onSave: (VoiceConfigEntity) -> Unit,
) {
    val s = strings()
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var provider by remember { mutableStateOf(existing?.provider ?: "openai-compat-tts") }
    var baseUrl by remember { mutableStateOf(existing?.baseUrl ?: "https://api.openai.com/v1") }
    var apiKey by remember { mutableStateOf(existing?.apiKey ?: "") }
    var modelName by remember { mutableStateOf(existing?.modelName ?: "") }
    var voiceId by remember { mutableStateOf(existing?.voiceId ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing != null) s.editModel else s.addModel) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(s.name) },
                        placeholder = { Text("我的语音模型") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                    )
                // Provider selector
                val currentProvider = VOICE_PROVIDERS.find { it.id == provider } ?: VOICE_PROVIDERS.first()
                OutlinedTextField(
                        value = currentProvider.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(s.provider) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                    )
                if (currentProvider.needEndpoint) {
                    OutlinedTextField(
                            value = baseUrl,
                            onValueChange = { baseUrl = it },
                            label = { Text(s.apiHost) },
                            placeholder = { Text("https://api.example.com/v1") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                }
                if (currentProvider.needApiKey) {
                    OutlinedTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            label = { Text(s.apiKey) },
                            placeholder = { Text("sk-...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(12.dp),
                        )
                }
                if (currentProvider.needModel) {
                    OutlinedTextField(
                            value = modelName,
                            onValueChange = { modelName = it },
                            label = { Text(s.modelName) },
                            placeholder = { Text("tts-1") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                }
                OutlinedTextField(
                        value = voiceId,
                        onValueChange = { voiceId = it },
                        label = { Text("Voice ID") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("alloy, shimmer, nova...") },
                        shape = RoundedCornerShape(12.dp),
                    )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val id = existing?.id ?: java.util.UUID.randomUUID().toString()
                    onSave(
                        VoiceConfigEntity(
                            id = id,
                            provider = provider,
                            name = name.ifBlank { provider },
                            baseUrl = baseUrl,
                            apiKey = apiKey,
                            modelName = modelName,
                            voiceId = voiceId,
                            enabled = true,
                        )
                    )
                },
                enabled = name.isNotBlank(),
            ) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}
