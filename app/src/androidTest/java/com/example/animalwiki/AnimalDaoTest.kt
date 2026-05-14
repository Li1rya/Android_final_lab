package com.example.animalwiki

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.animalwiki.data.database.AnimalDao
import com.example.animalwiki.data.database.AnimalDatabase
import com.example.animalwiki.data.database.entity.AnimalEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AnimalDaoTest {
    private lateinit var db: AnimalDatabase
    private lateinit var dao: AnimalDao

    private val tiger = AnimalEntity(1, "老虎", "大型猫科食肉动物", "哺乳类")
    private val eagle = AnimalEntity(2, "老鹰", "猛禽之王", "鸟类")

    @Before
    fun initDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AnimalDatabase::class.java
        ).allowMainThreadQueries()
            .build()
        dao = db.animalDao()
    }

    @After
    fun closeDb() = db.close()

    @Test
    fun insertAndGetAnimal() = runTest {
        dao.insertAnimal(tiger)
        val result = dao.getAnimalById(1)
        Assert.assertEquals(tiger.id, result?.id)
        Assert.assertEquals(tiger.name, result?.name)
    }

    @Test
    fun deleteAnimalById() = runTest {
        dao.insertAnimal(eagle)
        val deleteCount = dao.deleteAnimalById(2)
        Assert.assertEquals(1, deleteCount)
        Assert.assertNull(dao.getAnimalById(2))
    }

    @Test
    fun insertAllAndGetAll() = runTest {
        dao.insertAllAnimals(listOf(tiger, eagle))
        val all = dao.getAllAnimals()
        Assert.assertEquals(2, all.size)
    }
}