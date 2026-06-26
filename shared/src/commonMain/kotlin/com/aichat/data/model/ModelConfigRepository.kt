package com.aichat.data.model

import com.aichat.data.database.dao.ModelConfigDao
import com.aichat.data.database.entity.ModelConfigEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ModelConfigRepository(private val modelConfigDao: ModelConfigDao) {
    fun getAllConfigs(): Flow<List<ModelConfigEntity>> = modelConfigDao.getAll()

    fun getEnabledConfigs(): Flow<List<ModelConfigEntity>> = modelConfigDao.getEnabled()

    suspend fun getConfigById(id: String): ModelConfigEntity? = modelConfigDao.getById(id)

    suspend fun getDefaultConfig(): ModelConfigEntity? {
        return modelConfigDao.getEnabled().first().firstOrNull()
    }

    suspend fun insertConfig(config: ModelConfigEntity) = modelConfigDao.insert(config)

    suspend fun updateConfig(config: ModelConfigEntity) = modelConfigDao.update(config)

    suspend fun deleteConfig(id: String) = modelConfigDao.deleteById(id)
}
