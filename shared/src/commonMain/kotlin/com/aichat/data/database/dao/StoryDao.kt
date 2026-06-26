package com.aichat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aichat.data.database.entity.StoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {
    @Query("SELECT * FROM stories ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<StoryEntity>>

    @Query("SELECT * FROM stories WHERE id = :id")
    suspend fun getById(id: String): StoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(story: StoryEntity)

    @Query("DELETE FROM stories WHERE id = :id")
    suspend fun deleteById(id: String)
}
