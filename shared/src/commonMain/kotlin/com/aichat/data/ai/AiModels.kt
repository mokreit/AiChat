package com.aichat.data.ai

import com.aichat.data.api.ApiResult
import kotlinx.serialization.json.JsonObject

data class AiProviderConfig(
    val providerId: String,
    val apiHost: String,
    val apiKey: String,
    val model: String,
)

data class AiModelDiscovery(
    val models: List<String>,
    val resolvedApiHost: String,
)

data class AiChatRequest(
    val model: String,
    val messages: List<AiMessage>,
    val temperature: Double? = null,
    val topP: Double? = null,
    val maxTokens: Int? = null,
    val tools: List<AiToolDefinition> = emptyList(),
    val toolChoice: ToolChoice = ToolChoice.None,
    val responseFormat: AiResponseFormat? = null,
)

data class AiMessage(
    val role: String,
    val content: String? = null,
    val toolCalls: List<AiToolCall> = emptyList(),
    val toolCallId: String? = null,
    val contentParts: List<AiContentPart>? = null,
)

data class AiCompletion(
    val message: AiMessage,
    val finishReason: String? = null,
    val usage: AiUsage = AiUsage(),
    val structuredOutput: JsonObject? = null,
)

data class AiCompletionChunk(
    val contentDelta: String = "",
    val finishReason: String? = null,
    val usage: AiUsage = AiUsage(),
)

data class AiUsage(
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = 0,
) {
    operator fun plus(other: AiUsage) = AiUsage(
        promptTokens = promptTokens + other.promptTokens,
        completionTokens = completionTokens + other.completionTokens,
        totalTokens = totalTokens + other.totalTokens,
    )
}

data class AiToolCall(
    val id: String,
    val name: String,
    val arguments: String,
)

data class AiToolDefinition(
    val name: String,
    val description: String,
    val parameters: JsonObject,
    val strict: Boolean = true,
)

data class AiResponseFormat(
    val name: String,
    val description: String? = null,
    val schema: JsonObject,
    val strict: Boolean = true,
    val type: AiResponseFormatType = AiResponseFormatType.JsonSchema,
)

enum class AiResponseFormatType {
    JsonSchema,
    JsonObject,
}

sealed interface ToolChoice {
    data object None : ToolChoice
    data object Auto : ToolChoice
    data object Required : ToolChoice
    data class Specific(val name: String) : ToolChoice
}

sealed interface AiContentPart {
    data class Text(val text: String) : AiContentPart
    data class ImageUrl(val url: String, val detail: String? = null) : AiContentPart
    data class FileUrl(val url: String, val mimeType: String) : AiContentPart
}

interface AiProvider {
    val id: String

    suspend fun getModels(config: AiProviderConfig): ApiResult<List<String>>

    suspend fun discoverModels(config: AiProviderConfig): ApiResult<AiModelDiscovery> {
        return when (val result = getModels(config)) {
            is ApiResult.Success -> ApiResult.Success(
                AiModelDiscovery(
                    models = result.value,
                    resolvedApiHost = config.apiHost,
                ),
            )
            is ApiResult.HttpError -> result
            is ApiResult.NetworkError -> result
            is ApiResult.UnexpectedError -> result
        }
    }

    suspend fun complete(
        config: AiProviderConfig,
        request: AiChatRequest,
    ): ApiResult<AiCompletion>

    fun completeStreaming(
        config: AiProviderConfig,
        request: AiChatRequest,
    ): kotlinx.coroutines.flow.Flow<ApiResult<AiCompletionChunk>>

    suspend fun supportsToolCalls(config: AiProviderConfig): Boolean
}

class AiProviderRegistry(
    providers: List<AiProvider>,
    aliases: Map<String, String> = mapOf(
        "custom" to OpenAiCompatibleProvider.Id,
        "openai" to OpenAiCompatibleProvider.Id,
    ),
) {
    private val providersById = providers.associateBy(AiProvider::id)
    private val aliasesById = aliases

    fun find(providerId: String): AiProvider? {
        return providersById[providerId]
            ?: aliasesById[providerId]?.let(providersById::get)
    }
}
