package com.aichat.ui.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aichat.data.ai.AiMessage
import com.aichat.data.ai.AiProviderConfig
import com.aichat.data.ai.AiRepository
import com.aichat.data.api.ApiResult
import com.aichat.data.chat.ChatSessionRepository
import com.aichat.data.character.CharacterRepository
import com.aichat.data.database.entity.CharacterEntity
import com.aichat.data.database.entity.ChatSessionEntity
import com.aichat.data.database.entity.MessageEntity
import com.aichat.data.database.entity.StoryEntity
import com.aichat.data.model.ModelConfigRepository
import com.aichat.data.settings.SettingsRepository
import com.aichat.data.story.StoryRepository
import com.aichat.ui.chat.MessageUi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/** Info about a character in the story, used for avatar display */
data class StoryCharacterInfo(
    val name: String,
    val avatarUri: String,
    val characterId: String,
)

data class StoryChatUiState(
    val storyTitle: String = "",
    val storyDescription: String = "",
    val systemPrompt: String = "",
    val backgroundImage: String = "",
    val userNickname: String = "",
    val userJoined: Boolean = true,
    val characters: List<StoryCharacterInfo> = emptyList(),
    val messages: List<MessageUi> = emptyList(),
    val inputText: String = "",
    val isStreaming: Boolean = false,
    val streamingContent: String = "",
    val streamingSenderName: String = "",  // which character is currently speaking
    val error: String? = null,
)

/**
 * Parse AI response into individual character messages.
 * Format expected: [CharacterName]: "dialogue" or [CharacterName]: dialogue
 * Also supports *action* as narrator text.
 */
fun parseStoryMessages(rawContent: String, sessionId: String, baseTimestamp: Long): List<MessageEntity> {
    val messages = mutableListOf<MessageEntity>()
    val lines = rawContent.split("\n").filter { it.isNotBlank() }
    var msgIndex = 0

    for (line in lines) {
        val trimmed = line.trim()
        // Match pattern: CharacterName: "content" or CharacterName: content
        val nameColonMatch = Regex("^([\\p{L}\\p{N}_\\s]+?)[:：]\\s*(.+)").find(trimmed)
        if (nameColonMatch != null) {
            val name = nameColonMatch.groupValues[1].trim()
            var content = nameColonMatch.groupValues[2].trim()
            // Strip surrounding quotes
            if ((content.startsWith("\"") && content.endsWith("\"")) ||
                (content.startsWith("\u201C") && content.endsWith("\u201D")) ||
                (content.startsWith("「") && content.endsWith("」"))
            ) {
                content = content.substring(1, content.length - 1).trim()
            }
            if (content.isNotBlank()) {
                messages.add(
                    MessageEntity(
                        id = "${java.util.UUID.randomUUID()}_$msgIndex",
                        sessionId = sessionId,
                        role = "assistant",
                        content = content,
                        senderName = name,
                        timestamp = baseTimestamp + msgIndex,
                    )
                )
                msgIndex++
            }
        } else if (trimmed.startsWith("*") && trimmed.endsWith("*") && trimmed.length > 2) {
            // Narrator action: *something happened*
            messages.add(
                MessageEntity(
                    id = "${java.util.UUID.randomUUID()}_$msgIndex",
                    sessionId = sessionId,
                    role = "assistant",
                    content = trimmed,
                    senderName = "",
                    timestamp = baseTimestamp + msgIndex,
                )
            )
            msgIndex++
        } else if (trimmed.isNotBlank()) {
            // Narrator text
            messages.add(
                MessageEntity(
                    id = "${java.util.UUID.randomUUID()}_$msgIndex",
                    sessionId = sessionId,
                    role = "assistant",
                    content = trimmed,
                    senderName = "",
                    timestamp = baseTimestamp + msgIndex,
                )
            )
            msgIndex++
        }
    }
    return messages
}

