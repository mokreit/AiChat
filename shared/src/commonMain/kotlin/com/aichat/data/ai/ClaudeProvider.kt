package com.aichat.data.ai

import com.aichat.data.api.ApiResult
import com.aichat.data.api.normalizedBaseUrl
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ClaudeProvider(
    private val httpClient: HttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : AiProvider {

    override val id: String = Id

    override suspend fun getModels(config: AiProviderConfig): ApiResult<List<String>> {
        return ApiResult.Success(
            listOf("claude-sonnet-4-20250514", "claude-haiku-4-20250414", "claude-3-5-sonnet-20241022", "claude-3-5-haiku-20241022"),
        )
    }

    override suspend fun complete(
        config: AiProviderConfig,
        request: AiChatRequest,
    ): ApiResult<AiCompletion> {
        return try {
            val response = httpClient.post("${config.apiHost.normalizedBaseUrl()}messages") {
                header("x-api-key", config.apiKey.trim())
                header("anthropic-version", "2023-06-01")
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(ClaudeRequest.serializer(), buildRequest(config, request)))
            }
            val body = response.body<String>()
            val parsed = json.decodeFromString(ClaudeResponse.serializer(), body)
            ApiResult.Success(parsed.toAiCompletion())
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: ResponseException) {
            ApiResult.HttpError(error.response.status.value, error.message)
        } catch (error: Throwable) {
            ApiResult.NetworkError(error.message)
        }
    }

    override fun completeStreaming(
        config: AiProviderConfig,
        request: AiChatRequest,
    ): Flow<ApiResult<AiCompletionChunk>> = flow {
        try {
            val httpResponse = httpClient.preparePost("${config.apiHost.normalizedBaseUrl()}messages") {
                header("x-api-key", config.apiKey.trim())
                header("anthropic-version", "2023-06-01")
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(ClaudeRequest.serializer(), buildRequest(config, request, stream = true)))
            }.execute()

            val channel = httpResponse.bodyAsChannel()
            var buffer = ""

            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: continue
                val trimmed = line.trim()
                if (!trimmed.startsWith("data: ")) continue

                val data = trimmed.removePrefix("data: ").trim()
                if (data.isEmpty()) continue

                try {
                    val event = json.decodeFromString(ClaudeStreamEvent.serializer(), data)
                    when (event.type) {
                        "content_block_delta" -> {
                            val text = event.delta?.text ?: ""
                            if (text.isNotEmpty()) {
                                emit(ApiResult.Success(AiCompletionChunk(contentDelta = text)))
                            }
                        }
                        "message_delta" -> {
                            val stopReason = event.delta?.stopReason
                            if (stopReason != null) {
                                emit(ApiResult.Success(AiCompletionChunk(finishReason = stopReason)))
                            }
                        }
                        "message_stop" -> {
                            emit(ApiResult.Success(AiCompletionChunk(finishReason = "stop")))
                        }
                    }
                } catch (_: Exception) {
                    // Skip unparseable events
                }
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: ResponseException) {
            emit(ApiResult.HttpError(error.response.status.value, error.message))
        } catch (error: Throwable) {
            emit(ApiResult.NetworkError(error.message))
        }
    }

    override suspend fun supportsToolCalls(config: AiProviderConfig): Boolean = true

    private fun buildRequest(
        config: AiProviderConfig,
        request: AiChatRequest,
        stream: Boolean = false,
    ): ClaudeRequest {
        val systemMsg = request.messages.find { it.role == "system" }?.content ?: ""
        val chatMsgs = request.messages.filter { it.role != "system" }.map { msg ->
            ClaudeMessage(role = msg.role, content = msg.content ?: "")
        }

        return ClaudeRequest(
            model = config.model,
            maxTokens = request.maxTokens ?: 4096,
            system = systemMsg,
            messages = chatMsgs,
            stream = stream,
        )
    }

    companion object {
        const val Id = "claude"
    }
}

@kotlinx.serialization.Serializable
private data class ClaudeRequest(
    val model: String,
    @kotlinx.serialization.SerialName("max_tokens")
    val maxTokens: Int = 4096,
    val system: String = "",
    val messages: List<ClaudeMessage>,
    val stream: Boolean = false,
)

@kotlinx.serialization.Serializable
private data class ClaudeMessage(
    val role: String,
    val content: String,
)

@kotlinx.serialization.Serializable
private data class ClaudeResponse(
    val id: String = "",
    val type: String = "",
    val role: String = "assistant",
    val content: List<ClaudeContentBlock> = emptyList(),
    @kotlinx.serialization.SerialName("stop_reason")
    val stopReason: String? = null,
    val usage: ClaudeUsage = ClaudeUsage(),
) {
    fun toAiCompletion(): AiCompletion {
        val text = content.filter { it.type == "text" }.joinToString("") { it.text ?: "" }
        return AiCompletion(
            message = AiMessage(role = "assistant", content = text),
            finishReason = stopReason,
            usage = AiUsage(
                promptTokens = usage.inputTokens,
                completionTokens = usage.outputTokens,
                totalTokens = usage.inputTokens + usage.outputTokens,
            ),
        )
    }
}

@kotlinx.serialization.Serializable
private data class ClaudeContentBlock(
    val type: String = "",
    val text: String? = null,
)

@kotlinx.serialization.Serializable
private data class ClaudeUsage(
    @kotlinx.serialization.SerialName("input_tokens")
    val inputTokens: Int = 0,
    @kotlinx.serialization.SerialName("output_tokens")
    val outputTokens: Int = 0,
)

@kotlinx.serialization.Serializable
private data class ClaudeStreamEvent(
    val type: String = "",
    val delta: ClaudeStreamDelta? = null,
)

@kotlinx.serialization.Serializable
private data class ClaudeStreamDelta(
    val type: String = "",
    val text: String = "",
    @kotlinx.serialization.SerialName("stop_reason")
    val stopReason: String? = null,
)
