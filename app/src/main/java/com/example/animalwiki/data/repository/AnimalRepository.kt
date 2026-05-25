package com.example.animalwiki.data.repository

import android.content.Context
import android.util.Log
import com.example.animalwiki.data.database.AnimalDao
import com.example.animalwiki.data.database.entity.AnimalEntity
import com.example.animalwiki.data.local.AnimalSource
import com.example.animalwiki.data.model.Animal
import com.example.animalwiki.data.model.Classification
import com.google.gson.Gson

class AnimalRepository(
    context: Context,
    private val animalDao: AnimalDao
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

    suspend fun searchAnimals(keyword: String): List<Animal> =
    animalDao.searchAnimals(keyword).map { it.toAnimal() }

    suspend fun getAllAnimals(): List<Animal> =
    animalDao.getAllAnimals().map { it.toAnimal() }

    // ==================== 图片定位 ====================

    /**
     * 根据拉丁学名获取图片资源 ID 列表
     * @param latinName 拉丁学名，如 "Ailuropoda melanoleuca"
     * @param maxImages 最多查找几张（默认 5 张）
     * @return 有效的 R.drawable.xxx 资源 ID 列表
     */
    fun getImageResIds(context: Context, latinName: String, maxImages: Int = 5): List<Int> {
        val baseName = latinNameToImageName(latinName)
        val resIds = mutableListOf<Int>()

        for (i in 1..maxImages) {
            val imageName = "${baseName}_$i"
            val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
            if (resId != 0) {
                resIds.add(resId)
            } else {
                // 连续找不到就停止（假设编号连续）
                if (i > 1) break
            }
        }

        return resIds
    }

    /**
     * 获取单张图片的资源 ID（默认第一张）
     */
    fun getImageResId(context: Context, latinName: String, index: Int = 1): Int {
        val baseName = latinNameToImageName(latinName)
        val imageName = "${baseName}_$index"
        return context.resources.getIdentifier(imageName, "drawable", context.packageName)
    }

    /**
     * 拉丁学名转驼峰命名图片前缀
     */
    private fun latinNameToImageName(latinName: String): String {
        return "img_" + latinName
            .trim()
            .lowercase()
            .split(Regex("[\\s_-]+"))  // 按空格、下划线、连字符分割
            .filter { it.isNotBlank() }
            .mapIndexed { index, word ->
                if (index == 0) word else word.replaceFirstChar { it.uppercase() }
            }
            .joinToString("")
    }

    // ==================== 数据转换 ====================

    private fun com.example.animalwiki.data.model.JsonAnimal.toEntity(): AnimalEntity {
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
}