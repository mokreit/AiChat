package com.aichat.data.ai

import com.aichat.data.api.ApiResult
import com.aichat.data.model.ModelConfigRepository
import kotlinx.coroutines.flow.Flow

class AiRepository(
    private val providerRegistry: AiProviderRegistry,
    private val modelConfigRepository: ModelConfigRepository,
) {

    suspend fun discoverModels(
        apiHost: String,
        apiKey: String,
        providerId: String,
    ): ApiResult<AiModelDiscovery> {
        val provider = providerRegistry.find(providerId)
            ?: return ApiResult.UnexpectedError("Unknown provider: $providerId")
        val config = AiProviderConfig(
            providerId = providerId,
            apiHost = apiHost,
            apiKey = apiKey,
            model = "",
        )
        return provider.discoverModels(config)
    }

    suspend fun createChatCompletion(
        config: AiProviderConfig,
        messages: List<AiMessage>,
        tools: List<AiToolDefinition> = emptyList(),
        toolChoice: ToolChoice = ToolChoice.None,
    ): ApiResult<AiCompletion> {
        val provider = providerRegistry.find(config.providerId)
            ?: return ApiResult.UnexpectedError("Unknown provider: ${config.providerId}")
        val request = AiChatRequest(
            model = config.model,
            messages = messages,
            tools = tools,
            toolChoice = toolChoice,
        )
        return provider.complete(config, request)
    }

    fun createStreamingCompletion(
        config: AiProviderConfig,
        messages: List<AiMessage>,
        tools: List<AiToolDefinition> = emptyList(),
        toolChoice: ToolChoice = ToolChoice.None,
    ): Flow<ApiResult<AiCompletionChunk>> {
        val provider = providerRegistry.find(config.providerId)
            ?: return kotlinx.coroutines.flow.flow {
                emit(ApiResult.UnexpectedError("Unknown provider: ${config.providerId}"))
            }
        val request = AiChatRequest(
            model = config.model,
            messages = messages,
            tools = tools,
            toolChoice = toolChoice,
        )
        return provider.completeStreaming(config, request)
    }

    suspend fun supportsToolCalls(config: AiProviderConfig): Boolean {
        val provider = providerRegistry.find(config.providerId) ?: return false
        return provider.supportsToolCalls(config)
    }
}
