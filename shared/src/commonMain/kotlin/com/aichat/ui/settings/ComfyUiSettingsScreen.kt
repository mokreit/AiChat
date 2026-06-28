package com.aichat.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aichat.data.comfyui.ComfyUiRepository
import com.aichat.data.database.entity.ComfyUiConfigEntity
import com.aichat.design.AiChatTypography
import com.aichat.design.strings
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComfyUiSettingsScreen(
    onBack: () -> Unit,
    comfyUiRepository: ComfyUiRepository = koinInject(),
) {
    val s = strings()
    val scope = rememberCoroutineScope()
    val config by comfyUiRepository.getConfig().collectAsState(initial = null)
    val settingsRepository: com.aichat.data.settings.SettingsRepository = koinInject()
    val autoGen by settingsRepository.autoGenerateImage.collectAsState(initial = false)

    var serverUrl by remember { mutableStateOf("") }
    var workflowJson by remember { mutableStateOf("") }
    var negativePrompt by remember { mutableStateOf("") }
    var positivePromptPrefix by remember { mutableStateOf("") }
    var connectionStatus by remember { mutableStateOf<String?>(null) }
    var isConnecting by remember { mutableStateOf(false) }

    LaunchedEffect(config) {
        config?.let {
            serverUrl = it.serverUrl
            workflowJson = it.workflowJson
            negativePrompt = it.negativePrompt
            positivePromptPrefix = it.positivePromptPrefix
        }
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
                        text = s.comfyUiSettings,
                        style = AiChatTypography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF111827),
                    )
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Server URL
            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text(s.comfyUiServer) },
                placeholder = { Text("http://192.168.1.10:8440") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )

            // Test connection
            Button(
                onClick = {
                    if (serverUrl.isBlank()) return@Button
                    isConnecting = true
                    connectionStatus = null
                    scope.launch {
                        try {
                            comfyUiRepository.fetchAvailableModels(serverUrl)
                            connectionStatus = "连接成功"
                        } catch (e: Exception) {
                            connectionStatus = "连接失败: ${e.message}"
                        } finally {
                            isConnecting = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                enabled = !isConnecting && serverUrl.isNotBlank(),
            ) {
                if (isConnecting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("测试连接")
            }

            if (connectionStatus != null) {
                Text(
                    text = connectionStatus!!,
                    style = AiChatTypography.bodySmall,
                    color = if (connectionStatus!!.startsWith("连接成功")) Color(0xFF10B981) else Color(0xFFEF4444),
                )
            }

            // Auto/Manual toggle
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("自动生图", style = AiChatTypography.bodyMedium, color = Color(0xFF111827))
                        Text(
                            text = if (autoGen) "发送消息时自动生成图片" else "手动点击消息旁的按钮生图",
                            style = AiChatTypography.bodySmall,
                            color = Color(0xFF9CA3AF),
                        )
                    }
                    androidx.compose.material3.Switch(
                        checked = autoGen,
                        onCheckedChange = { scope.launch { settingsRepository.setAutoGenerateImage(it) } },
                    )
                }
            }

            // Workflow JSON
            OutlinedTextField(
                value = workflowJson,
                onValueChange = { workflowJson = it },
                label = { Text(s.workflowJson) },
                placeholder = { Text("粘贴或通过文件导入工作流 JSON") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 8,
                shape = RoundedCornerShape(12.dp),
            )

            // Import from file button
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
                                connectionStatus = null
                            } else {
                                connectionStatus = "未选择文件"
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                ) {
                    Text("选择 JSON 文件", style = AiChatTypography.bodySmall)
                }

                if (workflowJson.isNotBlank()) {
                    Button(
                        onClick = { workflowJson = "" },
                        modifier = Modifier.weight(0.5f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    ) {
                        Text("清空", style = AiChatTypography.bodySmall)
                    }
                }
            }

            Text(
                text = "字符数: ${workflowJson.length} | 点击「选择 JSON 文件」从手机中选择工作流文件。",
                style = AiChatTypography.bodySmall,
                color = Color(0xFF9CA3AF),
            )

            // Positive prompt prefix
            OutlinedTextField(
                value = positivePromptPrefix,
                onValueChange = { positivePromptPrefix = it },
                label = { Text("正面提示词前缀") },
                placeholder = { Text("very aesthetic, masterpiece, best quality, ultra-detailed") },
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

            // Negative prompt
            OutlinedTextField(
                value = negativePrompt,
                onValueChange = { negativePrompt = it },
                label = { Text("负面提示词") },
                placeholder = { Text("worst quality, bad quality, bad hands, bad feet, malformed hands, low quality") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp),
            )

            Text(
                text = "生图时使用的负面提示词，用英文逗号分隔。留空则使用工作流中的默认值。",
                style = AiChatTypography.bodySmall,
                color = Color(0xFF9CA3AF),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Save button
            Button(
                onClick = {
                    scope.launch {
                        val entity = ComfyUiConfigEntity(
                            id = "default",
                            serverUrl = serverUrl.trim(),
                            workflowJson = workflowJson.trim(),
                            negativePrompt = negativePrompt.trim(),
                            positivePromptPrefix = positivePromptPrefix.trim(),
                        )
                        comfyUiRepository.saveConfig(entity)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF000000),
                    contentColor = Color.White,
                ),
            ) {
                Text(s.save, style = AiChatTypography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
            }
        }
    }
}
