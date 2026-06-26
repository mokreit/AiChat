package com.aichat.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import com.aichat.design.strings
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CharacterAvatar(
    name: String,
    avatarUri: String = "",
    modifier: Modifier = Modifier,
) {
    val bitmap = if (avatarUri.isNotBlank()) {
        val cachedBitmap = remember(avatarUri) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
        LaunchedEffect(avatarUri) {
            if (cachedBitmap.value == null) {
                try {
                    cachedBitmap.value = com.aichat.platform.loadImageFromFile(avatarUri)
                } catch (_: Exception) {}
            }
        }
        cachedBitmap.value
    } else {
        null
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(0xFF000000)),  // black
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier.matchParentSize().clip(CircleShape),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            )
        } else {
            Text(
                text = name.take(1).uppercase(),
                style = AiChatTypography.titleLarge,
                color = Color.White,
            )
        }
    }
}

@Composable
fun UserAvatar(
    modifier: Modifier = Modifier,
    avatarUri: String = ""
) {
    var bitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    if (avatarUri.isNotBlank()) {
        androidx.compose.runtime.LaunchedEffect(avatarUri) {
            try {
                bitmap = com.aichat.platform.loadImageFromFile(avatarUri)
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondary),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = bitmap!!,
                contentDescription = null,
                modifier = Modifier.matchParentSize().clip(CircleShape),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            )
        } else {
            Text(
                text = "U",
                style = AiChatTypography.titleLarge,
                color = Color.White,
            )
        }
    }
}

/** AI-Native 3-dot typing indicator */
@Composable
fun TypingIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val dots = listOf(
        infiniteTransition.animateFloat(
            initialValue = 0.3f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(400), RepeatMode.Reverse),
            label = "dot1"
        ),
        infiniteTransition.animateFloat(
            initialValue = 0.3f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(400, delayMillis = 150), RepeatMode.Reverse),
            label = "dot2"
        ),
        infiniteTransition.animateFloat(
            initialValue = 0.3f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(400, delayMillis = 300), RepeatMode.Reverse),
            label = "dot3"
        ),
    )
    Row(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        dots.forEach { alpha ->
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(AiChatColors.typingDot.copy(alpha = alpha.value))
            )
        }
    }
}

@Composable
fun ChatBubble(
    text: String,
    isUser: Boolean,
    isPlaying: Boolean = false,
    isSynthesizing: Boolean = false,
    onPlayClick: (() -> Unit)? = null,
    avatarName: String = "",
    avatarUri: String = "",
    userAvatarUri: String = "",
    modifier: Modifier = Modifier,
) {
    val darkTheme = LocalDarkTheme.current

    // AI-Native bubble colors
    val bubbleBg = when {
        isUser -> AiChatColors.chatBubbleUser
        darkTheme -> AiChatColors.chatBubbleAssistantDark
        else -> AiChatColors.chatBubbleAssistant
    }
    val bubbleText = when {
        isUser -> AiChatColors.chatBubbleUserText
        darkTheme -> AiChatColors.chatBubbleAssistantDarkText
        else -> AiChatColors.chatBubbleAssistantText
    }
    val bubbleBorder = when {
        isUser -> null
        darkTheme -> BorderStroke(1.dp, AiChatColors.chatBubbleAssistantDarkBorder)
        else -> BorderStroke(1.dp, AiChatColors.chatBubbleAssistantBorder)
    }

    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (isUser) {
            Spacer(modifier = Modifier.weight(1f).fillMaxWidth(0.15f))
        } else {
            if (avatarName.isNotBlank()) {
                Box(modifier = Modifier.padding(start = 8.dp, end = 4.dp)) {
                    CharacterAvatar(
                        name = avatarName,
                        avatarUri = avatarUri,
                        modifier = Modifier.size(36.dp),
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isUser) 20.dp else 6.dp,
                bottomEnd = if (isUser) 6.dp else 20.dp,
            ),
            color = bubbleBg,
            border = bubbleBorder,
            shadowElevation = 0.dp,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(vertical = 2.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                if (isUser) {
                    Text(
                        text = text,
                        style = AiChatTypography.bodyMedium,
                        color = bubbleText,
                    )
                } else {
                    val segments = parseContentSegments(text)
                    Text(
                        text = buildAnnotatedString {
                            segments.forEach { seg ->
                                val color = when (seg.type) {
                                    "dialogue" -> if (darkTheme) Color(0xFFCBD5E1) else Color(0xFF334155)
                                    else -> if (darkTheme) Color(0xFF64748B) else Color(0xFF94A3B8)
                                }
                                withStyle(androidx.compose.ui.text.SpanStyle(color = color)) {
                                    append(seg.text)
                                }
                            }
                        },
                        style = AiChatTypography.bodyMedium,
                    )
                }
                if (onPlayClick != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    IconButton(
                        onClick = onPlayClick,
                        modifier = Modifier.size(28.dp),
                        enabled = !isSynthesizing,
                    ) {
                        if (isPlaying || isSynthesizing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = AiChatColors.voiceWave,
                            )
                        } else {
                            Text("\u25B6", fontSize = 12.sp, color = AiChatColors.aiAccent)
                        }
                    }
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(4.dp))
            UserAvatar(
                avatarUri = userAvatarUri,
                modifier = Modifier.size(36.dp).padding(end = 8.dp),
            )
        }
    }
}

