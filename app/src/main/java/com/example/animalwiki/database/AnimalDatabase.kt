package com.example.animalwiki.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Room注解，kapt会自动生成实现类
@Database(
    entities = [AnimalEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AnimalDatabase : RoomDatabase() {
    // 暴露Dao
    abstract fun animalDao(): AnimalDao

    // 单例模式（全局唯一数据库实例）
    companion object {
        @Volatile
        private var INSTANCE: AnimalDatabase? = null

        // 获取数据库实例
        fun getInstance(context: Context): AnimalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AnimalDatabase::class.java,
                    "animal_database" // 数据库文件名
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}