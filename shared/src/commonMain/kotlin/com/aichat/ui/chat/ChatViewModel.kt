package com.aichat.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aichat.data.ai.AiMessage
import com.aichat.data.ai.AiProviderConfig
import com.aichat.data.ai.AiRepository
import com.aichat.data.api.ApiResult
import com.aichat.data.chat.ChatSessionRepository
import com.aichat.data.database.entity.ChatSessionEntity
import com.aichat.data.database.entity.MessageEntity
import com.aichat.data.model.ModelConfigRepository
import com.aichat.data.settings.SettingsRepository
import com.aichat.data.voice.AudioPlayer
import com.aichat.data.voice.TtsConfig
import com.aichat.data.voice.TtsProviderRegistry
import com.aichat.data.voice.TtsRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class MessageUi(
    val id: String,
    val role: String,
    val content: String,
    val senderName: String = "",
    val voiceAttachmentUri: String? = null,
    val voiceDurationMs: Long? = null,
)

data class ChatUiState(
    val characterName: String = "",
    val characterFirstMessage: String = "",
    val systemPrompt: String = "",
    val modelConfigId: String = "",
    val ttsProviderId: String = "",
    val voiceId: String = "",
    val voiceApiEndpoint: String = "",
    val voiceApiKey: String = "",
    val voiceModel: String = "",
    val voiceDesignPrompt: String = "",
    val messages: List<MessageUi> = emptyList(),
    val inputText: String = "",
    val isStreaming: Boolean = false,
    val streamingContent: String = "",
    val playingMessageId: String? = null,
    val synthesizingMessageId: String? = null,
    val error: String? = null,
)

