package com.aichat.data.voice

import com.aichat.data.api.ApiResult
import com.aichat.data.api.normalizedBaseUrl
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class AliyunTtsProvider(
    private val httpClient: HttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : TtsProvider {

    override val id: String = Id
    override val displayName: String = "Aliyun TTS"

    override suspend fun synthesize(config: TtsConfig, request: TtsRequest): ApiResult<TtsResult> {
        return ttsApiCallAliyun {
            val baseUrl = config.apiHost.normalizedBaseUrl().ifBlank {
                "https://nls-gateway-cn-shanghai.aliyuncs.com"
            }
            val response = httpClient.post("${baseUrl}stream/v1/tts") {
                header("X-NLS-Token", config.apiKey.trim())
                contentType(ContentType.Application.Json)
                setBody(
                    json.encodeToString(
                        AliyunTtsRequest.serializer(),
                        AliyunTtsRequest(
                            text = request.text,
                            voice = request.voiceId.ifBlank { "xiaoyun" },
                            format = "mp3",
                            sampleRate = 16000,
                            volume = 50,
                            speechRate = 0,
                        ),
                    ),
                )
            }
            val bytes = response.body<ByteArray>()
            TtsResult(audioData = bytes, format = TtsAudioFormat.Mp3)
        }
    }

    override suspend fun getAvailableVoices(config: TtsConfig): ApiResult<List<String>> {
        return ApiResult.Success(ALIYUN_VOICES)
    }

    companion object {
        const val Id = "aliyun-tts"
        val ALIYUN_VOICES = listOf(
            "xiaoyun", "xiaogang", "ruoxi", "siqi", "sijia", "sicheng",
            "aiqi", "aijia", "aicheng", "aimin", "zhiyan", "zhiqi",
        )
    }
}

@Serializable
private data class AliyunTtsRequest(
    val text: String,
    val voice: String,
    val format: String = "mp3",
    @kotlinx.serialization.SerialName("sample_rate")
    val sampleRate: Int = 16000,
    val volume: Int = 50,
    @kotlinx.serialization.SerialName("speech_rate")
    val speechRate: Int = 0,
)

private suspend inline fun <T> ttsApiCallAliyun(block: () -> T): ApiResult<T> {
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
