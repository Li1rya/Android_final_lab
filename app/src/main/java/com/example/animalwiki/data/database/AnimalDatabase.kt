package com.example.animalwiki.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.animalwiki.data.database.entity.AnimalEntity

// Room数据库（@Database）
@Database(entities = [AnimalEntity::class], version = 1, exportSchema = false)
abstract class AnimalDatabase : RoomDatabase() {
    abstract fun animalDao(): AnimalDao

    // 单例模式
    companion object {
        @Volatile
        private var INSTANCE: AnimalDatabase? = null

        fun getInstance(context: Context): AnimalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.inMemoryDatabaseBuilder( // 内存数据库（测试友好）
                    context.applicationContext,
                    AnimalDatabase::class.java,
                ).allowMainThreadQueries() // 测试用（实际需移除，用协程）
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}