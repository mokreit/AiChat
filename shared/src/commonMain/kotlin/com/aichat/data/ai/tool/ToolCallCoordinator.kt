package com.aichat.data.ai.tool

import com.aichat.data.ai.AiChatRequest
import com.aichat.data.ai.AiMessage
import com.aichat.data.ai.AiProviderConfig
import com.aichat.data.ai.AiRepository
import com.aichat.data.ai.AiToolCall
import com.aichat.data.ai.AiToolDefinition
import com.aichat.data.api.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ToolCallCoordinator(
    private val toolRegistry: AiToolRegistry,
    private val aiRepository: AiRepository,
    private val maxIterations: Int = 5,
) {

    /**
     * 执行带工具调用的完整对话流程：
     * 1. 发送请求给 AI
     * 2. 如果 AI 返回 tool_calls，执行对应工具
     * 3. 将工具结果追加到消息列表，再次请求 AI
     * 4. 重复直到 AI 不再调用工具或达到最大迭代次数
     */
    suspend fun executeWithTools(
        config: AiProviderConfig,
        messages: List<AiMessage>,
        context: ToolContext,
        tools: List<AiToolDefinition> = toolRegistry.getDefinitions(context),
    ): ApiResult<AiMessage> {
        var currentMessages = messages.toList()
        var iteration = 0

        while (iteration < maxIterations) {
            iteration++
            val result = aiRepository.createChatCompletion(config, currentMessages, tools)
            when (result) {
                is ApiResult.Success -> {
                    val assistantMessage = result.value.message
                    val toolCalls = assistantMessage.toolCalls

                    if (toolCalls.isEmpty()) {
                        return ApiResult.Success(assistantMessage)
                    }

                    // 追加 assistant 消息（含 tool_calls）
                    currentMessages = currentMessages + assistantMessage

                    // 执行每个工具调用
                    for (call in toolCalls) {
                        val tool = toolRegistry.find(call.name)
                        val toolResult = if (tool != null) {
                            tool.execute(call, context)
                        } else {
                            ToolResult.Error("Unknown tool: ${call.name}")
                        }

                        // 追加工具结果消息
                        currentMessages = currentMessages + AiMessage(
                            role = "tool",
                            content = when (toolResult) {
                                is ToolResult.Success -> toolResult.content
                                is ToolResult.Error -> "Error: ${toolResult.message}"
                                is ToolResult.Terminal -> toolResult.content
                            },
                            toolCallId = call.id,
                        )
                    }
                }
                is ApiResult.HttpError -> return result
                is ApiResult.NetworkError -> return result
                is ApiResult.UnexpectedError -> return result
            }
        }

        return ApiResult.UnexpectedError("Max tool call iterations ($maxIterations) reached")
    }

    /**
     * Fallback 模式：当 AI 不支持 Tool Call 时
     * 1. 注入引导消息
     * 2. 解析 AI 输出中的工具标签
     */
    suspend fun executeWithFallback(
        config: AiProviderConfig,
        messages: List<AiMessage>,
        context: ToolContext,
    ): ApiResult<Pair<AiMessage, List<ToolResult.Terminal>>> {
        var preparedMessages = messages
        for (tool in toolRegistry.allTools()) {
            preparedMessages = tool.prepareFallbackMessages(preparedMessages, context)
        }

        val result = aiRepository.createChatCompletion(config, preparedMessages)
        return when (result) {
            is ApiResult.Success -> {
                val content = result.value.message.content.orEmpty()
                val fallbackResults = mutableListOf<ToolResult.Terminal>()
                for (tool in toolRegistry.allTools()) {
                    val parsed = tool.parseFallback(content, context)
                    if (parsed != null) {
                        fallbackResults.add(parsed)
                    }
                }
                ApiResult.Success(result.value.message to fallbackResults)
            }
            is ApiResult.HttpError -> result
            is ApiResult.NetworkError -> result
            is ApiResult.UnexpectedError -> result
        }
    }
}
