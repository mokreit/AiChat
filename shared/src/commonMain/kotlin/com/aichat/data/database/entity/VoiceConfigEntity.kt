package com.aichat.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_configs")
data class VoiceConfigEntity(
    @PrimaryKey val id: String,
    val provider: String,
    val name: String,
    val baseUrl: String = "",
    val apiKey: String = "",
    val modelName: String = "",
    val voiceId: String = "",
    val enabled: Boolean = true,
)
