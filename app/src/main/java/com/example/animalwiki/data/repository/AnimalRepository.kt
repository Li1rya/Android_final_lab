package com.example.animalwiki.data.repository

import android.R.attr.mode
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import com.example.animalwiki.data.database.AnimalDao
import com.example.animalwiki.data.database.AnimalDatabase
import com.example.animalwiki.data.database.FavoriteDao
import com.example.animalwiki.data.database.FavoriteDatabase
import com.example.animalwiki.data.database.HistoryDao
import com.example.animalwiki.data.database.HistoryDatabase
import com.example.animalwiki.data.model.Animal
import com.example.animalwiki.data.model.SearchMode        // ✅ 新增
import com.example.animalwiki.data.database.entity.AnimalEntity
import com.example.animalwiki.data.database.entity.FavoriteEntity
import com.example.animalwiki.data.database.entity.HistoryEntity
import com.example.animalwiki.data.local.AnimalSource
import com.example.animalwiki.data.model.Classification
import com.example.animalwiki.data.model.Favorite
import com.example.animalwiki.data.model.History
import com.example.animalwiki.data.model.JsonAnimal
import kotlinx.coroutines.flow.map

class AnimalRepository private constructor(
    private val context: Context,
    private val animalDao: AnimalDao,
    private val historyDao: HistoryDao,
    private val favoriteDao: FavoriteDao
) {
    private val animalSource = AnimalSource(context)
    private val gson = Gson()
    private val TAG = "AnimalRepository"

    suspend fun initializeDatabase() {
        val count = animalDao.getCount()
        if (count == 0) {
            Log.d(TAG, "数据库为空，开始从 JSON 导入...")
            val jsonAnimals = animalSource.loadAllAnimals()
            val entities = jsonAnimals.map { it.toEntity() }
            animalDao.insertAll(entities)
            Log.d(TAG, "导入完成，共 ${entities.size} 条")
        }
    }

    suspend fun getAnimalById(id: String): Animal? =
        animalDao.getAnimalById(id)?.toAnimal()

    suspend fun searchByName(name: String): Animal? =
        animalDao.getAnimalByName(name)?.toAnimal()

    // 原有方法保留，兼容旧调用
    suspend fun searchAnimals(keyword: String): List<Animal> =
    animalDao.searchAnimals(keyword).map { it.toAnimal() }

    // 新增：带搜索模式的搜索
    suspend fun searchAnimals(keyword: String, mode: SearchMode): List<Animal> {
        val entities = when (mode) {
            SearchMode.CN_NAME -> animalDao.searchByCnName(keyword)
            SearchMode.LATIN_NAME -> animalDao.searchByLatinName(keyword)
            SearchMode.CLASSIFICATION -> animalDao.searchByClassification(keyword)
            SearchMode.ALL -> animalDao.searchAnimals(keyword)
        }
        return entities.map { it.toAnimal() }
    }

    suspend fun getAllAnimals(): List<Animal> =
    animalDao.getAllAnimals().map { it.toAnimal() }

    fun getImageResIds(latinName: String, maxImages: Int = 5): List<Int> {
        val baseName = latinNameToImageName(latinName)
        val resIds = mutableListOf<Int>()

        for (i in 1..maxImages) {
            val imageName = "${baseName}_$i"
            val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
            if (resId != 0) {
                resIds.add(resId)
            } else {
                if (i > 1) break
            }
        }

        return resIds
    }

    fun getImageResId(latinName: String, index: Int = 1): Int {
        val baseName = latinNameToImageName(latinName)
        val imageName = "${baseName}_$index"
        return context.resources.getIdentifier(imageName, "drawable", context.packageName)
    }

    private fun latinNameToImageName(latinName: String): String {
        return "img_" + latinName
            .trim()
            .lowercase()                    // 全小写
            .split(Regex("[\\s_-]+"))       // 按空格/下划线/连字符分割
            .filter { it.isNotBlank() }     // 去掉空片段
            .joinToString("_")              // ← 改成用下划线连接
    }

    private fun JsonAnimal.toEntity(): AnimalEntity {
        return AnimalEntity(
            id = id,
            cnname = gson.toJson(cnname),
            latinName = latinName,
            kingdom = classification.kingdom,
            phylum = classification.phylum,
            className = classification.className,
            order = classification.order,
            family = classification.family,
            genus = classification.genus,
            species = classification.species,
            appearance = appearance,
            habits = habits,
            habitat = habitat,
            diet = diet
        )
    }

    private fun AnimalEntity.toAnimal(): Animal {
        return Animal(
            id = id,
            cnname = parseJsonList(cnname),
            latinName = latinName,
            classification = Classification(
                kingdom = kingdom,
                phylum = phylum,
                className = className,
                order = order,
                family = family,
                genus = genus,
                species = species
            ),
            appearance = appearance,
            habits = habits,
            habitat = habitat,
            diet = diet
        )
    }

    private fun parseJsonList(json: String): List<String> {
        return try {
            gson.fromJson(json, Array<String>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun HistoryEntity.toHistory(): History {
        return History(
            id = id,
            animalId = animalId,
            name = name,
            category = category,
            viewTime = viewTime
        )
    }

    private fun History.toEntity(): HistoryEntity {
        return HistoryEntity(
            id = id,
            animalId = animalId,
            name = name,
            category = category,
            viewTime = viewTime
        )
    }

    suspend fun insertHistory(history: History) {
        val count = historyDao.getHistoryCount()
        if (count >= 100) {
            historyDao.getOldestHistory()?.let { historyDao.deleteHistory(it) }
        }
        historyDao.insertHistory(history.toEntity())
    }

    fun getAllHistory(): Flow<List<History>> =
    historyDao.getAllHistory().map { list -> list.map { it.toHistory() } }

    suspend fun deleteHistory(history: History) =
        historyDao.deleteHistory(history.toEntity())

    suspend fun clearAllHistory() =
        historyDao.clearAllHistory()

    suspend fun clearAllHistoryCompletely() {
        HistoryDatabase.getDatabase(context).close()
        context.deleteDatabase("history_database")
        HistoryDatabase.resetInstance()
    }

    private fun FavoriteEntity.toFavorite(): Favorite {
        return Favorite(
            id = id,
            animalId = animalId,
            name = name,
            category = category,
            favoriteTime = favoriteTime
        )
    }

    private fun Favorite.toEntity(): FavoriteEntity {
        return FavoriteEntity(
            id = id,
            animalId = animalId,
            name = name,
            category = category,
            favoriteTime = favoriteTime
        )
    }

    suspend fun insertFavorite(favorite: Favorite) {
        favoriteDao.insertFavorite(favorite.toEntity())
    }

    fun getAllFavorites(): Flow<List<Favorite>> =
        favoriteDao.getAllFavorites().map { list -> list.map { it.toFavorite() } }

    suspend fun deleteFavorite(favorite: Favorite) =
        favoriteDao.deleteFavorite(favorite.toEntity())

    suspend fun clearAllFavorites() =
        favoriteDao.clearAllFavorites()

    suspend fun getFavoriteByAnimalId(animalId: String): Favorite? =
        favoriteDao.getFavoriteByAnimalId(animalId)?.toFavorite()

    suspend fun toggleFavorite(animal: Animal): Boolean {
        val existingFavorite = getFavoriteByAnimalId(animal.id)
        return if (existingFavorite != null) {
            deleteFavorite(existingFavorite)
            false
        } else {
            val favorite = Favorite(
                animalId = animal.id,
                name = animal.cnname.firstOrNull() ?: "未知动物",
                category = "${animal.classification.className} ${animal.classification.order}"
            )
            insertFavorite(favorite)
            true
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AnimalRepository? = null

        fun getInstance(context: Context): AnimalRepository {
            return INSTANCE ?: synchronized(this) {
                val animalDb = AnimalDatabase.getDatabase(context.applicationContext)
                val historyDb = HistoryDatabase.getDatabase(context.applicationContext)
                val favoriteDb = FavoriteDatabase.getDatabase(context.applicationContext)
                val instance = AnimalRepository(
                    context.applicationContext,
                    animalDb.animalDao(),
                    historyDb.historyDao(),
                    favoriteDb.favoriteDao()
                )
                INSTANCE = instance
                instance
            }
        }
    }
}