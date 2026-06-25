package com.aichat.data.voice

import com.aichat.data.database.dao.VoiceConfigDao
import com.aichat.data.database.entity.VoiceConfigEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class VoiceConfigRepository(private val voiceConfigDao: VoiceConfigDao) {
    fun getAllConfigs(): Flow<List<VoiceConfigEntity>> = voiceConfigDao.getAll()

    fun getEnabledConfigs(): Flow<List<VoiceConfigEntity>> = voiceConfigDao.getEnabled()

    suspend fun getConfigById(id: String): VoiceConfigEntity? = voiceConfigDao.getById(id)

    suspend fun getDefaultConfig(): VoiceConfigEntity? {
        return voiceConfigDao.getEnabled().first().firstOrNull()
    }

    suspend fun insertConfig(config: VoiceConfigEntity) = voiceConfigDao.insert(config)

    suspend fun updateConfig(config: VoiceConfigEntity) = voiceConfigDao.update(config)

    suspend fun deleteConfig(id: String) = voiceConfigDao.deleteById(id)
}