@Composable
fun ChatInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean = false,
    onStop: (() -> Unit)? = null,
    placeholder: String? = null,
    modifier: Modifier = Modifier,
    onVoiceToggle: (() -> Unit)? = null,
    isVoiceMode: Boolean = false,
    onVoicePressStart: (() -> Unit)? = null,
    onVoicePressEnd: (() -> Unit)? = null,
    onVoicePressCancel: (() -> Unit)? = null,
    onSuggest: (() -> Unit)? = null,
    isSuggesting: Boolean = false,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (isVoiceMode) {
            var dragOffsetY by remember { mutableStateOf(0f) }
            var isRecordingActive by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onVoiceToggle ?: {}) {
                    Icon(
                        imageVector = Icons.Filled.Keyboard,
                        contentDescription = "Keyboard",
                        tint = AiChatColors.aiAccent,
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            if (isRecordingActive) AiChatColors.aiAccent.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                        )
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    dragOffsetY = 0f
                                    isRecordingActive = true
                                    onVoicePressStart?.invoke()
                                },
                                onDragEnd = {
                                    if (isRecordingActive) {
                                        isRecordingActive = false
                                        if (dragOffsetY < -160f) {
                                            onVoicePressCancel?.invoke()
                                        } else {
                                            onVoicePressEnd?.invoke()
                                        }
                                    }
                                },
                                onDragCancel = {
                                    if (isRecordingActive) {
                                        isRecordingActive = false
                                        onVoicePressCancel?.invoke()
                                    }
                                },
                                onDrag = { _, dragAmount -> dragOffsetY += dragAmount.y }
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (isRecordingActive) {
                            if (dragOffsetY < -160f) "\u677E\u5F00\u53D6\u6D88" else "\u677E\u5F00\u53D1\u9001"
                        } else "\u6309\u4F4F\u8BF4\u8BDD",
                        style = AiChatTypography.bodyLarge,
                        color = if (isRecordingActive) AiChatColors.aiAccent
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            // AI-Native clean input field
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Suggest button
                    if (onSuggest != null) {
                        IconButton(
                            onClick = onSuggest,
                            enabled = !isSuggesting && !isSending,
                            modifier = Modifier.size(40.dp),
                        ) {
                            if (isSuggesting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = AiChatColors.aiAccent,
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = "\u63A8\u8350\u56DE\u590D",
                                    tint = AiChatColors.aiAccent,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                    // Text input
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 40.dp),
                        textStyle = AiChatTypography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        cursorBrush = SolidColor(AiChatColors.aiAccent),
                        maxLines = 4,
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                if (value.isEmpty()) {
                                    Text(
                                        text = placeholder ?: strings().typeMessage,
                                        style = AiChatTypography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                    // Voice toggle
                    if (onVoiceToggle != null) {
                        IconButton(
                            onClick = onVoiceToggle,
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Mic,
                                contentDescription = "Voice",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    // Send / Stop button
                    if (isSending && onStop != null) {
                        IconButton(
                            onClick = onStop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error),
                        ) {
                            Text("\u25A0", color = Color.White, fontSize = 14.sp)
                        }
                    } else {
                        IconButton(
                            onClick = onSend,
                            enabled = value.isNotBlank() && !isSending,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (value.isNotBlank()) AiChatColors.aiAccent
                                    else MaterialTheme.colorScheme.outline,
                                ),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

// Content segment parsing
private data class ContentSegment(val type: String, val text: String)

private fun parseContentSegments(content: String): List<ContentSegment> {
    val segments = mutableListOf<ContentSegment>()
    val regex = Regex("""(\[[^\]]*\])|（[^）]*）|\([^)]*\)""")
    var lastIndex = 0
    for (match in regex.findAll(content)) {
        if (match.range.first > lastIndex) {
            segments.add(ContentSegment("dialogue", content.substring(lastIndex, match.range.first)))
        }
        val matched = match.value
        if (matched.startsWith("[")) {
            segments.add(ContentSegment("meta", matched))
        } else {
            segments.add(ContentSegment("action", matched))
        }
        lastIndex = match.range.last + 1
    }
    if (lastIndex < content.length) {
        segments.add(ContentSegment("dialogue", content.substring(lastIndex)))
    }
    return segments.ifEmpty { listOf(ContentSegment("dialogue", content)) }
}

/** Strip action/meta text, keep only dialogue for TTS */
fun stripActionText(content: String): String {
    return content
        .replace(Regex("""\[[^\]]*\]"""), "")
        .replace(Regex("""（[^）]*）"""), "")
        .replace(Regex("""\([^)]*\)"""), "")
        .trim()
}

@Composable
fun BottomNavBar(
    currentTab: String,
    onTabClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Exact from 1.html: border-t border-violet-100 bg-white
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)), // gray-200
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 0.dp,
                    end = 0.dp,
                    top = 12.dp,
                    bottom = 12.dp + WindowInsets.navigationBars
                        .asPaddingValues().calculateBottomPadding(),
                ),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Messages tab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabClick("messages") }
                    .padding(vertical = 0.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = "消息",
                    tint = if (currentTab == "messages") Color(0xFF000000) else Color(0xFF9CA3AF),
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "消息",
                    style = AiChatTypography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                    color = if (currentTab == "messages") Color(0xFF000000) else Color(0xFF9CA3AF),
                )
            }
            // Stories tab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabClick("stories") }
                    .padding(vertical = 0.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoStories,
                    contentDescription = "故事",
                    tint = if (currentTab == "stories") Color(0xFF000000) else Color(0xFF9CA3AF),
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "故事",
                    style = AiChatTypography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                    color = if (currentTab == "stories") Color(0xFF000000) else Color(0xFF9CA3AF),
                )
            }
            // Settings tab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabClick("settings") }
                    .padding(vertical = 0.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "设置",
                    tint = if (currentTab == "settings") Color(0xFF000000) else Color(0xFF9CA3AF),
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "设置",
                    style = AiChatTypography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                    color = if (currentTab == "settings") Color(0xFF000000) else Color(0xFF9CA3AF),
                )
            }
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp), // px-4 py-4
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AiChatTypography.bodyMedium.copy(fontWeight = FontWeight.Medium), // text-sm font-medium
                color = Color(0xFF111827), // text-gray-900
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = AiChatTypography.bodySmall, // text-xs
                    color = Color(0xFF9CA3AF), // text-gray-400
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}
