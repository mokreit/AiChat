package com.aichat.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.intl.Locale

sealed interface AppStrings {
    // App
    val appName: String

    // Navigation
    val back: String
    val chat: String
    val stories: String
    val settings: String

    // Character
    val character: String
    val characterNotFound: String
    val noCharacters: String
    val addCharactersHint: String
    val personality: String
    val scenario: String
    val firstMessage: String
    val voiceDesign: String

    // Chat
    val typeMessage: String
    val sending: String
    val errorPrefix: String
    val retry: String
    val searchSessions: String
    val noSessions: String
    val deleteSession: String
    val deleteSessionConfirm: String
    val messages: String

    // Story
    val story: String
    val storyNotFound: String
    val noStories: String
    val systemPrompt: String

    // Settings
    val appearance: String
    val darkTheme: String
    val aiModel: String
    val voice: String
    val autoPlayVoice: String
    val about: String
    val version: String
    val apiHost: String
    val apiKey: String
    val modelName: String
    val notConfigured: String
    val configure: String
    val save: String
    val cancel: String
    val confirm: String
    val delete: String
    val addModel: String
    val editModel: String
    val configureModel: String
    val noModelConfig: String
    val defaultModel: String
    val ttsProvider: String
    val on: String
    val off: String
    val appDescription: String
    val builtWith: String

    // Voice
    val play: String
    val stop: String

    // Create/Edit
    val createCharacter: String
    val editCharacter: String
    val createStory: String
    val editStory: String
    val name: String
    val description: String
    val requiredField: String

    // Story - character selection
    val participatingCharacters: String

    // Chat actions
    val copy: String
    val regenerate: String
    val rewindToHere: String
    val deleteMessage: String
    val messageActions: String
    val stopGenerating: String
    val exportChat: String
    val exportSuccess: String
    val exportFailed: String
    val clearChat: String
    val chatCleared: String
    val chatExported: String
    val language: String
    val chinese: String
    val english: String
    val nickname: String
    val avatar: String
    val themeMode: String
    val followSystem: String
    val lightMode: String
    val darkMode: String
    val configPresets: String
    val savePreset: String
    val loadPreset: String
    val deletePreset: String
    val presetName: String
    val noPresets: String
    val fetchModels: String
    val fetchingModels: String
    val fetchModelsFailed: String
    val selectModel: String
    val modelConfig: String
    val textModel: String
    val voiceModel: String
    val provider: String
    val customApi: String
    val clear: String
    val modelCount: String
    val techStack: String
    val openSource: String
    val tapToEdit: String
    val configPreset: String
    val presetCount: String
    val saveCurrentConfig: String
    val saved: String
    val cleared: String
    val switchedConfig: String
    val deleted: String
    val pleaseInput: String
    val pleaseFillApi: String
    val noModelsFound: String
    val modelsFetched: String
    val cropImage: String
    val backgroundImage: String
    val zoomIn: String
    val zoomOut: String
    val preview: String
}

