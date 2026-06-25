package com.aichat.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.aichat.design.strings
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

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
            .background(MaterialTheme.colorScheme.tertiary),
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
    val bgColor by animateColorAsState(
        targetValue = if (isUser) AiChatColors.chatBubbleUser else AiChatColors.chatBubbleAssistant,
        animationSpec = tween(200),
    )
    val textColor = if (isUser) AiChatColors.chatBubbleUserText else AiChatColors.chatBubbleAssistantText
    val darkTheme = LocalDarkTheme.current
    val adjustedBg = if (!isUser && darkTheme) Color(0xFF2A2A2A) else bgColor
    val adjustedText = if (!isUser && darkTheme) Color(0xFFE0E0E0) else textColor

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (isUser) {
            // User avatar on right
            Spacer(modifier = Modifier.weight(1f).fillMaxWidth(0.2f))
        } else {
            // Character avatar on left
            if (avatarName.isNotBlank()) {
                Box(modifier = Modifier.padding(start = 8.dp)) {
                    CharacterAvatar(
                        name = avatarName,
                        avatarUri = avatarUri,
                        modifier = Modifier.size(40.dp),
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
        }

        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp,
            ),
            colors = CardDefaults.cardColors(containerColor = adjustedBg),
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(vertical = 4.dp),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (isUser) {
                    Text(
                        text = text,
                        style = AiChatTypography.bodyMedium,
                        color = adjustedText,
                    )
                } else {
                    // Parse segments: dialogue (black) / action (gray) / meta (gray)
                    val segments = parseContentSegments(text)
                    Text(
                        text = buildAnnotatedString {
                            segments.forEach { seg ->
                                val color = when (seg.type) {
                                    "dialogue" -> Color(0xFF333333)
                                    else -> Color(0xFF999999)
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
                        modifier = Modifier.size(32.dp),
                        enabled = !isSynthesizing,
                    ) {
                        if (isPlaying || isSynthesizing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = AiChatColors.voiceWave,
                            )
                        } else {
                            Text("\u25B6", fontSize = androidx.compose.ui.unit.TextUnit(14f, androidx.compose.ui.unit.TextUnitType.Sp))
                        }
                    }
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(4.dp))
            // User avatar
            UserAvatar(
                avatarUri = userAvatarUri,
                modifier = Modifier.size(40.dp).padding(end = 8.dp),
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
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder ?: strings().typeMessage, style = AiChatTypography.bodyMedium) },
        maxLines = 4,
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        ),
        trailingIcon = {
            if (isSending && onStop != null) {
                Button(
                    onClick = onStop,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                    modifier = Modifier.padding(4.dp),
                ) {
                    Text("\u25A0", fontSize = androidx.compose.ui.unit.TextUnit(14f, androidx.compose.ui.unit.TextUnitType.Sp))
                }
            } else {
                Button(
                    onClick = onSend,
                    enabled = value.isNotBlank() && !isSending,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                    ),
                    modifier = Modifier.padding(4.dp),
                ) {
                    Text("\u27A4", fontSize = androidx.compose.ui.unit.TextUnit(16f, androidx.compose.ui.unit.TextUnitType.Sp))
                }
            }
        },
    )
}

// Content segment parsing for dialogue/action/meta (like original project)
// [...] = meta (not read by TTS, gray)
// （...）或(...) = action (not read by TTS, gray)
// rest = dialogue (read by TTS, dark)
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AiChatTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = AiChatTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
