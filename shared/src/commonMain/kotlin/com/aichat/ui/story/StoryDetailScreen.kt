package com.aichat.ui.story

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aichat.data.story.StoryRepository
import com.aichat.design.AiChatTypography
import com.aichat.design.strings
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryDetailScreen(
    storyId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit = {},
    onStartStoryChat: (String) -> Unit = {},
    storyRepository: StoryRepository = koinInject(),
) {
    val s = strings()
    var story by remember { mutableStateOf<com.aichat.data.database.entity.StoryEntity?>(null) }
    LaunchedEffect(storyId) {
        story = storyRepository.getStoryById(storyId)
    }
    val s2 = story

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s2?.title ?: s.story) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
                    }
                },
                actions = {
                    if (s2 != null) {
                        IconButton(onClick = { onEdit(s2.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = s.editStory)
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (s2 == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                Text(s.storyNotFound, style = AiChatTypography.bodyLarge)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                Text(
                    text = s2.title,
                    style = AiChatTypography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (s2.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = s2.description,
                        style = AiChatTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (s2.systemPrompt.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = s.systemPrompt,
                        style = AiChatTypography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = s2.systemPrompt,
                        style = AiChatTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { onStartStoryChat(s2.id) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(s.chat)
                }
            }
        }
    }
}
