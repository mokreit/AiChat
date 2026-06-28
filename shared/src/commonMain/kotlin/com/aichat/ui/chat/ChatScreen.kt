package com.aichat.ui.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.aichat.data.character.CharacterRepository
import com.aichat.data.model.ModelConfigRepository
import com.aichat.design.AiChatColors
import com.aichat.design.AiChatTypography
import com.aichat.design.ChatBubble
import com.aichat.design.ChatInputField
import com.aichat.design.TypingIndicator
import com.aichat.design.strings
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.koin.compose.koinInject

/** Parse content segments: "dialogue" in quotes, *action* in asterisks */
data class ContentSegment(val type: String, val text: String)

fun parseContentSegments(content: String): List<ContentSegment> {
    val segments = mutableListOf<ContentSegment>()
    var i = 0
    val chars = content

    while (i < chars.length) {
        when {
            // Quoted dialogue: "text" or "text"
            chars[i] == '"' || chars[i] == '\u201C' -> {
                val closeChar = if (chars[i] == '"') '"' else '\u201D'
                val start = i
                i++
                while (i < chars.length && chars[i] != closeChar) i++
                if (i < chars.length) i++ // skip closing quote
                segments.add(ContentSegment("dialogue", chars.substring(start, i)))
            }
            // Action: *text*
            chars[i] == '*' -> {
                val start = i
                i++
                while (i < chars.length && chars[i] != '*') i++
                if (i < chars.length) i++ // skip closing *
                val text = chars.substring(start + 1, i - 1)
                segments.add(ContentSegment("action", text))
            }
            else -> {
                val start = i
                while (i < chars.length && chars[i] != '"' && chars[i] != '\u201C' && chars[i] != '*') i++
                if (i > start) {
                    segments.add(ContentSegment("narration", chars.substring(start, i)))
                }
            }
        }
    }
    return segments
}

