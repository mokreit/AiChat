package com.aichat.data.ai

import com.aichat.data.api.ApiResult
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

/**
 * 小米 MiMo 适配器
 *
 * MiMo 使用 OpenAI 兼容接口，但需要自动选择计费 Host。
 * 委托 OpenAiCompatibleProvider，仅覆盖 apiHost 选择逻辑。
 */
class XiaomiMimoProvider(
    private val httpClient: HttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : AiProvider {

    override val id: String = Id

    override suspend fun getModels(config: AiProviderConfig): ApiResult<List<String>> {
        val resolvedConfig = resolveConfig(config)
        return delegate.getModels(resolvedConfig)
    }

    override suspend fun complete(
        config: AiProviderConfig,
        request: AiChatRequest,
    ): ApiResult<AiCompletion> {
        val resolvedConfig = resolveConfig(config)
        return delegate.complete(resolvedConfig, request)
    }

    override fun completeStreaming(
        config: AiProviderConfig,
        request: AiChatRequest,
    ): kotlinx.coroutines.flow.Flow<ApiResult<AiCompletionChunk>> {
        val resolvedConfig = resolveConfig(config)
        return delegate.completeStreaming(resolvedConfig, request)
    }

    override suspend fun supportsToolCalls(config: AiProviderConfig): Boolean {
        val model = config.model.lowercase()
        return model.contains("mimo")
    }

    private val delegate = OpenAiCompatibleProvider(httpClient, json)

    /**
     * MiMo 自动选择计费 Host
     * 如果用户未指定 apiHost，使用默认 MiMo 端点
     */
    private fun resolveConfig(config: AiProviderConfig): AiProviderConfig {
        val host = if (config.apiHost.isBlank()) {
            DEFAULT_MIMO_HOST
        } else {
            config.apiHost
        }
        return config.copy(apiHost = host)
    }

    companion object {
        const val Id = "xiaomimimo"
        const val DEFAULT_MIMO_HOST = "https://api.maimiao.cn/v1/"
    }
}
