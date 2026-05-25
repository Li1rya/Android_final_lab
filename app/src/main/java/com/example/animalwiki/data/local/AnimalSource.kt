package com.example.animalwiki.data.local

import android.content.Context
import com.example.animalwiki.data.model.JsonAnimal
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

class AnimalSource(private val context: Context) {

    private val gson = Gson()
    private val jsonFiles = listOf(
        "animals/mammals.json",
        "animals/birds.json",
        "animals/reptiles.json",
        "animals/amphibians.json",
        "animals/fish.json",
        "animals/insects.json",
        "animals/marine_invertebrates.json",
        "animals/others.json"
    )

    /**
     * 从 assets 加载所有动物 JSON 数据
     */
    fun loadAllAnimals(): List<JsonAnimal> {
        val allAnimals = mutableListOf<JsonAnimal>()

        jsonFiles.forEach { filename ->
            try {
                val jsonString = context.assets.open(filename).use { stream ->
                    BufferedReader(InputStreamReader(stream)).use { reader ->
                        reader.readText()
                    }
                }

                val type = object : TypeToken<List<JsonAnimal>>() {}.type
                val animals: List<JsonAnimal> = gson.fromJson(jsonString, type)
                allAnimals.addAll(animals)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return allAnimals
    }

    /**
     * 根据分类加载特定 JSON 文件
     */
    fun loadAnimalsByCategory(category: String): List<JsonAnimal> {
        val filename = "animals/${category}.json"
        return try {
            val jsonString = context.assets.open(filename).use { stream ->
                BufferedReader(InputStreamReader(stream)).use { reader ->
                    reader.readText()
                }
            }
            val type = object : TypeToken<List<JsonAnimal>>() {}.type
            gson.fromJson(jsonString, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}