@Composable
fun RichContentText(content: String) {
    val segments = parseContentSegments(content)
    val annotated = buildAnnotatedString {
        for (seg in segments) {
            when (seg.type) {
                "dialogue" -> withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                    append(seg.text)
                }
                "action" -> withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic,
                    )
                ) {
                    append(seg.text)
                }
                else -> append(seg.text)
            }
        }
    }
    Text(text = annotated)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    sessionId: String,
    characterId: String,
    onBack: () -> Unit,
    onEditCharacter: ((String) -> Unit)? = null,
    viewModel: ChatViewModel = rememberChatViewModel(sessionId, characterId),
    characterRepository: CharacterRepository = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val s = strings()
    val scope = rememberCoroutineScope()
    var bgImageUrl by remember { mutableStateOf("") }
    var bgAlpha by remember { mutableFloatStateOf(0.3f) }
    var bubbleAlpha by remember { mutableFloatStateOf(1.0f) }
    var characterAvatarUri by remember { mutableStateOf("") }
    var halfScreenMode by remember { mutableStateOf(false) }

    val audioRecorder = com.aichat.platform.rememberAudioRecorder()
    var isVoiceMode by remember { mutableStateOf(false) }
    var voiceError by remember { mutableStateOf<String?>(null) }
    val modelConfigRepository = koinInject<com.aichat.data.model.ModelConfigRepository>()

    val settingsRepository = koinInject<com.aichat.data.settings.SettingsRepository>()
    val userAvatarUri by settingsRepository.avatar.collectAsState(initial = "")

    LaunchedEffect(characterId) {
        val char = characterRepository.getCharacterById(characterId)
        bgImageUrl = char?.backgroundImage ?: ""
        bgAlpha = char?.backgroundAlpha ?: 0.3f
        bubbleAlpha = char?.bubbleAlpha ?: 1.0f
        characterAvatarUri = char?.avatarUri ?: ""
    }

    var selectedMessage by remember { mutableStateOf<MessageUi?>(null) }
    var showChatMenu by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var exportSuccess by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(uiState.messages.size, uiState.streamingContent) {
        // Scroll to the last item (bottom of chat)
        if (uiState.messages.isNotEmpty() || uiState.streamingContent.isNotBlank()) {
            val totalItems = uiState.messages.size + if (uiState.isStreaming) 1 else 0
            if (totalItems > 0) {
                listState.animateScrollToItem(totalItems - 1)
            }
        }
    }

    // Message action dialog
    selectedMessage?.let { msg ->
            val clipboardManager = LocalClipboardManager.current
            AlertDialog(
            onDismissRequest = { selectedMessage = null },
            title = { Text(s.messageActions) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    DropdownMenuItem(
                        text = { Text(s.copy) },
                        onClick = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(msg.content))
                            selectedMessage = null
                        },
                    )
                    if (msg.role == "assistant") {
                        DropdownMenuItem(
                            text = { Text(s.regenerate) },
                            onClick = {
                                viewModel.regenerateMessage(msg.id)
                                selectedMessage = null
                            },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(s.rewindToHere) },
                        onClick = {
                            viewModel.rewindToMessage(msg.id)
                            selectedMessage = null
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(s.deleteMessage) },
                        onClick = {
                            viewModel.deleteMessage(msg.id)
                            selectedMessage = null
                        },
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedMessage = null }) { Text(s.cancel) }
            },
        )
    }

    // Clear chat confirmation
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(s.clearChat) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearChat()
                    showClearConfirm = false
                }) { Text(s.confirm) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text(s.cancel) }
            },
        )
    }

    // Export result feedback
    if (exportSuccess != null) {
        AlertDialog(
            onDismissRequest = { exportSuccess = null },
            title = { Text(s.exportChat) },
            text = { Text(if (exportSuccess == true) s.exportSuccess else s.exportFailed) },
            confirmButton = {
                TextButton(onClick = { exportSuccess = null }) { Text(s.confirm) }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.characterName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
                    }
                },
                actions = {
                    IconButton(onClick = { showChatMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = showChatMenu,
                        onDismissRequest = { showChatMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(s.exportChat) },
                            onClick = {
                                showChatMenu = false
                                scope.launch {
                                    val messages = uiState.messages
                                    val sb = StringBuilder()
                                    sb.appendLine("# ${uiState.characterName}")
                                    sb.appendLine()
                                    if (uiState.characterFirstMessage.isNotBlank() && messages.isEmpty()) {
                                        sb.appendLine("[${uiState.characterName}]: ${uiState.characterFirstMessage}")
                                    }
                                    for (msg in messages) {
                                        val label = if (msg.role == "user") "You" else uiState.characterName
                                        sb.appendLine("[$label]: ${msg.content}")
                                        sb.appendLine()
                                    }
                                    val fileSaver = com.aichat.platform.FileSaver()
                                    val result = fileSaver.saveTextFile(
                                        defaultName = "${uiState.characterName}_chat.txt",
                                        content = sb.toString(),
                                    )
                                    exportSuccess = result
                                }
                            },
                        )
                        if (onEditCharacter != null) {
                            DropdownMenuItem(
                                text = { Text(s.editCharacter) },
                                onClick = {
                                    showChatMenu = false
                                    onEditCharacter(characterId)
                                },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(s.clearChat) },
                            onClick = {
                                showClearConfirm = true
                                showChatMenu = false
                            },
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Background image with overlay
            if (!bgImageUrl.isNullOrBlank()) {
                var bgBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

                LaunchedEffect(bgImageUrl) {
                    try {
                        val bitmap = com.aichat.platform.loadImageFromFile(bgImageUrl)
                        if (bitmap != null) {
                            bgBitmap = bitmap
                        }
                    } catch (_: Exception) { }
                }

                bgBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = bgAlpha,
                    )
                }
                // Semi-transparent overlay - only show when background is not fully opaque
                if (bgAlpha < 1f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.35f * (1f - bgAlpha))
                            .background(MaterialTheme.colorScheme.surface),
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Bottom,
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                val messages = if (halfScreenMode && uiState.messages.isNotEmpty()) {
                    listOf(uiState.messages.last())
                } else {
                    uiState.messages
                }

                // Show messages in chronological order
                val displayMessages = messages

                items(displayMessages, key = { it.id }) { message ->
                    var showImageViewer by remember { mutableStateOf(false) }
                    var viewerImageBytes by remember { mutableStateOf<ByteArray?>(null) }
                    var viewerImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
                    val httpClient = koinInject<io.ktor.client.HttpClient>()

                    if (message.content.isNotBlank() || message.imageUri != null) {
                        ChatBubble(
                            text = message.content,
                            isUser = message.role == "user",
                            bubbleAlpha = bubbleAlpha,
                            isPlaying = uiState.playingMessageId == message.id,
                            isSynthesizing = uiState.synthesizingMessageId == message.id,
                            onPlayClick = if (message.role == "assistant") {
                                { viewModel.playVoice(message.id, message.voiceAttachmentUri) }
                            } else null,
                            avatarName = if (message.role == "user") "" else uiState.characterName,
                            avatarUri = if (message.role == "user") "" else characterAvatarUri,
                            userAvatarUri = userAvatarUri ?: "",
                            imageUri = message.imageUri,
                            onImageClick = if (message.imageUri != null) {
                                {
                                    scope.launch {
                                        try {
                                            val bytes: ByteArray = httpClient.get(message.imageUri).body()
                                            viewerImageBytes = bytes
                                            viewerImageBitmap = com.aichat.platform.loadImageFromBytes(bytes)
                                            showImageViewer = true
                                        } catch (_: Exception) {}
                                    }
                                }
                            } else null,
                            onGenerateImage = if (message.role == "assistant" && message.imageUri == null && message.content.isNotBlank()) {
                                { viewModel.generateImageManually(message.content) }
                            } else null,
                            onRegenerateImage = if (message.role == "assistant" && message.imageUri != null) {
                                { viewModel.regenerateImage(message.id, message.content) }
                            } else null,
                            isGeneratingImage = uiState.generatingImage,
                            modifier = Modifier.combinedClickable(
                                onClick = {},
                                onLongClick = { selectedMessage = message },
                            ),
                        )
                    }
                    // Full-screen image viewer for this message
                    if (showImageViewer && viewerImageBitmap != null) {
                        FullScreenImageViewer(
                            imageBitmap = viewerImageBitmap!!,
                            imageBytes = viewerImageBytes,
                            onDismiss = { showImageViewer = false },
                        )
                    }
                }

                // Streaming content (after messages = appears at bottom)
                if (uiState.isStreaming && uiState.streamingContent.isBlank()) {
                    item(key = "loading") {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Box(modifier = Modifier.padding(start = 8.dp, end = 4.dp)) {
                                com.aichat.design.CharacterAvatar(
                                    name = uiState.characterName,
                                    avatarUri = characterAvatarUri,
                                    modifier = Modifier.size(36.dp),
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 6.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            ) {
                                TypingIndicator()
                            }
                        }
                    }
                }
                if (uiState.isStreaming && uiState.streamingContent.isNotBlank()) {
                    item(key = "streaming") {
                        ChatBubble(
                            text = uiState.streamingContent,
                            isUser = false,
                            bubbleAlpha = bubbleAlpha,
                            avatarName = uiState.characterName,
                            avatarUri = characterAvatarUri,
                        )
                    }
                }
            }

            if (uiState.error != null) {
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = AiChatTypography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            // Half/full screen toggle button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(
                    onClick = { halfScreenMode = !halfScreenMode },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = if (halfScreenMode) Icons.Default.Fullscreen else Icons.Default.FullscreenExit,
                        contentDescription = if (halfScreenMode) "全屏" else "半屏",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (voiceError != null) {
                Text(
                    text = voiceError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = AiChatTypography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            if (uiState.suggestions.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for (suggestion in uiState.suggestions) {
                        Surface(
                            onClick = { viewModel.selectSuggestion(suggestion) },
                            shape = RoundedCornerShape(16.dp),
                            color = AiChatColors.aiAccentSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AiChatColors.aiAccent.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = suggestion,
                                style = AiChatTypography.bodySmall,
                                color = AiChatColors.aiAccent,
                                maxLines = 2,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            )
                        }
                    }
                }
            }

            ChatInputField(
                value = uiState.inputText,
                onValueChange = viewModel::updateInput,
                onSend = viewModel::sendMessage,
                isSending = uiState.isStreaming,
                onStop = if (uiState.isStreaming) viewModel::stopGenerating else null,
                modifier = Modifier.padding(12.dp),
                onSuggest = { viewModel.generateSuggestions() },
                isSuggesting = uiState.isSuggesting,
                onVoiceToggle = {
                    isVoiceMode = !isVoiceMode
                },
                isVoiceMode = isVoiceMode,
                onVoicePressStart = {
                    voiceError = null
                    audioRecorder.startRecording(
                        onError = { err ->
                            voiceError = err
                        }
                    )
                },
                onVoicePressEnd = {
                    val result = audioRecorder.stopRecording(cancel = false)
                    if (result != null) {
                        viewModel.sendVoiceMessage(result.filePath, result.durationMs)
                    } else {
                        voiceError = "录音时间太短"
                    }
                },
                onVoicePressCancel = {
                    audioRecorder.stopRecording(cancel = true)
                },
            )
            }
        }
    }
}

