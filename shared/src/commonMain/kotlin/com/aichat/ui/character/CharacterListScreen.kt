package com.aichat.ui.character

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aichat.data.character.CharacterRepository
import com.aichat.data.database.entity.CharacterEntity
import com.aichat.design.AiChatTypography
import com.aichat.design.BottomNavBar
import com.aichat.design.CharacterAvatar
import com.aichat.design.strings
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Surface
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterListScreen(
    onCharacterClick: (String) -> Unit,
    onStoriesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddCharacterClick: () -> Unit,
    onEditCharacterClick: (String) -> Unit,
    onSessionsClick: () -> Unit = {},
    characterRepository: CharacterRepository = koinInject(),
) {
    val s = strings()
    val characters by characterRepository.getAllCharacters().collectAsState(initial = emptyList())
    var deleteTarget by remember { mutableStateOf<CharacterEntity?>(null) }
    val scope = rememberCoroutineScope()

    // Delete confirmation dialog
    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(s.deleteMessage) },
            text = { Text("确定要删除角色「${deleteTarget!!.name}」吗？") },
            confirmButton = {
                TextButton(onClick = {
                    val id = deleteTarget!!.id
                    deleteTarget = null
                    scope.launch { characterRepository.deleteCharacter(id) }
                }) { Text(s.confirm) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text(s.cancel) }
            },
        )
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            // Exact from 1.html: flex items-center justify-between px-4 py-3 bg-white border-b border-violet-50
            Surface(
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)), // border-violet-50
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = s.appName,
                        style = AiChatTypography.titleLarge.copy(fontSize = 20.sp), // text-xl font-semibold
                        color = Color(0xFF111827), // text-gray-900
                    )
                    IconButton(
                        onClick = onAddCharacterClick,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = s.addModel,
                            tint = Color(0xFF000000), // text-violet-600
                        )
                    }
                }
            }
        },
        bottomBar = {
            BottomNavBar(
                currentTab = "messages",
                onTabClick = { tab ->
                    when (tab) {
                        "stories" -> onStoriesClick()
                        "settings" -> onSettingsClick()
                    }
                },
            )
        },
    ) { padding ->
        if (characters.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = s.noCharacters,
                    style = AiChatTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = s.addCharactersHint,
                    style = AiChatTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(characters, key = { it.id }) { character ->
                    SwipeToDismissItem(
                        character = character,
                        onClick = { onCharacterClick(character.id) },
                        onEditClick = { onEditCharacterClick(character.id) },
                        onDelete = { deleteTarget = character },
                    )
                }
            }
        }
    }
}

@Composable
private fun SwipeToDismissItem(
    character: CharacterEntity,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                false // don't dismiss automatically, wait for dialog
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFEF4444)) // red-500
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
    ) {
        CharacterCard(
            character = character,
            onClick = onClick,
            onEditClick = onEditClick,
        )
    }
}

@Composable
private fun CharacterCard(
    character: CharacterEntity,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CharacterAvatar(name = character.name, avatarUri = character.avatarUri, modifier = Modifier.size(44.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = character.name,
                    style = AiChatTypography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                    color = Color(0xFF111827),
                )
                Text(
                    text = character.description.ifBlank { "" },
                    style = AiChatTypography.bodySmall,
                    color = Color(0xFF9CA3AF),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(
                onClick = onEditClick,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
