package com.aichat.data.ai.tool

import com.aichat.data.ai.AiMessage
import com.aichat.data.ai.AiToolCall
import com.aichat.data.ai.AiToolDefinition
import com.aichat.data.api.ApiResult
import com.aichat.data.voice.TtsConfig
import com.aichat.data.voice.TtsProviderRegistry
import com.aichat.data.voice.TtsRequest
import com.aichat.data.platform.KmpFileManager
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.JsonObject

class VoiceSynthesisTool(
    private val ttsProviderRegistry: TtsProviderRegistry,
    private val fileManager: KmpFileManager,
) : AiTool {

    override val name = "synthesize_voice"
    override val executionType = ToolExecutionType.TerminalOutput
    override val risk = ToolRisk.ReadOnly

    override fun definition(context: ToolContext): AiToolDefinition {
        val voiceDesignDesc = if (context.voiceDesignPrompt.isNotBlank()) {
            "The character's voice design: ${context.voiceDesignPrompt}"
        } else {
            "Describe the voice characteristics for the character"
        }

        return AiToolDefinition(
            name = name,
            description = "Synthesize speech for the AI character's response. $voiceDesignDesc",
            parameters = buildJsonObject {
                put("type", "object")
                put("properties", buildJsonObject {
                    put("voice_design", buildJsonObject {
                        put("type", "string")
                        put("description", "Natural language description of the voice characteristics, e.g. 'a shy girl speaking softly in a quiet corridor'")
                    })
                    put("text", buildJsonObject {
                        put("type", "string")
                        put("description", "The exact text to synthesize as speech. Should match the character's dialogue.")
                    })
                    put("ai_response", buildJsonObject {
                        put("type", "string")
                        put("description", "The full AI response text to display to the user while voice plays")
                    })
                })
                put("required", kotlinx.serialization.json.buildJsonArray {
                    add(kotlinx.serialization.json.JsonPrimitive("text"))
                    add(kotlinx.serialization.json.JsonPrimitive("ai_response"))
                })
            },
            strict = false,
        )
    }

    override suspend fun execute(call: AiToolCall, context: ToolContext): ToolResult {
        val args = try {
            parseArguments(call.arguments)
        } catch (_: Exception) {
            return ToolResult.Error("Failed to parse tool arguments")
        }

        val text = args["text"] ?: return ToolResult.Error("Missing 'text' argument")
        val aiResponse = args["ai_response"] ?: text

        val providerId = context.ttsProviderId.ifBlank { "openai-compat-tts" }
        val provider = ttsProviderRegistry.find(providerId)
            ?: return ToolResult.Error("TTS provider not found: $providerId")

        val config = TtsConfig(
            providerId = providerId,
            voiceId = context.voiceDesignPrompt.ifBlank { "alloy" },
        )

        val request = TtsRequest(
            text = text,
            voiceId = config.voiceId,
        )

        return when (val result = provider.synthesize(config, request)) {
            is ApiResult.Success -> {
                val audioUri = saveAudioData(result.value.audioData, result.value.format.value)
                ToolResult.Terminal(
                    content = aiResponse,
                    attachments = listOfNotNull(
                        audioUri?.let {
                            ToolAttachment(
                                type = "voice",
                                uri = it,
                                mimeType = "audio/${result.value.format.value}",
                            )
                        },
                    ),
                )
            }
            is ApiResult.HttpError -> ToolResult.Error("TTS HTTP error: ${result.statusCode}")
            is ApiResult.NetworkError -> ToolResult.Error("TTS network error: ${result.message}")
            is ApiResult.UnexpectedError -> ToolResult.Error("TTS error: ${result.message}")
        }
    }

    override fun prepareFallbackMessages(messages: List<AiMessage>, context: ToolContext): List<AiMessage> {
        val voiceContract = buildString {
            append("<voice_output_contract>\n")
            append("When you want to speak with voice, use this format:\n")
            append("<voice_synthesis voice_design=\"${context.voiceDesignPrompt}\">\n")
            append("[the text to speak]\n")
            append("</voice_synthesis>\n")
            append("You can include multiple voice_synthesis tags in your response.\n")
            append("The text outside the tags will be shown normally.\n")
            append("</voice_output_contract>")
        }
        return messages + AiMessage(
            role = "system",
            content = voiceContract,
        )
    }

    override suspend fun parseFallback(content: String, context: ToolContext): ToolResult.Terminal? {
        val pattern = Regex("<voice_synthesis(?:\\s+voice_design=\"([^\"]*)\")?>\\s*([\\s\\S]*?)\\s*</voice_synthesis>")
        val matches = pattern.findAll(content).toList()
        if (matches.isEmpty()) return null

        val providerId = context.ttsProviderId.ifBlank { "openai-compat-tts" }
        val provider = ttsProviderRegistry.find(providerId) ?: return null

        val cleanContent = pattern.replace(content, "").trim()
        val attachments = mutableListOf<ToolAttachment>()

        for (match in matches) {
            val voiceDesign = match.groupValues[1].ifBlank { context.voiceDesignPrompt }
            val text = match.groupValues[2].trim()
            if (text.isBlank()) continue

            val config = TtsConfig(
                providerId = providerId,
                voiceId = voiceDesign.ifBlank { "alloy" },
            )
            val request = TtsRequest(text = text, voiceId = config.voiceId)

            when (val result = provider.synthesize(config, request)) {
                is ApiResult.Success -> {
                    val audioUri = saveAudioData(result.value.audioData, result.value.format.value)
                    if (audioUri != null) {
                        attachments.add(
                            ToolAttachment(
                                type = "voice",
                                uri = audioUri,
                                mimeType = "audio/${result.value.format.value}",
                            ),
                        )
                    }
                }
                else -> continue
            }
        }

        return ToolResult.Terminal(
            content = cleanContent,
            attachments = attachments,
        )
    }

    private fun parseArguments(json: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val textRegex = Regex("\"(text|ai_response|voice_design)\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
        textRegex.findAll(json).forEach { match ->
            result[match.groupValues[1]] = match.groupValues[2]
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
        }
        return result
    }

    private fun saveAudioData(audioData: ByteArray, format: String): String? {
        return try {
            val path = fileManager.createTempFile("tts_", ".$format")
            fileManager.writeFile(path, audioData)
            path.toString()
        } catch (_: Exception) {
            null
        }
    }
}