@Composable
private fun FullScreenImageViewer(
    imageBitmap: ImageBitmap,
    imageBytes: ByteArray?,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var saveMessage by remember { mutableStateOf<String?>(null) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = {
            onDismiss()
            scale = 1f; offsetX = 0f; offsetY = 0f
        },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 5f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (scale > 1.5f) {
                                scale = 1f; offsetX = 0f; offsetY = 0f
                            } else {
                                scale = 3f
                            }
                        },
                    )
                },
        ) {
            // Image with zoom/pan
            Image(
                bitmap = imageBitmap,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY,
                    ),
                contentScale = ContentScale.Fit,
            )

            // Close button (top-right)
            IconButton(
                onClick = {
                    onDismiss()
                    scale = 1f; offsetX = 0f; offsetY = 0f
                },
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 40.dp, end = 16.dp),
            ) {
                Text("X", color = Color.White, fontSize = 20.sp)
            }

            // Save button (bottom-center)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
                    .combinedClickable(
                        onClick = {
                            scope.launch {
                                val bytes = imageBytes
                                if (bytes != null) {
                                    val file = java.io.File(
                                        android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_PICTURES),
                                        "AiChat_${System.currentTimeMillis()}.png"
                                    )
                                    try {
                                        file.writeBytes(bytes)
                                        saveMessage = "已保存到相册"
                                    } catch (e: Exception) {
                                        saveMessage = "保存失败: ${e.message}"
                                    }
                                }
                            }
                        },
                        onLongClick = {},
                    ),
            ) {
                Text(
                    text = "保存图片",
                    style = AiChatTypography.bodyMedium,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                )
            }

            // Save toast
            if (saveMessage != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 100.dp),
                ) {
                    Text(
                        text = saveMessage!!,
                        style = AiChatTypography.bodySmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberChatViewModel(
    sessionId: String,
    characterId: String,
): ChatViewModel {
    val aiRepository = koinInject<com.aichat.data.ai.AiRepository>()
    val modelConfigRepository = koinInject<com.aichat.data.model.ModelConfigRepository>()
    val chatSessionRepository = koinInject<com.aichat.data.chat.ChatSessionRepository>()
    val audioPlayer = koinInject<com.aichat.data.voice.AudioPlayer>()
    val characterRepository = koinInject<com.aichat.data.character.CharacterRepository>()
    val ttsProviderRegistry = koinInject<com.aichat.data.voice.TtsProviderRegistry>()
    val settingsRepository = koinInject<com.aichat.data.settings.SettingsRepository>()
    val comfyUiRepository = koinInject<com.aichat.data.comfyui.ComfyUiRepository>()
    val imageModelConfigRepository = koinInject<com.aichat.data.model.ImageModelConfigRepository>()
    val toolRegistry = koinInject<com.aichat.data.ai.tool.AiToolRegistry>()

    return androidx.lifecycle.viewmodel.compose.viewModel {
        ChatViewModel(
            sessionId = sessionId,
            characterId = characterId,
            characterRepository = characterRepository,
            aiRepository = aiRepository,
            modelConfigRepository = modelConfigRepository,
            chatSessionRepository = chatSessionRepository,
            audioPlayer = audioPlayer,
            ttsProviderRegistry = ttsProviderRegistry,
            settingsRepository = settingsRepository,
            comfyUiRepository = comfyUiRepository,
            imageModelConfigRepository = imageModelConfigRepository,
            toolRegistry = toolRegistry,
        )
    }
}