object ZhStrings : AppStrings {
    override val appName = "AiChat"
    override val back = "返回"
    override val chat = "聊天"
    override val stories = "故事"
    override val settings = "设置"
    override val character = "角色"
    override val characterNotFound = "未找到角色"
    override val noCharacters = "暂无角色"
    override val addCharactersHint = "在设置中添加角色"
    override val personality = "性格"
    override val scenario = "场景"
    override val participatingCharacters = "参与角色"
    override val firstMessage = "开场白"
    override val voiceDesign = "语音设计"
    override val typeMessage = "输入消息..."
    override val sending = "发送中..."
    override val errorPrefix = "错误："
    override val retry = "重试"
    override val searchSessions = "搜索对话"
    override val noSessions = "暂无对话"
    override val deleteSession = "删除对话"
    override val deleteSessionConfirm = "确定删除此对话？"
    override val messages = "消息"
    override val story = "故事"
    override val storyNotFound = "未找到故事"
    override val noStories = "暂无故事"
    override val systemPrompt = "系统提示词"
    override val appearance = "外观"
    override val darkTheme = "深色主题"
    override val aiModel = "AI 模型"
    override val voice = "语音"
    override val autoPlayVoice = "自动播放语音"
    override val about = "关于"
    override val version = "版本"
    override val apiHost = "API 地址"
    override val apiKey = "API Key"
    override val modelName = "模型名称"
    override val notConfigured = "未配置"
    override val configure = "配置"
    override val save = "保存"
    override val cancel = "取消"
    override val confirm = "确认"
    override val delete = "删除"
    override val addModel = "添加模型"
    override val editModel = "编辑模型"
    override val configureModel = "配置 API 和模型"
    override val noModelConfig = "暂无模型配置，请添加"
    override val defaultModel = "默认模型"
    override val ttsProvider = "TTS 提供商"
    override val on = "开"
    override val off = "关"
    override val appDescription = "AI 角色聊天应用"
    override val builtWith = "基于 Kotlin Multiplatform 构建"
    override val play = "播放"
    override val stop = "停止"
    override val createCharacter = "创建角色"
    override val editCharacter = "编辑角色"
    override val createStory = "创建故事"
    override val editStory = "编辑故事"
    override val name = "名称"
    override val description = "描述"
    override val requiredField = "此项为必填"
    override val copy = "复制"
    override val regenerate = "重新生成"
    override val rewindToHere = "回溯到此处"
    override val deleteMessage = "删除消息"
    override val messageActions = "消息操作"
    override val stopGenerating = "停止生成"
    override val exportChat = "导出对话"
    override val exportSuccess = "导出成功"
    override val exportFailed = "导出失败"
    override val clearChat = "清空对话"
    override val chatCleared = "对话已清空"
    override val chatExported = "对话已导出"
    override val language = "语言"
    override val chinese = "中文"
    override val english = "English"
    override val nickname = "昵称"
    override val avatar = "头像"
    override val themeMode = "主题模式"
    override val followSystem = "跟随系统"
    override val lightMode = "浅色模式"
    override val darkMode = "深色模式"
    override val configPresets = "配置预设"
    override val savePreset = "保存为预设"
    override val loadPreset = "加载预设"
    override val deletePreset = "删除预设"
    override val presetName = "预设名称"
    override val noPresets = "暂无预设"
    override val fetchModels = "获取模型列表"
    override val fetchingModels = "获取中..."
    override val fetchModelsFailed = "获取模型列表失败"
    override val selectModel = "选择模型"
    override val modelConfig = "模型配置"
    override val textModel = "文本模型"
    override val voiceModel = "语音模型"
    override val provider = "提供商"
    override val customApi = "自定义 API 地址"
    override val clear = "清空"
    override val modelCount = " 个"
    override val techStack = "Kotlin Multiplatform + Compose"
    override val openSource = "开源许可"
    override val tapToEdit = "点击修改昵称"
    override val configPreset = "配置预设"
    override val presetCount = " 个"
    override val saveCurrentConfig = "保存当前配置"
    override val saved = "已保存"
    override val cleared = "已清空"
    override val switchedConfig = "已切换配置"
    override val deleted = "已删除"
    override val pleaseInput = "请输入"
    override val pleaseFillApi = "请先填写 API 地址和 Key"
    override val noModelsFound = "未获取到模型"
    override val modelsFetched = "获取到 %d 个模型"
    override val cropImage = "裁剪图片"
    override val backgroundImage = "背景图"
    override val zoomIn = "放大"
    override val zoomOut = "缩小"
    override val preview = "预览"
}

