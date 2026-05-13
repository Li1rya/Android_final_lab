package com.example.animalwiki

import com.example.animalwiki.database.AnimalDao
import com.example.animalwiki.database.AnimalEntity
import com.example.animalwiki.model.Animal
import com.example.animalwiki.network.AnimalApiService
import com.example.animalwiki.repository.AnimalRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class AnimalRepositoryTest {

    // 模拟本地数据库
    private val mockDao = object : AnimalDao {
        private val db = mutableListOf<AnimalEntity>()

        override suspend fun getAllAnimals(): List<AnimalEntity> = db.toList()
        override suspend fun getAnimalById(id: Int): AnimalEntity? = db.find { it.id == id }
        override suspend fun insertAnimals(animals: List<AnimalEntity>) { db.addAll(animals) }
        override suspend fun clearAll() { db.clear() }
    }

    // 模拟网络接口
    private val mockApi = object : AnimalApiService {
        override suspend fun getAnimalList(): List<Animal> {
            return listOf(
                Animal(1, "狮子", "非洲草原的大型猫科动物"),
                Animal(2, "老虎", "亚洲森林的顶级掠食者")
            )
        }

        override suspend fun getAnimalDetail(id: Int): Animal {
            return Animal(id, "测试动物", "测试用动物描述")
        }
    }

    private val repository = AnimalRepository(mockDao, mockApi)

    // 测试1：DAO 增查
    @Test
    fun test_dao_insert_query() = runTest {
        mockDao.clearAll()
        val data = AnimalEntity(1, "熊猫", "中国国宝")
        mockDao.insertAnimals(listOf(data))

        val result = mockDao.getAllAnimals()
        assertEquals(1, result.size)
        assertEquals("熊猫", result[0].name)
    }

    // 测试2：仓库层获取列表
    @Test
    fun test_repository_list() = runTest {
        mockDao.clearAll()
        val list = repository.getAnimalList(forceRefresh = true)
        assertEquals(2, list.size)
    }

    // 测试3：仓库层获取详情
    @Test
    fun test_repository_detail() = runTest {
        mockDao.clearAll()
        val animal = repository.getAnimalDetail(1)
        assertNotNull(animal)
        assertEquals(1, animal?.id)
    }
}