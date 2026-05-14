package com.example.animalwiki

import android.app.Application
import com.example.animalwiki.data.database.AnimalDatabase

class App : Application() {
    lateinit var animalDb: AnimalDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        animalDb = AnimalDatabase.getInstance(this)
    }
}