package com.aichat.data.story

import com.aichat.data.database.dao.StoryDao
import com.aichat.data.database.entity.StoryEntity
import kotlinx.coroutines.flow.Flow

class StoryRepository(private val storyDao: StoryDao) {
    fun getAllStories(): Flow<List<StoryEntity>> = storyDao.getAll()

    suspend fun getStoryById(id: String): StoryEntity? = storyDao.getById(id)

    suspend fun insertStory(story: StoryEntity) = storyDao.insert(story)

    suspend fun deleteStory(id: String) = storyDao.deleteById(id)

    suspend fun ensureBuiltinStories() {
        for (story in DefaultStories.all) {
            if (storyDao.getById(story.id) == null) {
                storyDao.insert(story)
            }
        }
    }
}
