package com.example.animalwiki

import android.app.Application
import com.example.animalwiki.data.database.AnimalDatabase
import com.example.animalwiki.data.repository.AnimalRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {
    lateinit var animalDb: AnimalDatabase
        private set

    lateinit var animalRepository: AnimalRepository
        private set

    override fun onCreate() {
        super.onCreate()

        // 首次启动：从 assets JSON 导入数据到 Room
        CoroutineScope(Dispatchers.IO).launch {
            AnimalRepository.getInstance(this@App).initializeDatabase()
        }
    }
}