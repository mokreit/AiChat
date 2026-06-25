package com.aichat.ui.character

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aichat.data.character.CharacterRepository
import com.aichat.design.AiChatTypography
import com.aichat.design.CharacterAvatar
import com.aichat.design.strings
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    characterId: String,
    onBack: () -> Unit,
    onStartChat: (String) -> Unit,
    onEdit: (String) -> Unit,
    characterRepository: CharacterRepository = koinInject(),
) {
    val s = strings()
    var character by remember { mutableStateOf<com.aichat.data.database.entity.CharacterEntity?>(null) }
    LaunchedEffect(characterId) {
        character = characterRepository.getCharacterById(characterId)
    }
    val char = character

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(char?.name ?: s.character) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
                    }
                },
                actions = {
                    if (char != null) {
                        IconButton(onClick = { onEdit(char.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = s.editCharacter)
                        }
                        IconButton(onClick = { onStartChat(char.id) }) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = s.chat)
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (char == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(s.characterNotFound, style = AiChatTypography.bodyLarge)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CharacterAvatar(
                    name = char.name,
                    avatarUri = char.avatarUri,
                    modifier = Modifier.size(80.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = char.name,
                    style = AiChatTypography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (char.firstMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = s.firstMessage, style = AiChatTypography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = char.firstMessage, style = AiChatTypography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
