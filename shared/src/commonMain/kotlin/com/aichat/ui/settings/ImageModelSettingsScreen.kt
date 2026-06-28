package com.aichat.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.aichat.data.database.entity.ImageModelConfigEntity
import com.aichat.data.model.ImageModelConfigRepository
import com.aichat.data.settings.SettingsRepository
import com.aichat.design.AiChatTypography
import com.aichat.design.strings
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.util.UUID

data class ImageModelPreset(
    val id: String,
    val name: String,
    val type: String,
    val baseUrl: String = "",
    val modelName: String = "",
)

val IMAGE_MODEL_PRESETS = listOf(
    ImageModelPreset(
        id = "comfyui",
        name = "ComfyUI",
        type = "comfyui",
    ),
    ImageModelPreset(
        id = "siliconflow",
        name = "SiliconFlow",
        type = "siliconflow",
        baseUrl = "https://api.siliconflow.cn/v1",
        modelName = "stabilityai/stable-diffusion-xl-base-1.0",
    ),
    ImageModelPreset(
        id = "dall-e",
        name = "DALL-E",
        type = "dall-e",
        baseUrl = "https://api.openai.com/v1",
        modelName = "dall-e-3",
    ),
    ImageModelPreset(
        id = "stability",
        name = "Stability AI",
        type = "stability",
        baseUrl = "https://api.stability.ai/v2beta",
        modelName = "stable-diffusion-xl-1024-v1-0",
    ),
)

