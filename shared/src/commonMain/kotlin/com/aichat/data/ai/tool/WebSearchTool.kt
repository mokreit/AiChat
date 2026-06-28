package com.aichat.data.ai.tool

import com.aichat.data.ai.AiToolCall
import com.aichat.data.ai.AiToolDefinition
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.JsonPrimitive

class WebSearchTool(
    private val webSearchService: WebSearchService,
) : AiTool {

    override val name = "web_search"
    override val executionType = ToolExecutionType.TerminalOutput
    override val risk = ToolRisk.ReadOnly

    override fun definition(context: ToolContext): AiToolDefinition {
        return AiToolDefinition(
            name = name,
            description = """
                Search the web for real-time information and return summarized results.
                Use this when the user asks about current events, recent news, weather, stock prices,
                or any information that may have changed since your training data.
                The tool searches the web, extracts content from the most relevant pages,
                and returns a clean summary.
            """.trimIndent(),
            parameters = buildJsonObject {
                put("type", "object")
                put("properties", buildJsonObject {
                    put("query", buildJsonObject {
                        put("type", "string")
                        put("description", "The search query. Be specific for better results.")
                    })
                    put("max_results", buildJsonObject {
                        put("type", "number")
                        put("description", "Number of search results to fetch (1-5, default 3)")
                    })
                })
                put("required", buildJsonArray {
                    add(JsonPrimitive("query"))
                })
                put("additionalProperties", false)
            },
            strict = false, // let AI decide whether to use max_results
        )
    }

    override suspend fun execute(call: AiToolCall, context: ToolContext): ToolResult {
        val args = try {
            parseArguments(call.arguments)
        } catch (_: Exception) {
            return ToolResult.Error("Failed to parse tool arguments. Expected JSON with 'query' field.")
        }

        val query = args["query"]
            ?: return ToolResult.Error("Missing required argument 'query'.")

        val maxResults = args["max_results"]?.toIntOrNull()?.coerceIn(1, 5) ?: 3

        return try {
            val result = webSearchService.searchAndExtract(query, maxResults)
            ToolResult.Success(result)
        } catch (e: Exception) {
            ToolResult.Error("Web search failed: ${e.message}")
        }
    }

    private fun parseArguments(json: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val regex = Regex("\"(query|max_results)\"\\s*:\\s*\"?((?:[^\"\\\\]|\\\\.)+?)\"?")
        regex.findAll(json).forEach { match ->
            result[match.groupValues[1]] = match.groupValues[2]
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .trimEnd('"')
        }
        // Also try parsing number values (for max_results written as number not string)
        val numRegex = Regex("\"(max_results)\"\\s*:\\s*(\\d+)")
        numRegex.findAll(json).forEach { match ->
            result[match.groupValues[1]] = match.groupValues[2]
        }
        return result
    }
}
