package com.aichat.data.ai.tool

import com.aichat.data.ai.AiMessage
import com.aichat.data.ai.AiToolCall
import com.aichat.data.ai.AiToolDefinition
import kotlinx.serialization.json.JsonObject

enum class ToolExecutionType {
    /** 工具输出直接作为最终结果返回给用户 */
    TerminalOutput,
    /** 工具输出需要继续传给 AI 处理 */
    IntermediateOutput,
}

enum class ToolRisk {
    /** 只读操作，无副作用 */
    ReadOnly,
    /** 有副作用（如发送消息、修改数据） */
    Destructive,
}

sealed interface ToolResult {
    /** 工具成功执行，产出内容 */
    data class Success(
        val content: String,
        /** 附件数据（如音频、图片） */
        val attachments: List<ToolAttachment> = emptyList(),
    ) : ToolResult

    /** 工具执行失败 */
    data class Error(
        val message: String,
    ) : ToolResult

    /** 终止输出，直接展示给用户 */
    data class Terminal(
        val content: String,
        val attachments: List<ToolAttachment> = emptyList(),
    ) : ToolResult
}

data class ToolAttachment(
    val type: String,
    val uri: String,
    val mimeType: String? = null,
    val durationMs: Long? = null,
)

data class ToolContext(
    val characterName: String = "",
    val voiceDesignPrompt: String = "",
    val voiceSampleUri: String = "",
    val ttsProviderId: String = "",
    val sessionId: String = "",
)

interface AiTool {
    val name: String
    val executionType: ToolExecutionType
    val risk: ToolRisk

    fun definition(context: ToolContext): AiToolDefinition

    suspend fun execute(call: AiToolCall, context: ToolContext): ToolResult

    /** Fallback 模式：注入额外消息引导 AI 使用工具 */
    fun prepareFallbackMessages(messages: List<AiMessage>, context: ToolContext): List<AiMessage> = messages

    /** Fallback 模式：解析 AI 输出中的工具标签 */
    suspend fun parseFallback(content: String, context: ToolContext): ToolResult.Terminal? = null
}

class AiToolRegistry(
    tools: List<AiTool>,
) {
    private val toolsByName = tools.associateBy(AiTool::name)

    fun find(toolName: String): AiTool? = toolsByName[toolName]

    fun allTools(): List<AiTool> = toolsByName.values.toList()

    fun getDefinitions(context: ToolContext): List<AiToolDefinition> =
        toolsByName.values.map { it.definition(context) }
}