private val TYPE_COLORS = mapOf(
    "comfyui" to 0xFF61AFEF,
    "siliconflow" to 0xFF98C379,
    "dall-e" to 0xFF10A37F,
    "stability" to 0xFFC678DD,
    "custom_api" to 0xFF9CA3AF,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageModelSettingsScreen(
    onBack: () -> Unit,
    imageModelConfigRepository: ImageModelConfigRepository = koinInject(),
    settingsRepository: SettingsRepository = koinInject(),
) {
    val s = strings()
    val scope = rememberCoroutineScope()
    val configs by imageModelConfigRepository.getAllConfigs().collectAsState(initial = emptyList())
    val autoGenerateImage by settingsRepository.autoGenerateImage.collectAsState(initial = false)
    var showAddDialog by remember { mutableStateOf(false) }
    var editingConfig by remember { mutableStateOf<ImageModelConfigEntity?>(null) }

    if (showAddDialog || editingConfig != null) {
        ImageModelEditDialog(
            existing = editingConfig,
            onDismiss = { showAddDialog = false; editingConfig = null },
            onSave = { config ->
                scope.launch {
                    if (editingConfig != null) {
                        imageModelConfigRepository.updateConfig(config)
                    } else {
                        imageModelConfigRepository.insertConfig(config)
                    }
                }
                showAddDialog = false
                editingConfig = null
            },
        )
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Surface(
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back, tint = Color(0xFF374151))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "图像模型",
                        style = AiChatTypography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF111827),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.size(40.dp))
                }
            }
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
                Text(
                    "暂无图像模型配置，请添加",
                    style = AiChatTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
                // Auto-generate image toggle
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "自动生图",
                                style = AiChatTypography.bodyLarge,
                                color = Color(0xFF111827),
                            )
                            Text(
                                text = "AI 回复后自动生成图片",
                                style = AiChatTypography.bodySmall,
                                color = Color(0xFF9CA3AF),
                            )
                        }
                        Switch(
                            checked = autoGenerateImage,
                            onCheckedChange = { scope.launch { settingsRepository.setAutoGenerateImage(it) } },
                        )
                    }
                }
                items(configs, key = { it.id }) { config ->
                    ImageModelConfigCard(
                        config = config,
                        onClick = { editingConfig = config },
                        onDelete = {
                            scope.launch { imageModelConfigRepository.deleteConfig(config.id) }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageModelConfigCard(
    config: ImageModelConfigEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val s = strings()
    val typeLabel = when (config.type) {
        "comfyui" -> "ComfyUI"
        "siliconflow" -> "SiliconFlow"
        "dall-e" -> "DALL-E"
        "stability" -> "Stability AI"
        else -> config.type
    }
    val subtitle = when (config.type) {
        "comfyui" -> config.serverUrl.ifBlank { "ComfyUI" }
        else -> config.modelName.ifBlank { config.baseUrl }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
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
                    .background(
                        Color(TYPE_COLORS[config.type] ?: 0xFF9CA3AF).copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = typeLabel.take(1).uppercase(),
                    style = AiChatTypography.titleMedium,
                    color = Color(TYPE_COLORS[config.type] ?: 0xFF9CA3AF),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = config.name.ifBlank { typeLabel },
                    style = AiChatTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "$typeLabel · ${subtitle.ifBlank { s.notConfigured }}",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImageModelEditDialog(
    existing: ImageModelConfigEntity?,
    onDismiss: () -> Unit,
    onSave: (ImageModelConfigEntity) -> Unit,
) {
    val s = strings()
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var type by remember { mutableStateOf(existing?.type ?: "") }
    var baseUrl by remember { mutableStateOf(existing?.baseUrl ?: "") }
    var apiKey by remember { mutableStateOf(existing?.apiKey ?: "") }
    var modelName by remember { mutableStateOf(existing?.modelName ?: "") }
    var serverUrl by remember { mutableStateOf(existing?.serverUrl ?: "") }
    var workflowJson by remember { mutableStateOf(existing?.workflowJson ?: "") }
    var positivePromptPrefix by remember { mutableStateOf(existing?.positivePromptPrefix ?: "") }
    var negativePrompt by remember { mutableStateOf(existing?.negativePrompt ?: "") }
    var showPresetPicker by remember { mutableStateOf(type.isBlank() && existing == null) }
    var showEditDialog by remember { mutableStateOf(existing != null) }

    if (showPresetPicker) {
        AlertDialog(
            onDismissRequest = {
                showPresetPicker = false
                onDismiss()
            },
            title = { Text("选择类型") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    IMAGE_MODEL_PRESETS.forEach { preset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    type = preset.type
                                    if (preset.baseUrl.isNotBlank() && baseUrl.isBlank()) baseUrl = preset.baseUrl
                                    if (preset.modelName.isNotBlank() && modelName.isBlank()) modelName = preset.modelName
                                    showPresetPicker = false
                                    showEditDialog = true
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(TYPE_COLORS[preset.type] ?: 0xFF9CA3AF).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = preset.name.take(1),
                                    style = AiChatTypography.titleMedium,
                                    color = Color(TYPE_COLORS[preset.type] ?: 0xFF9CA3AF),
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = preset.name,
                                    style = AiChatTypography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                if (preset.baseUrl.isNotBlank()) {
                                    Text(
                                        text = preset.baseUrl,
                                        style = AiChatTypography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                    androidx.compose.material3.HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                type = "custom_api"
                                showPresetPicker = false
                                showEditDialog = true
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF9CA3AF).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("?", style = AiChatTypography.titleMedium, color = Color(0xFF9CA3AF))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "自定义 API",
                            style = AiChatTypography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = {
                    showPresetPicker = false
                    if (type.isBlank()) onDismiss()
                }) { Text(s.cancel) }
            },
        )
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
        title = { Text(if (existing != null) "编辑图像模型" else "添加图像模型") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Preset quick-fill buttons
                if (type.isNotBlank()) {
                    val currentTypeName = when (type) {
                        "comfyui" -> "ComfyUI"
                        "siliconflow" -> "SiliconFlow"
                        "dall-e" -> "DALL-E"
                        "stability" -> "Stability AI"
                        else -> "自定义 API"
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(TYPE_COLORS[type] ?: 0xFF9CA3AF).copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, Color(TYPE_COLORS[type] ?: 0xFF9CA3AF).copy(alpha = 0.3f)),
                        ) {
                            Text(
                                text = currentTypeName,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = AiChatTypography.bodySmall,
                                color = Color(TYPE_COLORS[type] ?: 0xFF9CA3AF),
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        TextButton(onClick = { showPresetPicker = true }) {
                            Text("切换", style = AiChatTypography.bodySmall)
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(s.name) },
                    placeholder = { Text("我的图像模型") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )

                when (type) {
                    "comfyui" -> {
                        OutlinedTextField(
                            value = serverUrl,
                            onValueChange = { serverUrl = it },
                            label = { Text(s.comfyUiServer) },
                            placeholder = { Text("http://192.168.1.10:8440") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )

                        OutlinedTextField(
                            value = workflowJson,
                            onValueChange = { workflowJson = it },
                            label = { Text(s.workflowJson) },
                            placeholder = { Text("粘贴或通过文件导入") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 6,
                            shape = RoundedCornerShape(12.dp),
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val content = com.aichat.platform.pickJsonFile()
                                        if (content != null) {
                                            workflowJson = content
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                            ) {
                                Text("选择 JSON 文件", style = AiChatTypography.bodySmall)
                            }
                            if (workflowJson.isNotBlank()) {
                                Button(
                                    onClick = { workflowJson = "" },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                ) {
                                    Text(s.clear, style = AiChatTypography.bodySmall)
                                }
                            }
                        }

                        Text(
                            text = "字符数: ${workflowJson.length}",
                            style = AiChatTypography.bodySmall,
                            color = Color(0xFF9CA3AF),
                        )

                        OutlinedTextField(
                            value = positivePromptPrefix,
                                onValueChange = { positivePromptPrefix = it },
                                label = { Text("正面提示词前缀") },
                                placeholder = { Text("very aesthetic, masterpiece, best quality") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                maxLines = 4,
                                shape = RoundedCornerShape(12.dp),
                            )

                        Text(
                            text = "会添加到正面提示词的最前面，作为通用质量 tag。留空则不添加。",
                            style = AiChatTypography.bodySmall,
                            color = Color(0xFF9CA3AF),
                        )

                        OutlinedTextField(
                            value = negativePrompt,
                            onValueChange = { negativePrompt = it },
                            label = { Text("负面提示词") },
                            placeholder = { Text("worst quality, bad quality, bad hands") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(12.dp),
                        )

                        Text(
                            text = "生图时使用的负面提示词。留空则使用工作流中的默认值。",
                            style = AiChatTypography.bodySmall,
                            color = Color(0xFF9CA3AF),
                        )
                    }

                    else -> {
                        OutlinedTextField(
                            value = baseUrl,
                            onValueChange = { baseUrl = it },
                            label = { Text(s.apiHost) },
                            placeholder = { Text("https://api.example.com/v1") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )

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

                        OutlinedTextField(
                            value = modelName,
                            onValueChange = { modelName = it },
                            label = { Text(s.modelName) },
                            placeholder = {
                                when (type) {
                                    "siliconflow" -> Text("stabilityai/stable-diffusion-xl-base-1.0")
                                    "dall-e" -> Text("dall-e-3")
                                    "stability" -> Text("stable-diffusion-xl-1024-v1-0")
                                    else -> Text("model-name")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val id = existing?.id ?: UUID.randomUUID().toString()
                    onSave(
                        ImageModelConfigEntity(
                            id = id,
                            name = name.ifBlank {
                                when (type) {
                                    "comfyui" -> "ComfyUI"
                                    "siliconflow" -> "SiliconFlow"
                                    "dall-e" -> "DALL-E"
                                    "stability" -> "Stability AI"
                                    else -> "自定义 API"
                                }
                            },
                            type = type,
                            baseUrl = baseUrl.trim(),
                            apiKey = apiKey.trim(),
                            modelName = modelName.trim(),
                            serverUrl = serverUrl.trim(),
                            workflowJson = workflowJson.trim(),
                            positivePromptPrefix = positivePromptPrefix.trim(),
                            negativePrompt = negativePrompt.trim(),
                            enabled = true,
                        )
                    )
                },
                enabled = when (type) {
                    "comfyui" -> serverUrl.isNotBlank()
                    else -> baseUrl.isNotBlank() && modelName.isNotBlank()
                },
            ) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
    }
}
