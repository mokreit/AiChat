package com.aichat.ui.story

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aichat.data.database.entity.StoryEntity
import com.aichat.data.story.StoryRepository
import com.aichat.design.strings
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryEditScreen(
    storyId: String?,
    onBack: () -> Unit,
    storyRepository: StoryRepository = koinInject(),
) {
    val s = strings()
    val scope = rememberCoroutineScope()
    val isEdit = storyId != null

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var systemPrompt by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf(false) }
    var originalCreatedAt by remember { mutableStateOf(0L) }

    if (isEdit) {
        val stories by storyRepository.getAllStories().collectAsState(initial = emptyList())
        val story = stories.find { it.id == storyId }
        LaunchedEffect(story) {
            story?.let {
                title = it.title
                description = it.description
                systemPrompt = it.systemPrompt
                originalCreatedAt = it.createdAt
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) s.editStory else s.createStory) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; titleError = false },
                label = { Text("${s.name} *") },
                isError = titleError,
                supportingText = if (titleError) {{ Text(s.requiredField) }} else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(s.description) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
            )
            OutlinedTextField(
                value = systemPrompt,
                onValueChange = { systemPrompt = it },
                label = { Text(s.systemPrompt) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 8,
            )

            Spacer(modifier = Modifier.height(8.dp))

            androidx.compose.material3.Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@Button
                    }
                    scope.launch {
                        val now = System.currentTimeMillis()
                        val entity = StoryEntity(
                            id = storyId ?: java.util.UUID.randomUUID().toString(),
                            title = title,
                            description = description,
                            systemPrompt = systemPrompt,
                            createdAt = if (isEdit) originalCreatedAt else now,
                            updatedAt = now,
                        )
                        storyRepository.insertStory(entity)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(s.save)
            }
        }
    }
}
