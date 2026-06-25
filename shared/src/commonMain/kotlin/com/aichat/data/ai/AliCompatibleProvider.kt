package com.aichat.data.ai

import com.aichat.data.api.ApiResult
import com.aichat.data.api.normalizedBaseUrl
import io.ktor.client.HttpClient
import io.ktor.client.call.body
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 阿里兼容适配器 - 通义千问等阿里系模型
 *
 * 阿里云 DashScope 兼容 OpenAI 接口格式，
 * 但部分细节（如错误码、流式格式）有差异。
 * 此 Provider 处理这些差异。
 */
class AliCompatibleProvider(
    private val httpClient: HttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : AiProvider {

    override val id: String = Id

    override suspend fun getModels(config: AiProviderConfig): ApiResult<List<String>> {
        // 阿里云 DashScope 兼容 OpenAI 的 /models 端点
        return OpenAiCompatibleProvider(httpClient, json).getModels(config)
    }

    override suspend fun complete(
        config: AiProviderConfig,
        request: AiChatRequest,
    ): ApiResult<AiCompletion> {
        // 阿里云兼容 OpenAI 格式，直接委托
        return OpenAiCompatibleProvider(httpClient, json).complete(config, request)
    }

    override fun completeStreaming(
        config: AiProviderConfig,
        request: AiChatRequest,
    ): Flow<ApiResult<AiCompletionChunk>> {
        // 阿里云流式格式与 OpenAI 一致
        return OpenAiCompatibleProvider(httpClient, json).completeStreaming(config, request)
    }

    override suspend fun supportsToolCalls(config: AiProviderConfig): Boolean {
        // 通义千问 qwen-plus/qwen-max 支持 tool_calls
        val model = config.model.lowercase()
        return model.contains("qwen") && !model.contains("qwen-turbo")
    }

    companion object {
        const val Id = "ali-compatible"
    }
}
