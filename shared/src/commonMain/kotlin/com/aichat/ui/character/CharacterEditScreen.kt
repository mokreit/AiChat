package com.aichat.ui.character

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.aichat.data.character.CharacterRepository
import com.aichat.data.database.entity.CharacterEntity
import com.aichat.data.model.ModelConfigRepository
import com.aichat.data.voice.VoiceConfigRepository
import com.aichat.design.AiChatTypography
import com.aichat.design.strings
import com.aichat.platform.FilePicker
import com.aichat.platform.loadImageFromFile
import com.aichat.platform.saveImageBitmap
import com.aichat.ui.common.ImageCropDialog
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterEditScreen(
    characterId: String?,
    onBack: () -> Unit,
    characterRepository: CharacterRepository = koinInject(),
    modelConfigRepository: ModelConfigRepository = koinInject(),
    voiceConfigRepository: VoiceConfigRepository = koinInject(),
) {
    val s = strings()
    val scope = rememberCoroutineScope()
    val isEdit = characterId != null

    var name by remember { mutableStateOf("") }
    var avatarUri by remember { mutableStateOf("") }
    var backgroundImage by remember { mutableStateOf("") }
    var firstMessage by remember { mutableStateOf("") }
    var systemPrompt by remember { mutableStateOf("") }
    var voiceId by remember { mutableStateOf("") }
    var modelConfigId by remember { mutableStateOf("") }
    var voiceConfigId by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var originalCreatedAt by remember { mutableStateOf(0L) }

    var avatarBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var bgBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var cropSource by remember { mutableStateOf<ImageBitmap?>(null) }
    var cropForAvatar by remember { mutableStateOf(false) }

    val modelConfigs by modelConfigRepository.getAllConfigs().collectAsState(initial = emptyList())
    val voiceConfigs by voiceConfigRepository.getAllConfigs().collectAsState(initial = emptyList())

    if (isEdit) {
        val characters by characterRepository.getAllCharacters().collectAsState(initial = emptyList())
        val character = characters.find { it.id == characterId }
        LaunchedEffect(character) {
            character?.let {
                name = it.name
                avatarUri = it.avatarUri
                backgroundImage = it.backgroundImage
                firstMessage = it.firstMessage
                systemPrompt = it.systemPrompt
                voiceId = it.voiceId
                modelConfigId = it.modelConfigId
                voiceConfigId = it.voiceConfigId
                originalCreatedAt = it.createdAt
            }
        }
    }

    LaunchedEffect(avatarUri) {
        if (avatarUri.isNotBlank()) {
            avatarBitmap = loadImageFromFile(avatarUri)
        }
    }
    LaunchedEffect(backgroundImage) {
        if (backgroundImage.isNotBlank()) {
            bgBitmap = loadImageFromFile(backgroundImage)
        }
    }

    cropSource?.let { src ->
        ImageCropDialog(
            bitmap = src,
            aspectRatio = if (cropForAvatar) 1f else null,
            circularPreview = cropForAvatar,
            onConfirm = { cropped ->
                val filename = if (cropForAvatar) "char_avatar_${characterId ?: "new"}_${System.currentTimeMillis()}"
                else "char_bg_${characterId ?: "new"}_${System.currentTimeMillis()}"
                val saved = saveImageBitmap(cropped, filename)
                if (saved != null) {
                    if (cropForAvatar) avatarUri = saved else backgroundImage = saved
                }
                cropSource = null
            },
            onDismiss = { cropSource = null },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) s.editCharacter else s.createCharacter) },
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
            // Avatar picker
            Text(s.avatar, style = AiChatTypography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary)
                        .clickable {
                            scope.launch {
                                val picker = FilePicker()
                                val uri = picker.pickImage() ?: return@launch
                                val bitmap = loadImageFromFile(uri) ?: return@launch
                                cropSource = bitmap
                                cropForAvatar = true
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (avatarBitmap != null) {
                        Image(
                            bitmap = avatarBitmap!!,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Text(
                            text = name.take(1).uppercase().ifBlank { "?" },
                            style = AiChatTypography.titleLarge,
                            color = Color.White,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(s.avatar, style = AiChatTypography.bodyLarge)
            }

            // Background image picker
            Text(s.backgroundImage, style = AiChatTypography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 96.dp, height = 54.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            scope.launch {
                                val picker = FilePicker()
                                val uri = picker.pickImage() ?: return@launch
                                val bitmap = loadImageFromFile(uri) ?: return@launch
                                cropSource = bitmap
                                cropForAvatar = false
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (bgBitmap != null) {
                        Image(
                            bitmap = bgBitmap!!,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Text(
                            text = s.notConfigured,
                            style = AiChatTypography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(s.backgroundImage, style = AiChatTypography.bodyLarge)
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = { Text("${s.name} *") },
                isError = nameError,
                supportingText = if (nameError) {{ Text(s.requiredField) }} else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = firstMessage,
                onValueChange = { firstMessage = it },
                label = { Text(s.firstMessage) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
            )
            OutlinedTextField(
                value = systemPrompt,
                onValueChange = { systemPrompt = it },
                label = { Text(s.systemPrompt) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 6,
            )
            // Voice model selector
            var voiceMenuExpanded by remember { mutableStateOf(false) }
            val selectedVoiceName = voiceConfigs.find { it.id == voiceConfigId }?.let {
                it.name.ifBlank { it.voiceId.ifBlank { it.provider } }
            } ?: s.notConfigured

            ExposedDropdownMenuBox(
                expanded = voiceMenuExpanded,
                onExpandedChange = { voiceMenuExpanded = !voiceMenuExpanded },
            ) {
                OutlinedTextField(
                    value = selectedVoiceName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(s.voiceModel) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = voiceMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                )
                ExposedDropdownMenu(
                    expanded = voiceMenuExpanded,
                    onDismissRequest = { voiceMenuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(s.notConfigured) },
                        onClick = {
                            voiceConfigId = ""
                            voiceMenuExpanded = false
                        },
                    )
                    voiceConfigs.forEach { config ->
                        DropdownMenuItem(
                            text = { Text(config.name.ifBlank { config.voiceId.ifBlank { config.provider } }) },
                            onClick = {
                                voiceConfigId = config.id
                                voiceMenuExpanded = false
                            },
                        )
                    }
                }
            }

            // Model config selector
            var modelMenuExpanded by remember { mutableStateOf(false) }
            val selectedModelName = modelConfigs.find { it.id == modelConfigId }?.let {
                it.name.ifBlank { it.modelName }
            } ?: s.defaultModel

            ExposedDropdownMenuBox(
                expanded = modelMenuExpanded,
                onExpandedChange = { modelMenuExpanded = !modelMenuExpanded },
            ) {
                OutlinedTextField(
                    value = selectedModelName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(s.aiModel) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                )
                ExposedDropdownMenu(
                    expanded = modelMenuExpanded,
                    onDismissRequest = { modelMenuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(s.defaultModel) },
                        onClick = {
                            modelConfigId = ""
                            modelMenuExpanded = false
                        },
                    )
                    modelConfigs.forEach { config ->
                        DropdownMenuItem(
                            text = { Text(config.name.ifBlank { config.modelName }) },
                            onClick = {
                                modelConfigId = config.id
                                modelMenuExpanded = false
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    scope.launch {
                        val now = System.currentTimeMillis()
                        val entity = CharacterEntity(
                            id = characterId ?: java.util.UUID.randomUUID().toString(),
                            name = name,
                            avatarUri = avatarUri,
                            backgroundImage = backgroundImage,
                            description = "",
                            personality = "",
                            scenario = "",
                            firstMessage = firstMessage,
                            systemPrompt = systemPrompt,
                            voiceDesignPrompt = "",
                            voiceId = "",
                            voiceConfigId = voiceConfigId,
                            modelConfigId = modelConfigId,
                            createdAt = if (isEdit) originalCreatedAt else now,
                            updatedAt = now,
                        )
                        characterRepository.insertCharacter(entity)
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
