package com.aichat.data.ai

import com.aichat.data.api.ApiResult
import com.aichat.data.api.OpenAiChatRequest
import com.aichat.data.api.OpenAiChatResponse
import com.aichat.data.api.OpenAiChatStreamResponse
import com.aichat.data.api.OpenAiFunctionCall
import com.aichat.data.api.OpenAiFunctionDefinition
import com.aichat.data.api.OpenAiJsonSchema
import com.aichat.data.api.OpenAiMessage
import com.aichat.data.api.OpenAiModelsResponse
import com.aichat.data.api.OpenAiResponseFormat
import com.aichat.data.api.OpenAiToolCall
import com.aichat.data.api.OpenAiToolDefinition
import com.aichat.data.api.normalizedBaseUrl
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
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
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class OpenAiCompatibleProvider(
    private val httpClient: HttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : AiProvider {

    override val id: String = Id

    override suspend fun getModels(config: AiProviderConfig): ApiResult<List<String>> {
        return apiCall {
            val response: OpenAiModelsResponse = httpClient.get("${config.apiHost.normalizedBaseUrl()}models") {
                bearerAuth(config.apiKey.trim())
            }.body()
            response.data
                .map { it.id.trim() }
                .filter(String::isNotEmpty)
                .distinct()
                .sorted()
        }
    }

    override suspend fun complete(
        config: AiProviderConfig,
        request: AiChatRequest,
    ): ApiResult<AiCompletion> {
        val apiResult = apiCall {
            httpClient.post("${config.apiHost.normalizedBaseUrl()}chat/completions") {
                bearerAuth(config.apiKey.trim())
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(request.toOpenAiRequest()))
            }.body<OpenAiChatResponse>()
        }
        return when (apiResult) {
            is ApiResult.Success -> apiResult.value.toCompletion(request.responseFormat, json)
            is ApiResult.HttpError -> apiResult
            is ApiResult.NetworkError -> apiResult
            is ApiResult.UnexpectedError -> apiResult
        }
    }

    override fun completeStreaming(
        config: AiProviderConfig,
        request: AiChatRequest,
    ): Flow<ApiResult<AiCompletionChunk>> = flow {
        try {
            httpClient.preparePost("${config.apiHost.normalizedBaseUrl()}chat/completions") {
                bearerAuth(config.apiKey.trim())
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(request.toOpenAiRequest(stream = true)))
            }.execute { response ->
                val channel = response.bodyAsChannel()
                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: break
                    val data = line.toOpenAiStreamData() ?: continue
                    if (data == OpenAiStreamDoneMarker) break
                    val chunk = try {
                        json.decodeFromString<OpenAiChatStreamResponse>(data).toCompletionChunk()
                    } catch (_: SerializationException) {
                        emit(ApiResult.UnexpectedError("Stream completion returned invalid JSON."))
                        return@execute
                    }
                    emit(ApiResult.Success(chunk))
                }
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: ResponseException) {
            emit(
                ApiResult.HttpError(
                    statusCode = error.response.status.value,
                    message = error.message,
                ),
            )
        } catch (error: Throwable) {
            emit(ApiResult.NetworkError(error.message))
        }
    }

    override suspend fun supportsToolCalls(config: AiProviderConfig): Boolean {
        val probeName = "test_tool_call"
        val request = AiChatRequest(
            model = config.model,
            messages = listOf(AiMessage(role = "user", content = "Return 2 using the tool.")),
            maxTokens = 1024,
            tools = listOf(
                AiToolDefinition(
                    name = probeName,
                    description = "Return the answer through this function.",
                    parameters = buildJsonObject {
                        put("type", "object")
                        put(
                            "properties",
                            buildJsonObject {
                                put(
                                    "answer",
                                    buildJsonObject { put("type", "string") },
                                )
                            },
                        )
                        put("required", buildJsonArray { add(JsonPrimitive("answer")) })
                        put("additionalProperties", false)
                    },
                ),
            ),
            toolChoice = ToolChoice.Required,
        )
        return when (val result = complete(config, request)) {
            is ApiResult.Success -> result.value.message.toolCalls.any { it.name == probeName }
            else -> false
        }
    }

    companion object {
        const val Id = "openai-compatible"
    }
}

private suspend inline fun <T> apiCall(block: () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(block())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: ResponseException) {
        ApiResult.HttpError(
            statusCode = error.response.status.value,
            message = error.message,
        )
    } catch (error: Throwable) {
        ApiResult.NetworkError(error.message)
    }
}