class ChatViewModel(
    private val sessionId: String,
    private val characterId: String,
    private val characterRepository: com.aichat.data.character.CharacterRepository,
    private val aiRepository: AiRepository,
    private val modelConfigRepository: ModelConfigRepository,
    private val chatSessionRepository: ChatSessionRepository,
    private val audioPlayer: AudioPlayer,
    private val ttsProviderRegistry: TtsProviderRegistry,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var streamingJob: Job? = null
    private var resolvedSessionId: String = sessionId

    init {
        loadCharacterInfo()
        resolveSessionAndLoadMessages()
    }

    private fun loadCharacterInfo() {
        viewModelScope.launch {
            val char = characterRepository.getCharacterById(characterId)
            if (char != null) {
                val prompt = buildSystemPrompt(char)
                _uiState.value = _uiState.value.copy(
                    characterName = char.name,
                    characterFirstMessage = char.firstMessage,
                    systemPrompt = prompt,
                    modelConfigId = char.modelConfigId,
                    ttsProviderId = char.ttsProviderId,
                    voiceId = char.voiceId,
                    voiceApiEndpoint = char.voiceApiEndpoint,
                    voiceApiKey = char.voiceApiKey,
                    voiceModel = char.voiceModel,
                    voiceDesignPrompt = char.voiceDesignPrompt,
                )
            } else {
                _uiState.value = _uiState.value.copy(characterName = "Chat")
            }
        }
    }

    private fun buildSystemPrompt(char: com.aichat.data.database.entity.CharacterEntity): String {
        val parts = mutableListOf<String>()
        parts.add("You are ${char.name}.")
        if (char.description.isNotBlank()) {
            parts.add("Description: ${char.description}")
        }
        if (char.personality.isNotBlank()) {
            parts.add("Personality: ${char.personality}")
        }
        if (char.scenario.isNotBlank()) {
            parts.add("Scenario: ${char.scenario}")
        }
        if (char.systemPrompt.isNotBlank()) {
            parts.add(char.systemPrompt)
        }
        return parts.joinToString("\n\n")
    }

    private fun resolveSessionAndLoadMessages() {
        viewModelScope.launch {
            // If no sessionId provided, find or create one for this character
            if (resolvedSessionId.isBlank()) {
                val existingSessions = chatSessionRepository.getSessionsByCharacter(characterId)
                    .firstOrNull()
                if (existingSessions != null && existingSessions.isNotEmpty()) {
                    resolvedSessionId = existingSessions.first().id
                } else {
                    resolvedSessionId = generateId()
                }
            }

            var session = chatSessionRepository.getSessionById(resolvedSessionId)
            if (session == null) {
                val currentCharacterName = _uiState.value.characterName.ifBlank { "Chat" }
                session = ChatSessionEntity(
                    id = resolvedSessionId,
                    characterId = characterId,
                    characterName = currentCharacterName,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                )
                chatSessionRepository.insertSession(session)
            }

            chatSessionRepository.getMessagesBySession(resolvedSessionId).collect { entities ->
                _uiState.value = _uiState.value.copy(
                    messages = entities.map { it.toUi() },
                )
            }
        }
    }

    fun updateInput(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank() || _uiState.value.isStreaming) return

        _uiState.value = _uiState.value.copy(inputText = "", isStreaming = true, error = null)

        val userMessage = MessageEntity(
            id = generateId(),
            sessionId = resolvedSessionId,
            role = "user",
            content = text,
            senderName = "",
            timestamp = System.currentTimeMillis(),
        )

        viewModelScope.launch {
            chatSessionRepository.insertMessage(userMessage)
            updateSessionMeta(text)
            callAi()
        }
    }

    fun stopGenerating() {
        streamingJob?.cancel()
        streamingJob = null
        val streamingContent = _uiState.value.streamingContent
        if (streamingContent.isNotBlank()) {
            viewModelScope.launch {
                val assistantMessage = MessageEntity(
                    id = generateId(),
                    sessionId = resolvedSessionId,
                    role = "assistant",
                    content = streamingContent,
                    senderName = _uiState.value.characterName,
                    timestamp = System.currentTimeMillis(),
                )
                chatSessionRepository.insertMessage(assistantMessage)
                updateSessionMeta(streamingContent)
            }
        }
        _uiState.value = _uiState.value.copy(isStreaming = false, streamingContent = "")
    }

    fun regenerateMessage(messageId: String) {
        val messages = _uiState.value.messages
        val idx = messages.indexOfFirst { it.id == messageId }
        if (idx == -1) return

        // Remove this message and all after it from DB
        viewModelScope.launch {
            val toRemove = messages.subList(idx, messages.size)
            for (msg in toRemove) {
                chatSessionRepository.deleteMessage(msg.id)
            }
            // Re-call AI with remaining context
            _uiState.value = _uiState.value.copy(isStreaming = true, error = null)
            callAi()
        }
    }

    fun rewindToMessage(messageId: String) {
        val messages = _uiState.value.messages
        val idx = messages.indexOfFirst { it.id == messageId }
        if (idx == -1) return

        viewModelScope.launch {
            // Delete this message and all after it
            val toRemove = messages.subList(idx, messages.size)
            for (msg in toRemove) {
                chatSessionRepository.deleteMessage(msg.id)
            }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            chatSessionRepository.deleteMessage(messageId)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            chatSessionRepository.deleteMessagesBySession(resolvedSessionId)
        }
    }

    private suspend fun callAi() {
        try {
            val configId = _uiState.value.modelConfigId
            val config = if (configId.isNotBlank()) {
                modelConfigRepository.getConfigById(configId)
            } else {
                modelConfigRepository.getDefaultConfig()
            }
            if (config == null) {
                _uiState.value = _uiState.value.copy(
                    isStreaming = false,
                    error = "No model config found. Please configure in Settings.",
                )
                return
            }

            val providerConfig = AiProviderConfig(
                providerId = config.provider,
                apiHost = config.baseUrl,
                apiKey = config.apiKey,
                model = config.modelName,
            )

            val history = mutableListOf<AiMessage>()
            val systemPrompt = _uiState.value.systemPrompt
            if (systemPrompt.isNotBlank()) {
                history.add(AiMessage(role = "system", content = systemPrompt))
            }
            history.addAll(_uiState.value.messages.map { msg ->
                AiMessage(role = msg.role, content = msg.content)
            })

            streamingJob = viewModelScope.launch {
                aiRepository.createStreamingCompletion(
                    config = providerConfig,
                    messages = history,
                ).collect { result ->
                    when (result) {
                        is com.aichat.data.api.ApiResult.Success -> {
                            val chunk = result.value
                            val content = chunk.contentDelta
                            val current = _uiState.value.streamingContent
                            _uiState.value = _uiState.value.copy(streamingContent = current + content)

                            if (chunk.finishReason == "stop" || chunk.finishReason == "length") {
                                val fullContent = _uiState.value.streamingContent
                                val assistantMessage = MessageEntity(
                                    id = generateId(),
                                    sessionId = resolvedSessionId,
                                    role = "assistant",
                                    content = fullContent,
                                    senderName = _uiState.value.characterName,
                                    timestamp = System.currentTimeMillis(),
                                )
                                chatSessionRepository.insertMessage(assistantMessage)
                                updateSessionMeta(fullContent)
                                _uiState.value = _uiState.value.copy(
                                    isStreaming = false,
                                    streamingContent = "",
                                )
                                streamingJob = null
                            }
                        }
                        is com.aichat.data.api.ApiResult.HttpError -> {
                            _uiState.value = _uiState.value.copy(
                                isStreaming = false,
                                error = "HTTP ${result.statusCode}: ${result.message}",
                                streamingContent = "",
                            )
                            streamingJob = null
                        }
                        is com.aichat.data.api.ApiResult.NetworkError -> {
                            _uiState.value = _uiState.value.copy(
                                isStreaming = false,
                                error = "Network error: ${result.message}",
                                streamingContent = "",
                            )
                            streamingJob = null
                        }
                        is com.aichat.data.api.ApiResult.UnexpectedError -> {
                            _uiState.value = _uiState.value.copy(
                                isStreaming = false,
                                error = result.message,
                                streamingContent = "",
                            )
                            streamingJob = null
                        }
                    }
                }
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isStreaming = false,
                error = e.message ?: "Unknown error",
                streamingContent = "",
            )
        }
    }

    private suspend fun updateSessionMeta(lastMessage: String) {
        val existing = chatSessionRepository.getSessionById(resolvedSessionId)
        val currentCharacterName = _uiState.value.characterName
        chatSessionRepository.updateSession(
            ChatSessionEntity(
                id = resolvedSessionId,
                characterId = characterId,
                characterName = currentCharacterName,
                lastMessage = lastMessage,
                lastTime = System.currentTimeMillis(),
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    fun playVoice(messageId: String, voiceUri: String?) {
        viewModelScope.launch {
            // If message already has voice, play it directly
            if (voiceUri != null) {
                _uiState.value = _uiState.value.copy(playingMessageId = messageId)
                audioPlayer.playFromUrl(voiceUri)
                _uiState.value = _uiState.value.copy(playingMessageId = null)
                return@launch
            }

            // Otherwise, synthesize on demand
            val state = _uiState.value
            val message = state.messages.find { it.id == messageId }
            if (message == null || message.role != "assistant") return@launch

            val providerId = state.ttsProviderId.ifBlank {
                settingsRepository.defaultTtsProviderId.firstOrNull() ?: "openai-compat-tts"
            }
            val provider = ttsProviderRegistry.find(providerId)
            if (provider == null) {
                _uiState.value = _uiState.value.copy(error = "TTS provider not found: $providerId")
                return@launch
            }

            val voiceId = state.voiceId.ifBlank {
                // Fallback: use first word of voiceDesignPrompt if it looks like a voice ID
                val design = state.voiceDesignPrompt.trim()
                val knownVoices = listOf("alloy","ash","coral","echo","fable","onyx","nova","sage","shimmer")
                val match = knownVoices.firstOrNull { design.lowercase().contains(it) }
                match ?: "alloy"
            }
            val apiHost = state.voiceApiEndpoint.ifBlank {
                settingsRepository.voiceApiHost.firstOrNull() ?: ""
            }
            val apiKey = state.voiceApiKey.ifBlank {
                settingsRepository.voiceApiKey.firstOrNull() ?: ""
            }
            val model = state.voiceModel.ifBlank {
                settingsRepository.voiceModel.firstOrNull() ?: "tts-1"
            }

            // Strip action/meta text, only read dialogue for TTS
            val ttsText = com.aichat.design.stripActionText(message.content)
            if (ttsText.isBlank()) {
                _uiState.value = _uiState.value.copy(error = "No dialogue text to read")
                return@launch
            }

            val config = TtsConfig(
                providerId = providerId,
                apiHost = apiHost,
                apiKey = apiKey,
                voiceId = voiceId,
                model = model,
            )
            val request = TtsRequest(
                text = ttsText,
                voiceId = voiceId,
                model = model,
            )

            _uiState.value = _uiState.value.copy(synthesizingMessageId = messageId)
            when (val result = provider.synthesize(config, request)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        synthesizingMessageId = null,
                        playingMessageId = messageId,
                    )
                    audioPlayer.play(result.value.audioData, result.value.format)
                    _uiState.value = _uiState.value.copy(playingMessageId = null)
                }
                else -> {
                    val errorMsg = when (val r = result) {
                        is com.aichat.data.api.ApiResult.HttpError -> "TTS HTTP ${r.statusCode}: ${r.message}"
                        is com.aichat.data.api.ApiResult.NetworkError -> "TTS Network: ${r.message}"
                        is com.aichat.data.api.ApiResult.UnexpectedError -> "TTS Error: ${r.message}"
                        else -> "Voice synthesis failed"
                    }
                    _uiState.value = _uiState.value.copy(
                        synthesizingMessageId = null,
                        error = errorMsg,
                    )
                }
            }
        }
    }

    fun stopVoice() {
        viewModelScope.launch {
            audioPlayer.stop()
            _uiState.value = _uiState.value.copy(playingMessageId = null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        streamingJob?.cancel()
    }

    private fun generateId(): String = java.util.UUID.randomUUID().toString()
}

private fun MessageEntity.toUi() = MessageUi(
    id = id,
    role = role,
    content = content,
    senderName = senderName,
    voiceAttachmentUri = voiceUri.ifBlank { null },
)
