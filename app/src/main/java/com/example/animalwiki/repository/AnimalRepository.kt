package com.example.animalwiki.repository

import com.example.animalwiki.database.AnimalDao
import com.example.animalwiki.database.AnimalEntity
import com.example.animalwiki.model.Animal
import com.example.animalwiki.network.AnimalApiService

class AnimalRepository(
    private val animalDao: AnimalDao,
    private val animalApi: AnimalApiService
) {
    // 数据转换：网络模型 → 数据库实体
    private fun Animal.toEntity(): AnimalEntity {
        return AnimalEntity(
            id = this.id,
            name = this.title,
            description = this.body
        )
    }

    // 核心逻辑：先本地 → 后网络
    suspend fun getAnimalList(forceRefresh: Boolean = false): List<AnimalEntity> {
        if (!forceRefresh) {
            val local = animalDao.getAllAnimals()
            if (local.isNotEmpty()) return local
        }

        val remote = animalApi.getAnimalList()
        val entities = remote.map { it.toEntity() }

        animalDao.insertAnimals(entities)
        return entities
    }

    // 获取详情：先本地后网络
    suspend fun getAnimalDetail(id: Int): AnimalEntity? {
        val local = animalDao.getAnimalById(id)
        if (local != null) return local

        val remote = animalApi.getAnimalDetail(id)
        val entity = remote.toEntity()

        animalDao.insertAnimals(listOf(entity))
        return entity
    }
}