private fun AiChatRequest.toOpenAiRequest(
    stream: Boolean = false,
) = OpenAiChatRequest(
    model = model,
    messages = messages.map { message ->
        OpenAiMessage(
            role = message.role,
            content = toOpenAiContent(message),
            toolCalls = message.toolCalls.takeIf(List<*>::isNotEmpty)?.map { call ->
                OpenAiToolCall(
                    id = call.id,
                    function = OpenAiFunctionCall(
                        name = call.name,
                        arguments = call.arguments,
                    ),
                )
            },
            toolCallId = message.toolCallId,
        )
    },
    stream = stream,
    temperature = temperature,
    topP = topP,
    maxTokens = maxTokens,
    tools = tools.takeIf(List<*>::isNotEmpty)?.map { tool ->
        OpenAiToolDefinition(
            type = "function",
            function = OpenAiFunctionDefinition(
                name = tool.name,
                description = tool.description,
                parameters = tool.parameters,
                strict = tool.strict,
            ),
        )
    },
    toolChoice = when (val choice = toolChoice) {
        ToolChoice.None -> JsonPrimitive("none")
        ToolChoice.Auto -> JsonPrimitive("auto")
        ToolChoice.Required -> JsonPrimitive("required")
        is ToolChoice.Specific -> buildJsonObject {
            put("type", "function")
            put("function", buildJsonObject { put("name", choice.name) })
        }
    },
    parallelToolCalls = false.takeIf { tools.any(AiToolDefinition::strict) },
    responseFormat = responseFormat?.toOpenAiResponseFormat(),
)

private const val OpenAiStreamDoneMarker = "[DONE]"

private fun String.toOpenAiStreamData(): String? {
    val trimmed = trim()
    if (!trimmed.startsWith("data:")) return null
    return trimmed.removePrefix("data:").trim()
}

private fun OpenAiChatStreamResponse.toCompletionChunk(): AiCompletionChunk {
    val choice = choices.firstOrNull()
    val usage = usage
    return AiCompletionChunk(
        contentDelta = choice?.delta?.content.orEmpty(),
        finishReason = choice?.finishReason,
        usage = AiUsage(
            promptTokens = usage?.promptTokens ?: 0,
            completionTokens = usage?.completionTokens ?: 0,
            totalTokens = usage?.totalTokens ?: 0,
        ),
    )
}

private fun AiResponseFormat.toOpenAiResponseFormat(): OpenAiResponseFormat {
    return when (type) {
        AiResponseFormatType.JsonSchema -> OpenAiResponseFormat(
            type = "json_schema",
            jsonSchema = OpenAiJsonSchema(
                name = name,
                description = description,
                schema = schema,
                strict = strict,
            ),
        )
        AiResponseFormatType.JsonObject -> OpenAiResponseFormat(type = "json_object")
    }
}

private fun OpenAiChatResponse.toCompletion(
    responseFormat: AiResponseFormat?,
    json: Json,
): ApiResult<AiCompletion> {
    val choice = choices.firstOrNull()
        ?: return ApiResult.UnexpectedError("Chat completion returned no choices.")
    val contentString = choice.message.content.toContentString()
    val structuredOutput = if (responseFormat != null) {
        val content = contentString
            ?: return ApiResult.UnexpectedError("Structured completion returned no content.")
        val parsed = try {
            json.parseToJsonElement(content)
        } catch (_: SerializationException) {
            return ApiResult.UnexpectedError("Structured completion returned invalid JSON.")
        }
        parsed as? JsonObject
            ?: return ApiResult.UnexpectedError("Structured completion returned a non-object JSON value.")
    } else {
        null
    }
    return ApiResult.Success(
        AiCompletion(
            message = AiMessage(
                role = choice.message.role,
                content = contentString,
                toolCalls = choice.message.toolCalls.orEmpty().map { call ->
                    AiToolCall(
                        id = call.id,
                        name = call.function.name,
                        arguments = call.function.arguments,
                    )
                },
                toolCallId = choice.message.toolCallId,
            ),
            finishReason = choice.finishReason,
            usage = AiUsage(
                promptTokens = usage?.promptTokens ?: 0,
                completionTokens = usage?.completionTokens ?: 0,
                totalTokens = usage?.totalTokens ?: 0,
            ),
            structuredOutput = structuredOutput,
        ),
    )
}

private fun toOpenAiContent(message: AiMessage): JsonElement? {
    val parts = message.contentParts
    if (!parts.isNullOrEmpty()) {
        return buildJsonArray {
            parts.forEach { part ->
                add(buildJsonObject {
                    when (part) {
                        is AiContentPart.Text -> {
                            put("type", "text")
                            put("text", part.text)
                        }
                        is AiContentPart.ImageUrl -> {
                            put("type", "image_url")
                            put("image_url", buildJsonObject {
                                put("url", part.url)
                                part.detail?.let { put("detail", it) }
                            })
                        }
                        is AiContentPart.FileUrl -> {
                            put("type", "file_url")
                            put("file_url", buildJsonObject { put("url", part.url) })
                        }
                    }
                })
            }
        }
    }
    return message.content?.let { JsonPrimitive(it) }
}

private fun JsonElement?.toContentString(): String? {
    if (this == null || this is JsonNull) return null
    return when (this) {
        is JsonPrimitive -> content
        is JsonArray -> {
            this.mapNotNull { element ->
                when (element) {
                    is JsonPrimitive -> element.content
                    is JsonObject -> {
                        if (element["type"]?.jsonPrimitive?.content == "text") {
                            element["text"]?.jsonPrimitive?.content
                        } else {
                            null
                        }
                    }
                    else -> null
                }
            }.joinToString("")
        }
        is JsonObject -> {
            if (this["type"]?.jsonPrimitive?.content == "text") {
                this["text"]?.jsonPrimitive?.content
            } else {
                this.toString()
            }
        }
    }
}