class StoryChatViewModel(
    private val storyId: String,
    private val storyRepository: StoryRepository,
    private val characterRepository: CharacterRepository,
    private val aiRepository: AiRepository,
    private val modelConfigRepository: ModelConfigRepository,
    private val chatSessionRepository: ChatSessionRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoryChatUiState())
    val uiState: StateFlow<StoryChatUiState> = _uiState.asStateFlow()

    private var streamingJob: Job? = null
    private var resolvedSessionId: String = ""
    private var modelConfigId: String = ""

    init {
        loadStory()
    }

    private fun loadStory() {
        viewModelScope.launch {
            val story = storyRepository.getStoryById(storyId) ?: return@launch
            resolvedSessionId = "story_$storyId"

            // Load characters
            val charIds = story.characterIds.split(",").map { it.trim() }.filter { it.isNotBlank() }
            val characterInfos = mutableListOf<StoryCharacterInfo>()
            val characterEntities = mutableListOf<CharacterEntity>()

            for (cid in charIds) {
                val char = characterRepository.getCharacterById(cid)
                if (char != null) {
                    characterEntities.add(char)
                    characterInfos.add(
                        StoryCharacterInfo(
                            name = char.name,
                            avatarUri = char.avatarUri,
                            characterId = char.id,
                        )
                    )
                    if (modelConfigId.isBlank() && char.modelConfigId.isNotBlank()) {
                        modelConfigId = char.modelConfigId
                    }
                }
            }

            // Build system prompt for multi-character turn-based dialogue
            val sb = mutableListOf<String>()
            if (story.systemPrompt.isNotBlank()) {
                sb.add(story.systemPrompt)
            }
            if (story.description.isNotBlank()) {
                sb.add("故事背景: ${story.description}")
            }

            // Character descriptions
            sb.add("=== 角色列表 ===")
            for (char in characterEntities) {
                val parts = mutableListOf("${char.name}:")
                if (char.description.isNotBlank()) parts.add("描述: ${char.description}")
                if (char.personality.isNotBlank()) parts.add("性格: ${char.personality}")
                if (char.scenario.isNotBlank()) parts.add("场景: ${char.scenario}")
                if (char.systemPrompt.isNotBlank()) parts.add(char.systemPrompt)
                sb.add(parts.joinToString(" "))
            }

            val userNick = story.userNickname.ifBlank { "我" }
            if (story.userJoined) {
                val userParts = mutableListOf("用户扮演的角色名为: $userNick")
                if (story.userDescription.isNotBlank()) {
                    userParts.add("用户角色描述: ${story.userDescription}")
                }
                sb.add(userParts.joinToString(" "))
            }

            sb.add(
                """=== 对话规则 ===
你是一个互动故事叙述者。请按以下规则生成回复：
1. 每个角色依次说话，格式为：角色名: "对话内容"
2. 角色之间轮流发言，每个角色说一句话或一段话
3. 每个角色说话要符合自己的性格和设定
4. 场景描述和动作用 *动作描述* 的格式
5. 每次回复包含 2-4 个角色的发言
6. 用户角色 ($userNick) 的发言由用户自己输入，你不要代替用户说话
7. 只生成NPC角色的对话，不要生成 $userNick 的对话
8. 对话要自然流畅，推动故事发展"""
            )

            _uiState.value = _uiState.value.copy(
                storyTitle = story.title,
                storyDescription = story.description,
                systemPrompt = sb.joinToString("\n\n"),
                backgroundImage = story.backgroundImage,
                userNickname = userNick,
                userJoined = story.userJoined,
                characters = characterInfos,
            )

            // Ensure session exists
            var session = chatSessionRepository.getSessionById(resolvedSessionId)
            if (session == null) {
                session = ChatSessionEntity(
                    id = resolvedSessionId,
                    characterId = storyId,
                    characterName = story.title,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                )
                chatSessionRepository.insertSession(session)
            }

            // Load messages
            chatSessionRepository.getMessagesBySession(resolvedSessionId).collect { entities ->
                _uiState.value = _uiState.value.copy(
                    messages = entities.map {
                        MessageUi(
                            id = it.id,
                            role = it.role,
                            content = it.content,
                            senderName = it.senderName,
                            voiceAttachmentUri = it.voiceUri.ifBlank { null },
                        )
                    },
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
            senderName = _uiState.value.userNickname,
            timestamp = System.currentTimeMillis(),
        )

        viewModelScope.launch {
            chatSessionRepository.insertMessage(userMessage)
            callAi()
        }
    }

    fun stopGenerating() {
        streamingJob?.cancel()
        streamingJob = null
        val streamingContent = _uiState.value.streamingContent
        if (streamingContent.isNotBlank()) {
            saveStreamedMessages(streamingContent)
        }
        _uiState.value = _uiState.value.copy(
            isStreaming = false,
            streamingContent = "",
            streamingSenderName = "",
        )
    }

    fun clearChat() {
        viewModelScope.launch {
            chatSessionRepository.deleteMessagesBySession(resolvedSessionId)
        }
    }

    /** Get avatar URI for a sender name by matching to character list */
    fun getAvatarForSender(senderName: String): String {
        if (senderName.isBlank()) return ""
        return _uiState.value.characters.find {
            it.name.equals(senderName, ignoreCase = true)
        }?.avatarUri ?: ""
    }

    private fun saveStreamedMessages(content: String) {
        viewModelScope.launch {
            val parsed = parseStoryMessages(content, resolvedSessionId, System.currentTimeMillis())
            if (parsed.isNotEmpty()) {
                for (msg in parsed) {
                    chatSessionRepository.insertMessage(msg)
                }
            } else {
                // Fallback: save as single narrator message
                val msg = MessageEntity(
                    id = generateId(),
                    sessionId = resolvedSessionId,
                    role = "assistant",
                    content = content,
                    senderName = "",
                    timestamp = System.currentTimeMillis(),
                )
                chatSessionRepository.insertMessage(msg)
            }
        }
    }

    private suspend fun callAi() {
        try {
            val config = if (modelConfigId.isNotBlank()) {
                modelConfigRepository.getConfigById(modelConfigId)
            } else {
                modelConfigRepository.getDefaultConfig()
            }
            if (config == null) {
                _uiState.value = _uiState.value.copy(
                    isStreaming = false,
                    error = "未找到模型配置，请在设置中配置。",
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
            val sys = _uiState.value.systemPrompt
            if (sys.isNotBlank()) {
                history.add(AiMessage(role = "system", content = sys))
            }
            history.addAll(_uiState.value.messages.map {
                AiMessage(role = it.role, content = it.content)
            })

            streamingJob = viewModelScope.launch {
                aiRepository.createStreamingCompletion(
                    config = providerConfig,
                    messages = history,
                ).collect { result ->
                    when (result) {
                        is ApiResult.Success -> {
                            val chunk = result.value
                            val current = _uiState.value.streamingContent
                            val newContent = current + chunk.contentDelta

                            // Track which character is currently speaking (last "Name:" seen)
                            val currentSpeaker = detectCurrentSpeaker(newContent)

                            _uiState.value = _uiState.value.copy(
                                streamingContent = newContent,
                                streamingSenderName = currentSpeaker,
                            )

                            if (chunk.finishReason == "stop" || chunk.finishReason == "length") {
                                saveStreamedMessages(newContent)
                                _uiState.value = _uiState.value.copy(
                                    isStreaming = false,
                                    streamingContent = "",
                                    streamingSenderName = "",
                                )
                                streamingJob = null
                            }
                        }
                        is ApiResult.HttpError -> {
                            _uiState.value = _uiState.value.copy(
                                isStreaming = false,
                                error = "HTTP ${result.statusCode}: ${result.message}",
                                streamingContent = "",
                                streamingSenderName = "",
                            )
                            streamingJob = null
                        }
                        is ApiResult.NetworkError -> {
                            _uiState.value = _uiState.value.copy(
                                isStreaming = false,
                                error = "网络错误: ${result.message}",
                                streamingContent = "",
                                streamingSenderName = "",
                            )
                            streamingJob = null
                        }
                        is ApiResult.UnexpectedError -> {
                            _uiState.value = _uiState.value.copy(
                                isStreaming = false,
                                error = result.message,
                                streamingContent = "",
                                streamingSenderName = "",
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
                streamingSenderName = "",
            )
        }
    }

    /** Detect which character name is the last speaker in the streaming content */
    private fun detectCurrentSpeaker(content: String): String {
        val matches = Regex("([\\p{L}\\p{N}_\\s]+?)[:：]\\s*[\"\\u201C「*]").findAll(content)
        return matches.lastOrNull()?.groupValues?.getOrNull(1)?.trim() ?: ""
    }

    override fun onCleared() {
        super.onCleared()
        streamingJob?.cancel()
    }

    private fun generateId(): String = java.util.UUID.randomUUID().toString()
}
