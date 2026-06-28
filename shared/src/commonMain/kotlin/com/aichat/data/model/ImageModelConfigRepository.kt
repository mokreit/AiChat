package com.aichat.data.model

import com.aichat.data.database.dao.ImageModelConfigDao
import com.aichat.data.database.entity.ImageModelConfigEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ImageModelConfigRepository(private val dao: ImageModelConfigDao) {
    fun getAllConfigs(): Flow<List<ImageModelConfigEntity>> = dao.getAll()
    fun getEnabledConfigs(): Flow<List<ImageModelConfigEntity>> = dao.getEnabled()
    suspend fun getConfigById(id: String): ImageModelConfigEntity? = dao.getById(id)
    suspend fun getDefaultConfig(): ImageModelConfigEntity? {
        return dao.getEnabled().first().firstOrNull()
    }
    suspend fun insertConfig(config: ImageModelConfigEntity) = dao.insert(config)
    suspend fun updateConfig(config: ImageModelConfigEntity) = dao.update(config)
    suspend fun deleteConfig(id: String) = dao.deleteById(id)
}
