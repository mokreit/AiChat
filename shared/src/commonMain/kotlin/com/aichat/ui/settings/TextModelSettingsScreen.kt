package com.aichat.ui.settings

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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.aichat.data.database.entity.ModelConfigEntity
import com.aichat.data.model.ModelConfigRepository
import com.aichat.data.ai.AiRepository
import com.aichat.data.api.ApiResult
import com.aichat.design.AiChatTypography
import com.aichat.design.strings
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

data class TextProvider(
    val id: String,
    val name: String,
    val endpoint: String,
    val color: Long,
)

val TEXT_PROVIDERS = listOf(
    TextProvider("openai", "OpenAI", "https://api.openai.com/v1", 0xFF10A37F),
    TextProvider("claude", "Claude", "https://api.anthropic.com/v1", 0xFFD97757),
    TextProvider("gemini", "Gemini", "https://generativelanguage.googleapis.com/v1beta", 0xFF4285F4),
    TextProvider("deepseek", "DeepSeek", "https://api.deepseek.com/v1", 0xFF4D6BFE),
    TextProvider("qwen", "通义千问", "https://dashscope.aliyuncs.com/compatible-mode/v1", 0xFF6236FF),
    TextProvider("doubao", "豆包", "https://ark.cn-beijing.volces.com/api/v3", 0xFFFF6A00),
    TextProvider("moonshot", "Moonshot", "https://api.moonshot.cn/v1", 0xFF000000),
    TextProvider("glm", "智谱 GLM", "https://open.bigmodel.cn/api/paas/v4", 0xFF3366FF),
    TextProvider("custom-text", "自定义", "", 0xFF999999),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextModelSettingsScreen(
    onBack: () -> Unit,
    modelConfigRepository: ModelConfigRepository = koinInject(),
    aiRepository: AiRepository = koinInject(),
) {
    val s = strings()
    val scope = rememberCoroutineScope()
    val configs by modelConfigRepository.getAllConfigs().collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var editingConfig by remember { mutableStateOf<ModelConfigEntity?>(null) }

    if (showAddDialog || editingConfig != null) {
        TextModelEditDialog(
            existing = editingConfig,
            aiRepository = aiRepository,
            onDismiss = { showAddDialog = false; editingConfig = null },
            onSave = { config ->
                scope.launch {
                    if (editingConfig != null) {
                        modelConfigRepository.updateConfig(config)
                    } else {
                        modelConfigRepository.insertConfig(config)
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
                title = { Text(s.textModel) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
                    }
                },
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
                Text(s.noModelConfig, style = AiChatTypography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    ModelConfigCard(
                        config = config,
                        onClick = { editingConfig = config },
                        onDelete = {
                            scope.launch { modelConfigRepository.deleteConfig(config.id) }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ModelConfigCard(
    config: ModelConfigEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val s = strings()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                    text = config.name.ifBlank { config.modelName },
                    style = AiChatTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = config.modelName.ifBlank { config.baseUrl },
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
private fun TextModelEditDialog(
    existing: ModelConfigEntity?,
    aiRepository: AiRepository,
    onDismiss: () -> Unit,
    onSave: (ModelConfigEntity) -> Unit,
) {
    val s = strings()
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var provider by remember { mutableStateOf(existing?.provider ?: "openai") }
    var baseUrl by remember { mutableStateOf(existing?.baseUrl ?: "https://api.openai.com/v1") }
    var apiKey by remember { mutableStateOf(existing?.apiKey ?: "") }
    var modelName by remember { mutableStateOf(existing?.modelName ?: "") }
    var isFetchingModels by remember { mutableStateOf(false) }
    var fetchError by remember { mutableStateOf("") }
    var availableModels by remember { mutableStateOf<List<String>?>(null) }
    var showModelPicker by remember { mutableStateOf(false) }
    var showProviderPicker by remember { mutableStateOf(false) }

    if (showProviderPicker) {
        ProviderPickerDialog(
            providers = TEXT_PROVIDERS,
            currentProviderId = provider,
            onDismiss = { showProviderPicker = false },
            onSelect = { p ->
                provider = p.id
                if (p.endpoint.isNotBlank()) baseUrl = p.endpoint
                showProviderPicker = false
            },
        )
    }

    if (showModelPicker && availableModels != null) {
        ModelPickerDialog(
            models = availableModels!!,
            currentModel = modelName,
            onDismiss = { showModelPicker = false },
            onSelect = { m -> modelName = m; showModelPicker = false },
        )
    }

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
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                // Provider selector
                val currentProvider = TEXT_PROVIDERS.find { it.id == provider } ?: TEXT_PROVIDERS.first()
                OutlinedTextField(
                    value = currentProvider.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(s.provider) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showProviderPicker = true },
                    enabled = false,
                )
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text(s.apiHost) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text(s.apiKey) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = modelName,
                        onValueChange = { modelName = it },
                        label = { Text(s.modelName) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    OutlinedButton(
                        onClick = {
                            isFetchingModels = true
                            fetchError = ""
                            scope.launch {
                                try {
                                    val result = aiRepository.discoverModels(
                                        apiHost = baseUrl,
                                        apiKey = apiKey,
                                        providerId = provider,
                                    )
                                    when (result) {
                                        is ApiResult.Success -> {
                                            availableModels = result.value.models
                                            if (result.value.models.isNotEmpty()) {
                                                showModelPicker = true
                                            } else {
                                                fetchError = "No models returned"
                                            }
                                        }
                                        is ApiResult.HttpError -> fetchError = "HTTP ${result.statusCode}"
                                        is ApiResult.NetworkError -> fetchError = "Network error"
                                        is ApiResult.UnexpectedError -> fetchError = result.message ?: "Error"
                                    }
                                } catch (e: Exception) {
                                    fetchError = e.message ?: "Connection failed"
                                }
                                isFetchingModels = false
                            }
                        },
                        enabled = baseUrl.isNotBlank() && !isFetchingModels,
                    ) {
                        Text(if (isFetchingModels) s.fetchingModels else s.fetchModels)
                    }
                }
                if (fetchError.isNotBlank()) {
                    Text(fetchError, color = MaterialTheme.colorScheme.error, style = AiChatTypography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val id = existing?.id ?: java.util.UUID.randomUUID().toString()
                    onSave(
                        ModelConfigEntity(
                            id = id,
                            provider = provider,
                            name = name.ifBlank { provider },
                            baseUrl = baseUrl,
                            apiKey = apiKey,
                            modelName = modelName,
                            enabled = true,
                        )
                    )
                },
                enabled = baseUrl.isNotBlank() && apiKey.isNotBlank() && modelName.isNotBlank(),
            ) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun ProviderPickerDialog(
    providers: List<TextProvider>,
    currentProviderId: String,
    onDismiss: () -> Unit,
    onSelect: (TextProvider) -> Unit,
) {
    val s = strings()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.provider) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                providers.forEach { provider ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (provider.id == currentProviderId) {
                                    Modifier.background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.small)
                                } else {
                                    Modifier
                                }
                            )
                            .clickable { onSelect(provider) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = provider.name,
                                style = AiChatTypography.bodyLarge,
                                color = if (provider.id == currentProviderId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            )
                            if (provider.endpoint.isNotBlank()) {
                                Text(text = provider.endpoint, style = AiChatTypography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                Text(text = s.customApi, style = AiChatTypography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } },
    )
}

@Composable
private fun ModelPickerDialog(
    models: List<String>,
    currentModel: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    val s = strings()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.selectModel) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                models.forEach { model ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (model == currentModel) {
                                    Modifier.background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.small)
                                } else {
                                    Modifier
                                }
                            )
                            .clickable { onSelect(model) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = model,
                            style = AiChatTypography.bodyMedium,
                            color = if (model == currentModel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } },
    )
}
