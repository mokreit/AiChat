package com.aichat.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

// ---- Request DTOs ----

@Serializable
data class OpenAiChatRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    val stream: Boolean = false,
    val temperature: Double? = null,
    @SerialName("top_p")
    val topP: Double? = null,
    @SerialName("max_tokens")
    val maxTokens: Int? = null,
    val tools: List<OpenAiToolDefinition>? = null,
    @SerialName("tool_choice")
    val toolChoice: JsonElement? = null,
    @SerialName("parallel_tool_calls")
    val parallelToolCalls: Boolean? = null,
    @SerialName("response_format")
    val responseFormat: OpenAiResponseFormat? = null,
)

@Serializable
data class OpenAiMessage(
    val role: String,
    val content: JsonElement? = null,
    @SerialName("tool_calls")
    val toolCalls: List<OpenAiToolCall>? = null,
    @SerialName("tool_call_id")
    val toolCallId: String? = null,
)

@Serializable
data class OpenAiToolCall(
    val id: String,
    val type: String = "function",
    val function: OpenAiFunctionCall,
)

@Serializable
data class OpenAiFunctionCall(
    val name: String,
    val arguments: String,
)

@Serializable
data class OpenAiToolDefinition(
    val type: String,
    val function: OpenAiFunctionDefinition,
)

@Serializable
data class OpenAiFunctionDefinition(
    val name: String,
    val description: String,
    val parameters: JsonObject,
    val strict: Boolean,
)

@Serializable
data class OpenAiResponseFormat(
    val type: String,
    @SerialName("json_schema")
    val jsonSchema: OpenAiJsonSchema? = null,
)

@Serializable
data class OpenAiJsonSchema(
    val name: String,
    val description: String? = null,
    val schema: JsonObject,
    val strict: Boolean,
)

// ---- Response DTOs ----

@Serializable
data class OpenAiModelsResponse(
    val data: List<OpenAiModel> = emptyList(),
)

@Serializable
data class OpenAiModel(
    val id: String,
)

@Serializable
data class OpenAiChatResponse(
    val choices: List<OpenAiChoice> = emptyList(),
    val usage: OpenAiUsage? = null,
)

@Serializable
data class OpenAiChoice(
    val message: OpenAiMessage,
    @SerialName("finish_reason")
    val finishReason: String? = null,
)

@Serializable
data class OpenAiChatStreamResponse(
    val choices: List<OpenAiStreamChoice> = emptyList(),
    val usage: OpenAiUsage? = null,
)

@Serializable
data class OpenAiStreamChoice(
    val delta: OpenAiStreamDelta = OpenAiStreamDelta(),
    @SerialName("finish_reason")
    val finishReason: String? = null,
)

@Serializable
data class OpenAiStreamDelta(
    val role: String? = null,
    val content: String? = null,
    @SerialName("tool_calls")
    val toolCalls: List<OpenAiStreamToolCall>? = null,
)

@Serializable
data class OpenAiStreamToolCall(
    val index: Int = 0,
    val id: String? = null,
    val function: OpenAiStreamFunction? = null,
)

@Serializable
data class OpenAiStreamFunction(
    val name: String? = null,
    val arguments: String? = null,
)

@Serializable
data class OpenAiUsage(
    @SerialName("prompt_tokens")
    val promptTokens: Int = 0,
    @SerialName("completion_tokens")
    val completionTokens: Int = 0,
    @SerialName("total_tokens")
    val totalTokens: Int = 0,
)
