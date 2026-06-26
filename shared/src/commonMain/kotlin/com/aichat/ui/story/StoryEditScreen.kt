package com.aichat.ui.story

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Surface
import com.aichat.data.character.CharacterRepository
import com.aichat.data.database.entity.CharacterEntity
import com.aichat.data.database.entity.StoryEntity
import com.aichat.data.story.StoryRepository
import com.aichat.design.AiChatTypography
import com.aichat.design.CharacterAvatar
import com.aichat.design.strings
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StoryEditScreen(
    storyId: String?,
    onBack: () -> Unit,
    storyRepository: StoryRepository = koinInject(),
    characterRepository: CharacterRepository = koinInject(),
) {
    val s = strings()
    val scope = rememberCoroutineScope()
    val isEdit = storyId != null

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var systemPrompt by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf(false) }
    var originalCreatedAt by remember { mutableStateOf(0L) }
    var selectedCharacterIds by remember { mutableStateOf(setOf<String>()) }

    // Load all characters for selection
    val characters by characterRepository.getAllCharacters().collectAsState(initial = emptyList())

    if (isEdit) {
        val stories by storyRepository.getAllStories().collectAsState(initial = emptyList())
        val story = stories.find { it.id == storyId }
        LaunchedEffect(story) {
            story?.let {
                title = it.title
                description = it.description
                systemPrompt = it.systemPrompt
                originalCreatedAt = it.createdAt
                selectedCharacterIds = it.characterIds.split(",").map { id -> id.trim() }.filter { id -> id.isNotBlank() }.toSet()
            }
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Surface(
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back, tint = Color(0xFF374151))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = if (isEdit) s.editStory else s.createStory,
                        style = AiChatTypography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF111827),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.size(40.dp))
                }
            }
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
            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; titleError = false },
                label = { Text("${s.name} *") },
                isError = titleError,
                supportingText = if (titleError) {{ Text(s.requiredField) }} else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF000000),
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    cursorColor = Color(0xFF000000),
                    focusedLabelColor = Color(0xFF000000),
                ),
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(s.description) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF000000),
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    cursorColor = Color(0xFF000000),
                    focusedLabelColor = Color(0xFF000000),
                ),
            )

            // System prompt
            OutlinedTextField(
                value = systemPrompt,
                onValueChange = { systemPrompt = it },
                label = { Text(s.systemPrompt) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 8,
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF000000),
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    cursorColor = Color(0xFF000000),
                    focusedLabelColor = Color(0xFF000000),
                ),
            )

            // Character selection
            if (characters.isNotEmpty()) {
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E7EB)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = s.participatingCharacters,
                            style = AiChatTypography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = Color(0xFF111827),
                        )
                        Text(
                            text = "${selectedCharacterIds.size} ${s.character}",
                            style = AiChatTypography.bodySmall,
                            color = Color(0xFF9CA3AF),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            for (char in characters) {
                                val isSelected = selectedCharacterIds.contains(char.id)
                                CharacterChip(
                                    character = char,
                                    isSelected = isSelected,
                                    onClick = {
                                        selectedCharacterIds = if (isSelected) {
                                            selectedCharacterIds - char.id
                                        } else {
                                            selectedCharacterIds + char.id
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
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
                            characterIds = selectedCharacterIds.joinToString(","),
                            createdAt = if (isEdit) originalCreatedAt else now,
                            updatedAt = now,
                        )
                        storyRepository.insertStory(entity)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF000000),
                    contentColor = Color.White,
                ),
            ) {
                Text(s.save, style = AiChatTypography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
            }
        }
    }
}

@Composable
private fun CharacterChip(
    character: CharacterEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    // character-chip: bg violet-50, border transparent, selected: border violet-600, bg violet-100
    val borderColor = if (isSelected) Color(0xFF000000) else Color.Transparent
    val bgColor = if (isSelected) {
        Color(0xFFE5E7EB) // violet-100 (primary-container)
    } else {
        Color(0xFFE5E7EB) // violet-50 (surface-variant)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CharacterAvatar(
            name = character.name,
            avatarUri = character.avatarUri,
            modifier = Modifier.size(28.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = character.name,
            style = AiChatTypography.bodyMedium,
            color = Color(0xFF111827), // gray-900
        )
        if (isSelected) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color(0xFF000000),
            )
        }
    }
}
