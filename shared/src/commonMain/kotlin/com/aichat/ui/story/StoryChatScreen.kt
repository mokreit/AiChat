package com.aichat.ui.story

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.aichat.design.AiChatTypography
import com.aichat.design.ChatBubble
import com.aichat.design.ChatInputField
import com.aichat.design.strings
import com.aichat.platform.loadImageFromFile
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StoryChatScreen(
    storyId: String,
    onBack: () -> Unit,
    viewModel: StoryChatViewModel = rememberStoryChatViewModel(storyId),
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val s = strings()

    LaunchedEffect(uiState.messages.size, uiState.streamingContent) {
        val total = uiState.messages.size +
            (if (uiState.isStreaming && uiState.streamingContent.isNotBlank()) 1 else 0)
        if (total > 0) listState.animateScrollToItem(total - 1)
    }

    var showClearConfirm by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(s.clearChat) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearChat(); showClearConfirm = false }) { Text(s.confirm) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text(s.cancel) }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.storyTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(s.clearChat) },
                            onClick = { showClearConfirm = true; showMenu = false },
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            // Background image
            if (uiState.backgroundImage.isNotBlank()) {
                var bgBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
                LaunchedEffect(uiState.backgroundImage) {
                    try { bgBitmap = loadImageFromFile(uiState.backgroundImage) } catch (_: Exception) {}
                }
                bgBitmap?.let { bmp ->
                    Image(
                        bitmap = bmp,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.3f,
                    )
                }
                Box(
                    modifier = Modifier.fillMaxSize().alpha(0.35f).background(MaterialTheme.colorScheme.surface),
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    // Story description as intro
                    if (uiState.storyDescription.isNotBlank() && uiState.messages.isEmpty() && !uiState.isStreaming) {
                        item(key = "intro") {
                            Text(
                                text = uiState.storyDescription,
                                style = AiChatTypography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                    }

                    // Messages with character avatars
                    items(uiState.messages, key = { it.id }) { message ->
                        val isUser = message.role == "user"
                        val avatarUri = if (isUser) "" else viewModel.getAvatarForSender(message.senderName)
                        val displayName = if (isUser) {
                            message.senderName.ifBlank { uiState.userNickname }
                        } else {
                            message.senderName.ifBlank { "旁白" }
                        }

                        ChatBubble(
                            text = message.content,
                            isUser = isUser,
                            avatarName = if (isUser) "" else displayName,
                            avatarUri = avatarUri,
                        )
                    }

                    // Streaming content with current speaker's avatar
                    if (uiState.isStreaming && uiState.streamingContent.isNotBlank()) {
                        item(key = "streaming") {
                            val streamAvatarUri = viewModel.getAvatarForSender(uiState.streamingSenderName)
                            val streamDisplayName = uiState.streamingSenderName.ifBlank { "旁白" }
                            ChatBubble(
                                text = uiState.streamingContent,
                                isUser = false,
                                avatarName = streamDisplayName,
                                avatarUri = streamAvatarUri,
                            )
                        }
                    }

                    // Loading indicator
                    if (uiState.isStreaming && uiState.streamingContent.isBlank()) {
                        item(key = "loading") {
                            ChatBubble(
                                text = "...",
                                isUser = false,
                                avatarName = uiState.characters.firstOrNull()?.name ?: "旁白",
                                avatarUri = uiState.characters.firstOrNull()?.avatarUri ?: "",
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
private fun rememberStoryChatViewModel(storyId: String): StoryChatViewModel {
    val storyRepository = koinInject<com.aichat.data.story.StoryRepository>()
    val characterRepository = koinInject<com.aichat.data.character.CharacterRepository>()
    val aiRepository = koinInject<com.aichat.data.ai.AiRepository>()
    val modelConfigRepository = koinInject<com.aichat.data.model.ModelConfigRepository>()
    val chatSessionRepository = koinInject<com.aichat.data.chat.ChatSessionRepository>()
    val settingsRepository = koinInject<com.aichat.data.settings.SettingsRepository>()

    return androidx.lifecycle.viewmodel.compose.viewModel {
        StoryChatViewModel(
            storyId = storyId,
            storyRepository = storyRepository,
            characterRepository = characterRepository,
            aiRepository = aiRepository,
            modelConfigRepository = modelConfigRepository,
            chatSessionRepository = chatSessionRepository,
            settingsRepository = settingsRepository,
        )
    }
}
