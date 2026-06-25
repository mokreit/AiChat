package com.aichat.data.character

import com.aichat.data.database.dao.CharacterDao
import com.aichat.data.database.entity.CharacterEntity
import kotlinx.coroutines.flow.Flow

class CharacterRepository(private val characterDao: CharacterDao) {
    fun getAllCharacters(): Flow<List<CharacterEntity>> = characterDao.getAll()

    suspend fun getCharacterById(id: String): CharacterEntity? = characterDao.getById(id)

    suspend fun insertCharacter(character: CharacterEntity) = characterDao.insert(character)

    suspend fun updateCharacter(character: CharacterEntity) = characterDao.update(character)

    suspend fun deleteCharacter(id: String) = characterDao.deleteById(id)

    suspend fun ensureBuiltinCharacters() {
        for (character in DefaultCharacters.all) {
            if (characterDao.getById(character.id) == null) {
                characterDao.insert(character)
            }
        }
    }
}
