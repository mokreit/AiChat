package com.aichat.data.voice

import com.aichat.data.api.ApiResult
import com.aichat.data.api.normalizedBaseUrl
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class EdgeTtsProvider(
    private val httpClient: HttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : TtsProvider {

    override val id: String = Id
    override val displayName: String = "Edge TTS"

    override suspend fun synthesize(config: TtsConfig, request: TtsRequest): ApiResult<TtsResult> {
        return ttsApiCallEdge {
            val response = httpClient.post("${config.apiHost.normalizedBaseUrl()}audio/speech") {
                contentType(ContentType.Application.Json)
                setBody(
                    json.encodeToString(
                        EdgeTtsRequest.serializer(),
                        EdgeTtsRequest(
                            model = "tts-1",
                            input = request.text,
                            voice = request.voiceId.ifBlank { DEFAULT_VOICE },
                            responseFormat = request.responseFormat.value,
                        ),
                    ),
                )
            }
            val bytes = response.body<ByteArray>()
            TtsResult(audioData = bytes, format = request.responseFormat)
        }
    }

    override suspend fun getAvailableVoices(config: TtsConfig): ApiResult<List<String>> {
        return ApiResult.Success(EDGE_VOICES)
    }

    companion object {
        const val Id = "edge-tts"
        const val DEFAULT_VOICE = "zh-CN-XiaoxiaoNeural"
        val EDGE_VOICES = listOf(
            "zh-CN-XiaoxiaoNeural",
            "zh-CN-XiaoyiNeural",
            "zh-CN-YunjianNeural",
            "zh-CN-XiaochenNeural",
            "zh-CN-XiaohanNeural",
            "zh-CN-XiaomengNeural",
            "zh-CN-XiaomoNeural",
            "zh-CN-XiaoqiuNeural",
            "zh-CN-XiaoruiNeural",
            "zh-CN-XiaoshuangNeural",
            "zh-CN-XiaoxuanNeural",
            "zh-CN-XiaoyanNeural",
            "zh-CN-XiaoyouNeural",
            "zh-CN-XiaozhenNeural",
            "en-US-JennyNeural",
            "en-US-GuyNeural",
            "en-US-AriaNeural",
            "en-US-DavisNeural",
            "ja-JP-NanamiNeural",
            "ja-JP-KeitaNeural",
            "ko-KR-SunHiNeural",
            "ko-KR-InJoonNeural",
        )
    }
}

@Serializable
private data class EdgeTtsRequest(
    val model: String,
    val input: String,
    val voice: String,
    @kotlinx.serialization.SerialName("response_format")
    val responseFormat: String = "mp3",
)

private suspend inline fun <T> ttsApiCallEdge(block: () -> T): ApiResult<T> {
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
