package com.example.animalwiki.data.repository


import com.example.animalwiki.data.database.AnimalDao
import com.example.animalwiki.data.model.Animal
import com.example.animalwiki.data.model.ApiAnimal
import com.example.animalwiki.data.model.toAnimal
import com.example.animalwiki.data.model.toAnimalEntity
import com.example.animalwiki.data.network.AnimalApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.collections.filter
import kotlin.collections.map

// 仓库层（单一数据源：先查本地DB，再请求网络更新DB）
class AnimalRepository(
    private val dao: AnimalDao,
    private val apiService: AnimalApiService
) {
    // 根据ID获取动物（挂起函数）
    suspend fun getAnimalById(animalId: Int): Animal? {
        // 1. 先查本地数据库
        val localAnimal = dao.getAnimalById(animalId)
        return try {
            // 2. 网络请求更新数据
            val apiAnimal = apiService.getAnimalById(animalId)
            val entity = apiAnimal.toAnimalEntity()
            dao.insertAnimal(entity)
            // 3. 返回最新数据
            entity.toAnimal()
        } catch (e: Exception) {
            // 网络失败，返回本地数据
            localAnimal?.toAnimal()
        }
    }

    // 根据分类获取动物列表（挂起函数）
    suspend fun getAnimalsByCategory(categoryId: Int): List<Animal> {
        val localAnimals = dao.getAllAnimals()
        return try {
            val apiAnimals = apiService.getAnimalsByCategory(categoryId)
            val entities = apiAnimals.map { it.toAnimalEntity() }
            dao.insertAllAnimals(entities)
            entities.map { it.toAnimal() }
        } catch (e: Exception) {
            localAnimals.map { it.toAnimal() }
        }
    }

    // 数据转换逻辑（用于单元测试）：过滤指定分类的动物
    fun filterAnimalsByCategory(animals: List<Animal>, category: String): List<Animal> {
        return animals.filter { it.category.equals(category, ignoreCase = true) }
    }

    // 添加动物（挂起函数）
    suspend fun addAnimal(animal: Animal): Animal? {
        return withContext(Dispatchers.IO) {
            try {
                val apiAnimal = ApiAnimal(
                    userId = 1,
                    id = animal.id,
                    title = animal.name,
                    body = animal.description
                )
                val createdApiAnimal = apiService.addAnimal(apiAnimal)
                val entity = createdApiAnimal.toAnimalEntity()
                dao.insertAnimal(entity)
                entity.toAnimal()
            } catch (e: Exception) {
                null
            }
        }
    }
}