package com.aichat.data.voice

import com.aichat.data.api.ApiResult
import com.aichat.data.api.normalizedBaseUrl
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class OpenAiCompatTtsProvider(
    private val httpClient: HttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : TtsProvider {

    override val id: String = Id
    override val displayName: String = "OpenAI TTS"

    override suspend fun synthesize(config: TtsConfig, request: TtsRequest): ApiResult<TtsResult> {
        return ttsApiCall {
            val response = httpClient.post("${config.apiHost.normalizedBaseUrl()}audio/speech") {
                bearerAuth(config.apiKey.trim())
                contentType(ContentType.Application.Json)
                setBody(
                    json.encodeToString(
                        OpenAiTtsRequest.serializer(),
                        OpenAiTtsRequest(
                            model = request.model,
                            input = request.text,
                            voice = request.voiceId,
                            responseFormat = request.responseFormat.value,
                            speed = request.speed,
                        ),
                    ),
                )
            }
            val bytes = response.body<ByteArray>()
            TtsResult(audioData = bytes, format = request.responseFormat)
        }
    }

    override suspend fun getAvailableVoices(config: TtsConfig): ApiResult<List<String>> {
        // OpenAI TTS 固定支持这些 voice
        return ApiResult.Success(
            listOf("alloy", "ash", "coral", "echo", "fable", "onyx", "nova", "sage", "shimmer"),
        )
    }

    companion object {
        const val Id = "openai-compat-tts"
    }
}

@Serializable
private data class OpenAiTtsRequest(
    val model: String,
    val input: String,
    val voice: String,
    @kotlinx.serialization.SerialName("response_format")
    val responseFormat: String = "mp3",
    val speed: Float = 1.0f,
)

private suspend inline fun <T> ttsApiCall(block: () -> T): ApiResult<T> {
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
