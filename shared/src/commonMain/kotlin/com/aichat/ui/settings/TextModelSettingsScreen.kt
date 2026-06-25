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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.aichat.data.ai.AiRepository
import com.aichat.data.api.ApiResult
import com.aichat.data.settings.SettingsRepository
import com.aichat.design.AiChatTypography
import com.aichat.design.SettingItem
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
    settingsRepository: SettingsRepository = koinInject(),
    aiRepository: AiRepository = koinInject(),
) {
    val s = strings()
    val scope = rememberCoroutineScope()
    val apiHost by settingsRepository.apiHost.collectAsState(initial = "")
    val apiKey by settingsRepository.apiKey.collectAsState(initial = "")

    var formApiEndpoint by remember { mutableStateOf("") }
    var formApiKey by remember { mutableStateOf("") }
    var formModel by remember { mutableStateOf("") }
    var availableModels by remember { mutableStateOf<List<String>?>(null) }
    var isFetchingModels by remember { mutableStateOf(false) }
    var fetchError by remember { mutableStateOf(false) }
    var fetchErrorMessage by remember { mutableStateOf("") }
    var showModelPicker by remember { mutableStateOf(false) }
    var showProviderPicker by remember { mutableStateOf(false) }
    var currentProviderId by remember { mutableStateOf("openai") }

    // Sync form fields when saved settings are loaded from DataStore
    LaunchedEffect(apiHost, apiKey) {
        if (apiHost != null && apiHost!!.isNotBlank()) formApiEndpoint = apiHost!!
        if (apiKey != null && apiKey!!.isNotBlank()) formApiKey = apiKey!!
    }

    // Detect current provider from apiHost
    val currentProvider = TEXT_PROVIDERS.find { it.endpoint.isNotBlank() && it.endpoint == formApiEndpoint }
        ?: TEXT_PROVIDERS.find { it.id == "custom-text" }!!

    if (showProviderPicker) {
        ProviderPickerDialog(
            providers = TEXT_PROVIDERS,
            currentProviderId = currentProvider.id,
            onDismiss = { showProviderPicker = false },
            onSelect = { provider ->
                currentProviderId = provider.id
                if (provider.endpoint.isNotBlank()) {
                    formApiEndpoint = provider.endpoint
                }
                showProviderPicker = false
            },
        )
    }

    if (showModelPicker && availableModels != null) {
        ModelPickerDialog(
            models = availableModels!!,
            currentModel = formModel,
            onDismiss = { showModelPicker = false },
            onSelect = { model ->
                formModel = model
                showModelPicker = false
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            // Provider section
            SectionHeader(s.provider)
            SettingItem(
                title = currentProvider.name,
                subtitle = if (currentProvider.endpoint.isNotBlank()) currentProvider.endpoint else s.customApi,
                onClick = { showProviderPicker = true },
            )

            // API config section
            SectionHeader(s.configure)
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                OutlinedTextField(
                    value = formApiEndpoint,
                    onValueChange = { formApiEndpoint = it },
                    label = { Text(s.apiHost) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = formApiKey,
                    onValueChange = { formApiKey = it },
                    label = { Text(s.apiKey) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = formModel,
                        onValueChange = { formModel = it },
                        label = { Text(s.modelName) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    OutlinedButton(
                        onClick = {
                            isFetchingModels = true
                            fetchError = false
                            fetchErrorMessage = ""
                            scope.launch {
                                try {
                                    val result = aiRepository.discoverModels(
                                        apiHost = formApiEndpoint,
                                        apiKey = formApiKey,
                                        providerId = currentProviderId,
                                    )
                                    when (result) {
                                        is ApiResult.Success -> {
                                            availableModels = result.value.models
                                            if (result.value.models.isNotEmpty()) {
                                                showModelPicker = true
                                            } else {
                                                fetchError = true
                                                fetchErrorMessage = "No models returned from server"
                                            }
                                        }
                                        is ApiResult.HttpError -> {
                                            fetchError = true
                                            fetchErrorMessage = "HTTP ${result.statusCode}: ${result.message}"
                                        }
                                        is ApiResult.NetworkError -> {
                                            fetchError = true
                                            fetchErrorMessage = "Network error: ${result.message}"
                                        }
                                        is ApiResult.UnexpectedError -> {
                                            fetchError = true
                                            fetchErrorMessage = result.message ?: "Unknown error"
                                        }
                                    }
                                } catch (e: Exception) {
                                    fetchError = true
                                    fetchErrorMessage = e.message ?: "Connection failed"
                                }
                                isFetchingModels = false
                            }
                        },
                        enabled = formApiEndpoint.isNotBlank() && !isFetchingModels,
                    ) {
                        Text(if (isFetchingModels) s.fetchingModels else s.fetchModels)
                    }
                }
                if (fetchError) {
                    Text(
                        text = fetchErrorMessage.ifBlank { s.fetchModelsFailed },
                        color = MaterialTheme.colorScheme.error,
                        style = AiChatTypography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            // Action buttons
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            settingsRepository.setApiHost(formApiEndpoint)
                            settingsRepository.setApiKey(formApiKey)
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(s.save)
                }
                OutlinedButton(
                    onClick = {
                        formApiEndpoint = ""
                        formApiKey = ""
                        formModel = ""
                        scope.launch {
                            settingsRepository.setApiHost("")
                            settingsRepository.setApiKey("")
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(s.clear)
                }
            }

            // Model list if available
            if (availableModels != null && availableModels!!.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(s.selectModel)
                availableModels!!.forEach { model ->
                    SettingItem(
                        title = model,
                        onClick = {
                            formModel = model
                            scope.launch { settingsRepository.setApiHost(formApiEndpoint) }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProviderPickerDialog(
    providers: List<TextProvider>,
    currentProviderId: String,
    onDismiss: () -> Unit,
    onSelect: (TextProvider) -> Unit,
) {
    val s = strings()
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.provider) },
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
                            .clickable { onSelect(provider) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = provider.name,
                                style = AiChatTypography.bodyLarge,
                                color = if (provider.id == currentProviderId) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                            if (provider.endpoint.isNotBlank()) {
                                Text(
                                    text = provider.endpoint,
                                    style = AiChatTypography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            } else {
                                Text(
                                    text = s.customApi,
                                    style = AiChatTypography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
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
private fun ModelPickerDialog(
    models: List<String>,
    currentModel: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    val s = strings()
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.selectModel) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                models.forEach { model ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (model == currentModel) {
                                    Modifier.background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.shapes.small,
                                    )
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
                            color = if (model == currentModel) {
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
