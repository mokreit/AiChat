package com.aichat.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey val id: String,
    val characterId: String,
    val characterName: String = "",
    val type: String = "character",
    val lastMessage: String = "",
    val lastTime: Long = 0L,
    val createdAt: Long,
    val updatedAt: Long = 0L,
)
