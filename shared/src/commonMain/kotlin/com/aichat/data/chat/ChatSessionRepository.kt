package com.aichat.data.chat

import com.aichat.data.database.dao.ChatSessionDao
import com.aichat.data.database.dao.MessageDao
import com.aichat.data.database.entity.ChatSessionEntity
import com.aichat.data.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

class ChatSessionRepository(
    private val chatSessionDao: ChatSessionDao,
    private val messageDao: MessageDao,
) {
    fun getAllSessions(): Flow<List<ChatSessionEntity>> = chatSessionDao.getAll()

    fun getSessionsByCharacter(characterId: String): Flow<List<ChatSessionEntity>> =
        chatSessionDao.getByCharacterId(characterId)

    suspend fun getSessionById(id: String): ChatSessionEntity? = chatSessionDao.getById(id)

    suspend fun insertSession(session: ChatSessionEntity) = chatSessionDao.insert(session)

    suspend fun updateSession(session: ChatSessionEntity) = chatSessionDao.update(session)

    suspend fun deleteSession(id: String) = chatSessionDao.deleteById(id)

    fun getMessagesBySession(sessionId: String): Flow<List<MessageEntity>> =
        messageDao.getBySessionId(sessionId)

    suspend fun getRecentMessages(sessionId: String, limit: Int = 50): List<MessageEntity> =
        messageDao.getRecentBySessionId(sessionId, limit)

    suspend fun insertMessage(message: MessageEntity) = messageDao.insert(message)

    suspend fun insertMessages(messages: List<MessageEntity>) = messageDao.insertAll(messages)

    suspend fun updateMessage(message: MessageEntity) = messageDao.update(message)

    suspend fun deleteMessage(id: String) = messageDao.deleteById(id)

    suspend fun deleteMessagesBySession(sessionId: String) = messageDao.deleteBySessionId(sessionId)

    suspend fun deleteMessagesAfter(sessionId: String, timestamp: Long) =
        messageDao.deleteAfterTimestamp(sessionId, timestamp)
}