object EnStrings : AppStrings {
    override val appName = "AiChat"
    override val back = "Back"
    override val chat = "Chat"
    override val stories = "Stories"
    override val settings = "Settings"
    override val character = "Character"
    override val characterNotFound = "Character not found"
    override val noCharacters = "No characters yet"
    override val addCharactersHint = "Add characters in Settings"
    override val personality = "Personality"
    override val scenario = "Scenario"
    override val participatingCharacters = "Participants"
    override val firstMessage = "First Message"
    override val voiceDesign = "Voice Design"
    override val typeMessage = "Type a message..."
    override val sending = "Sending..."
    override val errorPrefix = "Error: "
    override val retry = "Retry"
    override val searchSessions = "Search sessions"
    override val noSessions = "No sessions"
    override val deleteSession = "Delete session"
    override val deleteSessionConfirm = "Delete this session?"
    override val messages = "Messages"
    override val story = "Story"
    override val storyNotFound = "Story not found"
    override val noStories = "No stories yet"
    override val systemPrompt = "System Prompt"
    override val appearance = "Appearance"
    override val darkTheme = "Dark Theme"
    override val aiModel = "AI Model"
    override val voice = "Voice"
    override val autoPlayVoice = "Auto Play Voice"
    override val about = "About"
    override val version = "Version"
    override val apiHost = "API Host"
    override val apiKey = "API Key"
    override val modelName = "Model Name"
    override val notConfigured = "Not configured"
    override val configure = "Configure"
    override val save = "Save"
    override val cancel = "Cancel"
    override val confirm = "Confirm"
    override val delete = "Delete"
    override val addModel = "Add Model"
    override val editModel = "Edit Model"
    override val configureModel = "Configure API & Models"
    override val noModelConfig = "No model configured. Please add one."
    override val defaultModel = "Default Model"
    override val ttsProvider = "TTS Provider"
    override val on = "On"
    override val off = "Off"
    override val appDescription = "AI Character Chat App"
    override val builtWith = "Built with Kotlin Multiplatform"
    override val play = "Play"
    override val stop = "Stop"
    override val createCharacter = "Create Character"
    override val editCharacter = "Edit Character"
    override val createStory = "Create Story"
    override val editStory = "Edit Story"
    override val name = "Name"
    override val description = "Description"
    override val requiredField = "This field is required"
    override val copy = "Copy"
    override val regenerate = "Regenerate"
    override val rewindToHere = "Rewind to here"
    override val deleteMessage = "Delete message"
    override val messageActions = "Message actions"
    override val stopGenerating = "Stop generating"
    override val exportChat = "Export chat"
    override val exportSuccess = "Export successful"
    override val exportFailed = "Export failed"
    override val clearChat = "Clear chat"
    override val chatCleared = "Chat cleared"
    override val chatExported = "Chat exported"
    override val language = "Language"
    override val chinese = "Chinese"
    override val english = "English"
    override val nickname = "Nickname"
    override val avatar = "Avatar"
    override val themeMode = "Theme mode"
    override val followSystem = "Follow system"
    override val lightMode = "Light mode"
    override val darkMode = "Dark mode"
    override val configPresets = "Config presets"
    override val savePreset = "Save as preset"
    override val loadPreset = "Load preset"
    override val deletePreset = "Delete preset"
    override val presetName = "Preset name"
    override val noPresets = "No presets"
    override val fetchModels = "Fetch models"
    override val fetchingModels = "Fetching..."
    override val fetchModelsFailed = "Failed to fetch models"
    override val selectModel = "Select model"
    override val modelConfig = "Model Config"
    override val textModel = "Text Model"
    override val voiceModel = "Voice Model"
    override val provider = "Provider"
    override val customApi = "Custom API URL"
    override val clear = "Clear"
    override val modelCount = ""
    override val techStack = "Kotlin Multiplatform + Compose"
    override val openSource = "Open Source"
    override val tapToEdit = "Tap to edit nickname"
    override val configPreset = "Config Presets"
    override val presetCount = ""
    override val saveCurrentConfig = "Save current config"
    override val saved = "Saved"
    override val cleared = "Cleared"
    override val switchedConfig = "Config switched"
    override val deleted = "Deleted"
    override val pleaseInput = "Please input"
    override val pleaseFillApi = "Please fill API URL and Key first"
    override val noModelsFound = "No models found"
    override val modelsFetched = "%d models fetched"
    override val cropImage = "Crop Image"
    override val backgroundImage = "Background"
    override val zoomIn = "Zoom In"
    override val zoomOut = "Zoom Out"
    override val preview = "Preview"
}

val LocalStrings: ProvidableCompositionLocal<AppStrings> = compositionLocalOf { ZhStrings }

@Composable
@ReadOnlyComposable
fun strings(): AppStrings = LocalStrings.current

@Composable
fun rememberStrings(locale: String? = null): AppStrings {
    return remember(locale) {
        when {
            locale == null -> ZhStrings
            locale.startsWith("zh") -> ZhStrings
            else -> EnStrings
        }
    }
}

@Composable
fun ProvideStrings(strings: AppStrings, content: @Composable () -> Unit) {
    androidx.compose.runtime.CompositionLocalProvider(LocalStrings provides strings) {
        content()
    }
}
