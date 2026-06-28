package com.aichat.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val id: String,
    val name: String,
    val avatarUri: String = "",
    val description: String = "",
    val personality: String = "",
    val scenario: String = "",
    val firstMessage: String = "",
    val systemPrompt: String = "",
    val voiceDesignPrompt: String = "",
    val voiceSampleUri: String = "",
    val ttsProviderId: String = "",
    @ColumnInfo(defaultValue = "") val voiceApiEndpoint: String = "",
    @ColumnInfo(defaultValue = "") val voiceApiKey: String = "",
    @ColumnInfo(defaultValue = "") val voiceModel: String = "",
    @ColumnInfo(defaultValue = "") val voiceId: String = "",
    val backgroundImage: String = "",
    @ColumnInfo(defaultValue = "0") val backgroundDynamic: Boolean = false,
    @ColumnInfo(defaultValue = "0.3") val backgroundAlpha: Float = 0.3f,
    @ColumnInfo(defaultValue = "1.0") val bubbleAlpha: Float = 1.0f,
    @ColumnInfo(defaultValue = "") val userNickname: String = "",
    @ColumnInfo(defaultValue = "") val userAvatar: String = "",
    @ColumnInfo(defaultValue = "") val userDescription: String = "",
    @ColumnInfo(defaultValue = "") val modelConfigId: String = "",
    @ColumnInfo(defaultValue = "") val voiceConfigId: String = "",
    @ColumnInfo(defaultValue = "") val appearance: String = "",
    @ColumnInfo(defaultValue = "") val imageModelConfigId: String = "",
    val createdAt: Long,
    val updatedAt: Long,
)
