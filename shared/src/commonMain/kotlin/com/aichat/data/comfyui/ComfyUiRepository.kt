package com.aichat.data.comfyui

import com.aichat.data.api.ApiResult
import com.aichat.data.database.dao.ComfyUiConfigDao
import com.aichat.data.database.entity.ComfyUiConfigEntity
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.util.UUID

data class ComfyUiModelsInfo(
    val checkpoints: List<String> = emptyList(),
    val loras: List<String> = emptyList(),
    val samplers: List<String> = emptyList(),
    val schedulers: List<String> = emptyList(),
)

@Serializable
data class ComfyUiPromptResponse(val prompt_id: String)

@Serializable
data class ComfyUiHistoryResponse(val outputs: Map<String, ComfyUiOutput> = emptyMap())

@Serializable
data class ComfyUiOutput(val images: List<ComfyUiImageInfo> = emptyList())

@Serializable
data class ComfyUiImageInfo(val filename: String, val subfolder: String = "", val type: String = "output")

class ComfyUiRepository(
    private val httpClient: HttpClient,
    private val configDao: ComfyUiConfigDao,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    fun getConfig(): Flow<ComfyUiConfigEntity?> = configDao.getConfig()

    suspend fun saveConfig(config: ComfyUiConfigEntity) = configDao.upsert(config)

    suspend fun getConfigOnce(): ComfyUiConfigEntity? = configDao.getConfigOnce()

    suspend fun fetchAvailableModels(serverUrl: String): ComfyUiModelsInfo {
        val url = serverUrl.trimEnd('/')
        val checkpoints = mutableListOf<String>()
        val loras = mutableListOf<String>()
        val samplers = mutableListOf<String>()
        val schedulers = mutableListOf<String>()

        try {
            val info: JsonObject = httpClient.get("$url/object_info").body()

            info["CheckpointLoaderSimple"]?.jsonObject?.get("input")?.jsonObject
                ?.get("required")?.jsonObject?.get("ckpt_name")?.jsonArray
                ?.firstOrNull()?.jsonArray?.forEach {
                    checkpoints.add(it.jsonPrimitive.content)
                }

            info["LoraLoader"]?.jsonObject?.get("input")?.jsonObject
                ?.get("required")?.jsonObject?.get("lora_name")?.jsonArray
                ?.firstOrNull()?.jsonArray?.forEach {
                    loras.add(it.jsonPrimitive.content)
                }

            info["KSampler"]?.jsonObject?.get("input")?.jsonObject
                ?.get("required")?.jsonObject?.get("sampler_name")?.jsonArray
                ?.firstOrNull()?.jsonArray?.forEach {
                    samplers.add(it.jsonPrimitive.content)
                }

            info["KSampler"]?.jsonObject?.get("input")?.jsonObject
                ?.get("required")?.jsonObject?.get("scheduler")?.jsonArray
                ?.firstOrNull()?.jsonArray?.forEach {
                    schedulers.add(it.jsonPrimitive.content)
                }
        } catch (_: Exception) { }

        return ComfyUiModelsInfo(checkpoints, loras, samplers, schedulers)
    }

    fun generateImage(
        config: ComfyUiConfigEntity,
        prompt: String,
        negativePrompt: String = "",
    ): Flow<ApiResult<String>> = flow {
        val serverUrl = config.serverUrl.trimEnd('/')
        if (serverUrl.isBlank()) {
            emit(ApiResult.UnexpectedError("ComfyUI server URL not configured"))
            return@flow
        }
        if (config.workflowJson.isBlank()) {
            emit(ApiResult.UnexpectedError("请先配置工作流 JSON"))
            return@flow
        }

        try {
            val raw = json.parseToJsonElement(config.workflowJson).jsonObject
            val hasNodes = raw.containsKey("nodes")

            // Convert save format to API format if needed
            val apiWorkflow = if (hasNodes) {
                convertSaveToApi(raw)
            } else {
                raw
            }

            // Auto-detect positive/negative prompt nodes and replace text, randomize seed
            val modifiedWorkflow = replacePromptsAndSeed(apiWorkflow, prompt, negativePrompt)

            // Build the final request body
            val clientId = UUID.randomUUID().toString()
            val requestBody = """{"prompt":$modifiedWorkflow,"client_id":"$clientId"}"""

            // Submit via WebSocket (required by ComfyUI)
            val wsScheme = if (serverUrl.startsWith("https")) "wss" else "ws"
            val wsHost = serverUrl.removePrefix("http://").removePrefix("https://")
            val wsUrl = "$wsScheme://$wsHost/ws?clientId=$clientId"

            httpClient.webSocket(wsUrl) {
                val responseRaw: String = httpClient.post("$serverUrl/api/prompt") {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }.body()
                android.util.Log.d("ComfyUi", "Submit response: $responseRaw")
                val response: ComfyUiPromptResponse = json.decodeFromString(responseRaw)
                val promptId = response.prompt_id

                val maxWaitMs = 300_000L
                val startTime = System.currentTimeMillis()
                var completed = false

                for (frame in incoming) {
                    if (System.currentTimeMillis() - startTime > maxWaitMs) break
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        try {
                            val msg = json.parseToJsonElement(text).jsonObject
                            val type = msg["type"]?.jsonPrimitive?.content
                            val data = msg["data"]?.jsonObject
                            when (type) {
                                "execution_success" -> {
                                    completed = true
                                    break
                                }
                                "executing" -> {
                                    val node = data?.get("node")
                                    if (node == null || node.toString() == "null") {
                                        completed = true
                                        break
                                    }
                                }
                                "execution_error" -> {
                                    val errorMsg = data?.get("exception_message")?.jsonPrimitive?.content ?: "Unknown error"
                                    android.util.Log.e("ComfyUi", "Execution error: $errorMsg")
                                    emit(ApiResult.UnexpectedError("ComfyUI error: $errorMsg"))
                                    return@webSocket
                                }
                                "progress" -> {
                                    val value = data?.get("value")?.jsonPrimitive?.content ?: "0"
                                    val max = data?.get("max")?.jsonPrimitive?.content ?: "0"
                                    android.util.Log.d("ComfyUi", "Progress: $value/$max")
                                }
                            }
                        } catch (_: Exception) { }
                    }
                }

                if (!completed) {
                    emit(ApiResult.UnexpectedError("ComfyUI generation timeout"))
                    return@webSocket
                }

                delay(500)
                val historyRaw: String = httpClient.get("$serverUrl/api/history/$promptId").body()
                val history: Map<String, ComfyUiHistoryResponse> = json.decodeFromString(historyRaw)
                val result = history[promptId]
                val output = result?.outputs?.values?.firstOrNull()
                val imageInfo = output?.images?.firstOrNull()

                if (imageInfo != null) {
                    val imageUrl = "$serverUrl/view?filename=${imageInfo.filename}&subfolder=${imageInfo.subfolder}&type=${imageInfo.type}"
                    emit(ApiResult.Success(imageUrl))
                } else {
                    emit(ApiResult.UnexpectedError("No image generated"))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ComfyUi", "Error: ${e.message}", e)
            emit(ApiResult.NetworkError(e.message))
        }
    }

    /**
     * Auto-detect positive/negative prompt nodes by tracing KSampler inputs,
     * replace their text, and randomize seed.
     */
    private fun replacePromptsAndSeed(workflow: JsonObject, positivePrompt: String, negativePrompt: String): JsonObject {
        // Find KSampler and trace its positive/negative inputs
        var positiveNodeId: String? = null
        var negativeNodeId: String? = null
        for ((nodeId, node) in workflow) {
            val nodeObj = node.jsonObject
            val classType = nodeObj["class_type"]?.jsonPrimitive?.content
            if (classType != "KSampler") continue

            val positiveRef = nodeObj["inputs"]?.jsonObject?.get("positive")
            if (positiveRef is JsonArray && positiveRef.size >= 2) {
                val srcId = positiveRef[0].jsonPrimitive.content
                val srcNode = workflow[srcId]?.jsonObject
                if (srcNode?.get("class_type")?.jsonPrimitive?.content == "CLIPTextEncode") {
                    positiveNodeId = srcId
                }
            }

            val negativeRef = nodeObj["inputs"]?.jsonObject?.get("negative")
            if (negativeRef is JsonArray && negativeRef.size >= 2) {
                val srcId = negativeRef[0].jsonPrimitive.content
                val srcNode = workflow[srcId]?.jsonObject
                if (srcNode?.get("class_type")?.jsonPrimitive?.content == "CLIPTextEncode") {
                    negativeNodeId = srcId
                }
            }
            break
        }

        val modified = workflow.toMutableMap()

        // Replace positive prompt
        if (positiveNodeId != null) {
            val nodeObj = modified[positiveNodeId]!!.jsonObject
            val inputs = nodeObj["inputs"]!!.jsonObject.toMutableMap()
            inputs["text"] = JsonPrimitive(positivePrompt)
            modified[positiveNodeId] = buildJsonObject {
                nodeObj.forEach { (k, v) ->
                    if (k == "inputs") {
                        put(k, buildJsonObject { inputs.forEach { (ik, iv) -> put(ik, iv) } })
                    } else {
                        put(k, v)
                    }
                }
            }
        }

        // Replace negative prompt
        if (negativePrompt.isNotBlank() && negativeNodeId != null) {
            val nodeObj = modified[negativeNodeId]!!.jsonObject
            val inputs = nodeObj["inputs"]!!.jsonObject.toMutableMap()
            inputs["text"] = JsonPrimitive(negativePrompt)
            modified[negativeNodeId] = buildJsonObject {
                nodeObj.forEach { (k, v) ->
                    if (k == "inputs") {
                        put(k, buildJsonObject { inputs.forEach { (ik, iv) -> put(ik, iv) } })
                    } else {
                        put(k, v)
                    }
                }
            }
        }

        // Randomize seed for KSampler nodes
        val randomSeed = kotlin.random.Random.nextLong(0, Long.MAX_VALUE)
        for ((nodeId, nodeValue) in modified) {
            val node = nodeValue.jsonObject
            if (node["class_type"]?.jsonPrimitive?.content == "KSampler") {
                val nodeInputs = node["inputs"]!!.jsonObject.toMutableMap()
                nodeInputs["seed"] = JsonPrimitive(randomSeed)
                modified[nodeId] = buildJsonObject {
                    node.forEach { (k, v) ->
                        if (k == "inputs") {
                            put(k, buildJsonObject { nodeInputs.forEach { (ik, iv) -> put(ik, iv) } })
                        } else {
                            put(k, v)
                        }
                    }
                }
            }
        }

        return buildJsonObject { modified.forEach { (k, v) -> put(k, v) } }
    }

    /**
     * Convert ComfyUI save format (with "nodes" and "links" arrays)
     * to API format (node IDs as keys).
     */
    private fun convertSaveToApi(raw: JsonObject): JsonObject {
        val nodesArray = raw["nodes"]?.jsonArray ?: return raw
        val linksArray = raw["links"]?.jsonArray ?: return raw

        // Build link lookup: linkId -> [fromNode, fromSlot, toNode, toSlot]
        val linkMap = mutableMapOf<Int, List<Int>>()
        for (link in linksArray) {
            val arr = link.jsonArray
            val linkId = arr[0].jsonPrimitive.content.toInt()
            val fromNode = arr[1].jsonPrimitive.content.toInt()
            val fromSlot = arr[2].jsonPrimitive.content.toInt()
            val toNode = arr[3].jsonPrimitive.content.toInt()
            val toSlot = arr[4].jsonPrimitive.content.toInt()
            linkMap[linkId] = listOf(fromNode, fromSlot, toNode, toSlot)
        }

        val apiNodes = mutableMapOf<String, JsonElement>()

        for (nodeEl in nodesArray) {
            val nodeObj = nodeEl.jsonObject
            val nodeId = nodeObj["id"]?.jsonPrimitive?.content ?: continue
            val nodeType = nodeObj["type"]?.jsonPrimitive?.content ?: continue
            val widgetsValues = nodeObj["widgets_values"]?.jsonArray

            val inputs = mutableMapOf<String, JsonElement>()

            // Process connected inputs (from links)
            val nodeInputs = nodeObj["inputs"]?.jsonArray
            if (nodeInputs != null) {
                for (input in nodeInputs) {
                    val inputObj = input.jsonObject
                    val inputName = inputObj["name"]?.jsonPrimitive?.content ?: continue
                    val linkId = inputObj["link"]?.jsonPrimitive?.content?.toIntOrNull()
                    if (linkId != null) {
                        val linkInfo = linkMap[linkId]
                        if (linkInfo != null) {
                            inputs[inputName] = JsonArray(listOf(
                                JsonPrimitive(linkInfo[0].toString()),
                                JsonPrimitive(linkInfo[1])
                            ))
                        }
                    }
                }
            }

            // Map widget values to named inputs
            if (widgetsValues != null) {
                mapWidgetValues(nodeType, widgetsValues, inputs)
            }

            val apiNode = buildJsonObject {
                put("class_type", nodeType)
                put("inputs", buildJsonObject { inputs.forEach { (k, v) -> put(k, v) } })
                put("_meta", buildJsonObject {
                    put("title", nodeType)
                })
            }
            apiNodes[nodeId] = apiNode
        }

        return buildJsonObject { apiNodes.forEach { (k, v) -> put(k, v) } }
    }

    /**
     * Map widget values (positional) to named inputs based on node type.
     */
    private fun mapWidgetValues(
        nodeType: String,
        widgets: JsonArray,
        inputs: MutableMap<String, JsonElement>
    ) {
        when (nodeType) {
            "CheckpointLoaderSimple" -> {
                if (widgets.size >= 1) inputs["ckpt_name"] = JsonPrimitive(widgets[0].jsonPrimitive.content)
            }
            "CLIPLoader" -> {
                if (widgets.size >= 1) inputs["clip_name"] = JsonPrimitive(widgets[0].jsonPrimitive.content)
                if (widgets.size >= 2) inputs["type"] = JsonPrimitive(widgets[1].jsonPrimitive.content)
                if (widgets.size >= 3) inputs["device"] = JsonPrimitive(widgets[2].jsonPrimitive.content)
            }
            "VAELoader" -> {
                if (widgets.size >= 1) inputs["vae_name"] = JsonPrimitive(widgets[0].jsonPrimitive.content)
            }
            "CLIPTextEncode" -> {
                if (widgets.size >= 1) inputs["text"] = JsonPrimitive(widgets[0].jsonPrimitive.content)
            }
            "KSampler" -> {
                // KSampler widget order: [seed, control_after_generate, steps, cfg, sampler_name, scheduler, denoise]
                // control_after_generate is a hidden UI widget, not an API input
                if (widgets.size >= 1) {
                    inputs["seed"] = JsonPrimitive(widgets[0].jsonPrimitive.content.toLongOrNull() ?: 0)
                }
                if (widgets.size >= 3) {
                    val stepsVal = widgets[2].jsonPrimitive
                    if (stepsVal.content == "randomize") inputs["steps"] = JsonPrimitive(20)
                    else inputs["steps"] = JsonPrimitive(stepsVal.content.toIntOrNull() ?: 20)
                }
                if (widgets.size >= 4) inputs["cfg"] = JsonPrimitive(widgets[3].jsonPrimitive.content.toDoubleOrNull() ?: 7.0)
                if (widgets.size >= 5) inputs["sampler_name"] = JsonPrimitive(widgets[4].jsonPrimitive.content)
                if (widgets.size >= 6) inputs["scheduler"] = JsonPrimitive(widgets[5].jsonPrimitive.content)
                if (widgets.size >= 7) inputs["denoise"] = JsonPrimitive(widgets[6].jsonPrimitive.content.toDoubleOrNull() ?: 1.0)
            }
            "EmptyLatentImage" -> {
                if (widgets.size >= 1) inputs["width"] = JsonPrimitive(widgets[0].jsonPrimitive.content.toIntOrNull() ?: 512)
                if (widgets.size >= 2) inputs["height"] = JsonPrimitive(widgets[1].jsonPrimitive.content.toIntOrNull() ?: 512)
                if (widgets.size >= 3) inputs["batch_size"] = JsonPrimitive(widgets[2].jsonPrimitive.content.toIntOrNull() ?: 1)
            }
            "VAEDecode" -> { }
            "SaveImage" -> {
                if (widgets.size >= 1) inputs["filename_prefix"] = JsonPrimitive(widgets[0].jsonPrimitive.content)
            }
            "LoraLoader" -> {
                if (widgets.size >= 1) inputs["lora_name"] = JsonPrimitive(widgets[0].jsonPrimitive.content)
                if (widgets.size >= 2) inputs["strength_model"] = JsonPrimitive(widgets[1].jsonPrimitive.content.toDoubleOrNull() ?: 1.0)
                if (widgets.size >= 3) inputs["strength_clip"] = JsonPrimitive(widgets[2].jsonPrimitive.content.toDoubleOrNull() ?: 1.0)
            }
            "Power Lora Loader (rgthree)" -> {
                val loras = buildJsonArray {
                    for (widget in widgets) {
                        try {
                            val obj = widget.jsonObject
                            if (obj.containsKey("lora")) {
                                add(widget)
                            }
                        } catch (_: Exception) { }
                    }
                }
                if (loras.isNotEmpty()) {
                    inputs["lora_bundle"] = loras
                }
            }
        }
    }
}
