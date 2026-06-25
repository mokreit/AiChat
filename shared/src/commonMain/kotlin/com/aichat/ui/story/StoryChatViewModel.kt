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
    /** Streaming segments parsed in real-time (for story multi-character display) */
    val streamingSegments: List<StorySegment> = emptyList(),
    val error: String? = null,
)

data class StorySegment(
    val senderName: String,
    val content: String,
)

/**
 * Parse AI response into character segments using 【角色名】 format.
 * Matches 【CharacterName】content pairs.
 */
fun parseStorySegments(rawContent: String): List<StorySegment> {
    val results = mutableListOf<StorySegment>()
    val regex = Regex("【(.+?)】([\\s\\S]*?)(?=【.+?】|$)")
    val matches = regex.findAll(rawContent)
    for (match in matches) {
        val text = match.groupValues[2].trim()
        if (text.isNotBlank()) {
            results.add(StorySegment(senderName = match.groupValues[1], content = text))
        }
    }
    return results
}

/**
 * Convert parsed segments into MessageEntity list.
 */
fun segmentsToMessages(segments: List<StorySegment>, sessionId: String, baseTimestamp: Long): List<MessageEntity> {
    return segments.mapIndexed { index, seg ->
        MessageEntity(
            id = "${java.util.UUID.randomUUID()}_$index",
            sessionId = sessionId,
            role = "assistant",
            content = seg.content,
            senderName = seg.senderName,
            timestamp = baseTimestamp + index,
        )
    }
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

            // Build system prompt matching original format
            val sb = mutableListOf<String>()
            if (story.systemPrompt.isNotBlank()) {
                sb.add(story.systemPrompt)
            }

            // Character descriptions in 【角色名】 format
            for (char in characterEntities) {
                val parts = mutableListOf("【${char.name}】")
                if (char.description.isNotBlank()) parts.add("描述：${char.description}")
                parts.add("性格与行为：${char.systemPrompt.ifBlank { char.personality }}")
                sb.add(parts.joinToString("\n"))
            }

            val userNick = story.userNickname.ifBlank { "用户" }
            if (story.userJoined) {
                val userParts = mutableListOf("【${userNick}（用户）】")
                if (story.userDescription.isNotBlank()) {
                    userParts.add("描述：${story.userDescription}")
                } else {
                    userParts.add("用户是故事的参与者，可以自由行动和对话。")
                }
                sb.add(userParts.joinToString("\n"))
            }

            // Output format instructions - character dialogue with inner thoughts
            sb.add(
                """
输出格式要求：
- 严格使用 【角色名】对话内容 的格式输出每个角色的发言
- 每条发言必须以 【角色名】 开头
- 角色可以说话、也可以表达自己的内心想法和感受，但只能写该角色自己的内心活动
- 不要描述其他角色的内心想法或感受，每个角色只写自己
- 不要输出场景描述、环境描写、旁白叙述
- 动作描写简洁融入对话内容中，不要用括号或星号包裹
- 根据剧情自然发展，决定哪些角色需要说话，哪些角色保持沉默
- 不要每次都让所有角色发言，只在剧情需要时让相关角色说话
- 有时一个角色回复就够了，有时多个角色互动更合适，由剧情决定
- 当用户对某个角色说话时，其他角色可以根据场景自然地插话或做出反应，但不是必须的
- 用户的角色是 $userNick，绝对不要代替用户说话
- 不要输出任何不带 【角色名】 标记的文字""".trimIndent()
            )

            _uiState.value = _uiState.value.copy(
                storyTitle = story.title,
                storyDescription = story.description,
                systemPrompt = sb.joinToString("\n"),
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
            streamingSegments = emptyList(),
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
            val segments = parseStorySegments(content)
            if (segments.isNotEmpty()) {
                val msgs = segmentsToMessages(segments, resolvedSessionId, System.currentTimeMillis())
                for (msg in msgs) {
                    chatSessionRepository.insertMessage(msg)
                }
            }
            // If no 【】 format found, discard - we strictly only show character dialogue
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
            // Build history - for story messages with senderName, format as 【senderName】content
            history.addAll(_uiState.value.messages.map {
                if (it.role == "assistant" && it.senderName.isNotBlank()) {
                    AiMessage(role = "assistant", content = "【${it.senderName}】${it.content}")
                } else {
                    AiMessage(role = it.role, content = it.content)
                }
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

                            // Parse segments in real-time for display
                            val segments = parseStorySegments(newContent)

                            _uiState.value = _uiState.value.copy(
                                streamingContent = newContent,
                                streamingSegments = segments,
                            )

                            if (chunk.finishReason == "stop" || chunk.finishReason == "length") {
                                saveStreamedMessages(newContent)
                                _uiState.value = _uiState.value.copy(
                                    isStreaming = false,
                                    streamingContent = "",
                                    streamingSegments = emptyList(),
                                )
                                streamingJob = null
                            }
                        }
                        is ApiResult.HttpError -> {
                            _uiState.value = _uiState.value.copy(
                                isStreaming = false,
                                error = "HTTP ${result.statusCode}: ${result.message}",
                                streamingContent = "",
                                streamingSegments = emptyList(),
                            )
                            streamingJob = null
                        }
                        is ApiResult.NetworkError -> {
                            _uiState.value = _uiState.value.copy(
                                isStreaming = false,
                                error = "网络错误: ${result.message}",
                                streamingContent = "",
                                streamingSegments = emptyList(),
                            )
                            streamingJob = null
                        }
                        is ApiResult.UnexpectedError -> {
                            _uiState.value = _uiState.value.copy(
                                isStreaming = false,
                                error = result.message,
                                streamingContent = "",
                                streamingSegments = emptyList(),
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
                streamingSegments = emptyList(),
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        streamingJob?.cancel()
    }

    private fun generateId(): String = java.util.UUID.randomUUID().toString()
}
