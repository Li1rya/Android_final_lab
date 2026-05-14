package com.example.animalwiki.repository

import com.example.animalwiki.data.database.AnimalDao
import com.example.animalwiki.data.model.Animal
import com.example.animalwiki.data.model.toAnimal
import com.example.animalwiki.data.network.AnimalApiService
import com.example.animalwiki.data.repository.AnimalRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals

class AnimalRepositoryTest {
    private val mockDao: AnimalDao = mockk()
    private val mockApi: AnimalApiService = mockk()
    private lateinit var repository: AnimalRepository

    private val testAnimals = listOf(
        Animal(1, "老虎", "猫科", "哺乳类"),
        Animal(2, "老鹰", "猛禽", "鸟类"),
        Animal(3, "大象", "哺乳类", "哺乳类")
    )

    @Before
    fun setup() {
        repository = AnimalRepository(mockDao, mockApi)
    }

    @Test
    fun filterMammalAnimals() {
        val result = repository.filterAnimalsByCategory(testAnimals, "哺乳类")
        assertEquals(2, result.size)
    }

    @Test
    fun filterBirdAnimals() {
        val result = repository.filterAnimalsByCategory(testAnimals, "鸟类")
        assertEquals(1, result.size)
    }

    @Test
    fun networkErrorReturnLocalData() = runTest {
        val localEntity = com.example.animalwiki.data.database.entity.AnimalEntity(1, "老虎", "猫科", "哺乳类")
        coEvery { mockDao.getAnimalById(1) } returns localEntity
        coEvery { mockApi.getAnimalById(1) } throws Exception("Network Error")

        val result = repository.getAnimalById(1)
        assertEquals(localEntity.toAnimal(), result)
    }
}