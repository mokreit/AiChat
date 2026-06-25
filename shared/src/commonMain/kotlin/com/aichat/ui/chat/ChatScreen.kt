package com.aichat.ui.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.aichat.data.character.CharacterRepository
import com.aichat.design.AiChatTypography
import com.aichat.design.ChatBubble
import com.aichat.design.ChatInputField
import com.aichat.design.strings
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
    viewModel: ChatViewModel = rememberChatViewModel(sessionId, characterId),
    characterRepository: CharacterRepository = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val s = strings()
    val scope = rememberCoroutineScope()
    var bgImageUrl by remember { mutableStateOf("") }
    var characterAvatarUri by remember { mutableStateOf("") }

    val settingsRepository = koinInject<com.aichat.data.settings.SettingsRepository>()
    val userAvatarUri by settingsRepository.avatar.collectAsState(initial = "")

    LaunchedEffect(characterId) {
        val char = characterRepository.getCharacterById(characterId)
        bgImageUrl = char?.backgroundImage ?: ""
        characterAvatarUri = char?.avatarUri ?: ""
    }

    var selectedMessage by remember { mutableStateOf<MessageUi?>(null) }
    var showChatMenu by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var exportSuccess by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(uiState.messages.size, uiState.streamingContent) {
        val totalItems = uiState.messages.size +
            (if (uiState.characterFirstMessage.isNotBlank() && uiState.messages.isEmpty()) 1 else 0) +
            (if (uiState.isStreaming && uiState.streamingContent.isNotBlank()) 1 else 0)
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
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
                        alpha = 0.3f,
                    )
                }
                // Semi-transparent overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.35f)
                        .background(MaterialTheme.colorScheme.surface),
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                // Character first message (when no messages yet)
                if (uiState.characterFirstMessage.isNotBlank() && uiState.messages.isEmpty()) {
                    item(key = "__first_message__") {
                        ChatBubble(
                            text = uiState.characterFirstMessage,
                            isUser = false,
                            avatarName = uiState.characterName,
                            avatarUri = characterAvatarUri,
                            userAvatarUri = userAvatarUri ?: "",
                            modifier = Modifier.combinedClickable(
                                onClick = {},
                                onLongClick = { selectedMessage = MessageUi(id = "__first_message__", role = "assistant", content = uiState.characterFirstMessage) },
                            ),
                        )
                    }
                }

                items(uiState.messages, key = { it.id }) { message ->
                    ChatBubble(
                        text = message.content,
                        isUser = message.role == "user",
                        isPlaying = uiState.playingMessageId == message.id,
                        isSynthesizing = uiState.synthesizingMessageId == message.id,
                        onPlayClick = if (message.role == "assistant") {
                            { viewModel.playVoice(message.id, message.voiceAttachmentUri) }
                        } else null,
                        avatarName = if (message.role == "user") "" else uiState.characterName,
                        avatarUri = if (message.role == "user") "" else characterAvatarUri,
                        userAvatarUri = userAvatarUri ?: "",
                        modifier = Modifier.combinedClickable(
                            onClick = {},
                            onLongClick = { selectedMessage = message },
                        ),
                    )
                }

                // Streaming content
                if (uiState.isStreaming && uiState.streamingContent.isNotBlank()) {
                    item(key = "streaming") {
                        ChatBubble(
                            text = uiState.streamingContent,
                            isUser = false,
                        )
                    }
                }

                // Loading indicator
                if (uiState.isStreaming && uiState.streamingContent.isBlank()) {
                    item(key = "loading") {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
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

            ChatInputField(
                value = uiState.inputText,
                onValueChange = viewModel::updateInput,
                onSend = viewModel::sendMessage,
                isSending = uiState.isStreaming,
                onStop = if (uiState.isStreaming) viewModel::stopGenerating else null,
                modifier = Modifier.padding(12.dp),
            )
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
        )
    }
}
