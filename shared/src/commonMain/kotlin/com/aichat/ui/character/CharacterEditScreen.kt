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
import androidx.compose.material3.Slider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Surface
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
    var backgroundAlpha by remember { mutableFloatStateOf(0.3f) }
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
                backgroundAlpha = it.backgroundAlpha
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
                        text = if (isEdit) s.editCharacter else s.createCharacter,
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
            // Avatar picker
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E7EB)), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(s.avatar, style = AiChatTypography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = Color(0xFF9CA3AF))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF000000))
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
                        Text(s.avatar, style = AiChatTypography.bodyLarge, color = Color(0xFF111827))
                    }
                }
            }

            // Background image picker
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E7EB)), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(s.backgroundImage, style = AiChatTypography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = Color(0xFF9CA3AF))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(width = 96.dp, height = 54.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE5E7EB))
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
                                    contentScale = ContentScale.Fit,
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
                        Text(s.backgroundImage, style = AiChatTypography.bodyLarge, color = Color(0xFF111827))
                    }
                }
            }

            // Background alpha slider
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E7EB)), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "背景透明度：${"%.0f".format(backgroundAlpha * 100)}%",
                        style = AiChatTypography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = Color(0xFF9CA3AF),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Slider(
                            value = backgroundAlpha,
                            onValueChange = { backgroundAlpha = it },
                            valueRange = 0f..1f,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = "%.0f".format(backgroundAlpha * 100),
                            onValueChange = { str ->
                                str.filter { it.isDigit() }.toIntOrNull()?.let { pct ->
                                    backgroundAlpha = (pct.coerceIn(0, 100) / 100f)
                                }
                            },
                            modifier = Modifier.width(64.dp),
                            singleLine = true,
                            suffix = { Text("%", style = AiChatTypography.bodySmall) },
                            textStyle = AiChatTypography.bodySmall,
                        )
                    }
                }
            }

            // Name
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E7EB)), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; nameError = false },
                        label = { Text("${s.name} *") },
                        isError = nameError,
                        supportingText = if (nameError) {{ Text(s.requiredField) }} else null,
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
                }
            }

            // First message
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E7EB)), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = firstMessage,
                        onValueChange = { firstMessage = it },
                        label = { Text(s.firstMessage) },
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
                }
            }

            // System prompt
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E7EB)), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = systemPrompt,
                        onValueChange = { systemPrompt = it },
                        label = { Text(s.systemPrompt) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 6,
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF000000),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            cursorColor = Color(0xFF000000),
                            focusedLabelColor = Color(0xFF000000),
                        ),
                    )
                }
            }

            // Voice model selector
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E7EB)), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                }
            }

            // Model config selector
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E7EB)), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                            backgroundAlpha = backgroundAlpha,
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
