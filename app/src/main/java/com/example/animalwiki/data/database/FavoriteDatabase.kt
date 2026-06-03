package com.example.animalwiki.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.animalwiki.data.database.entity.FavoriteEntity
import com.example.animalwiki.data.database.entity.FolderEntity


@Database(
    entities = [FavoriteEntity::class,FolderEntity::class],
    version = 2,
    exportSchema = false
)
abstract class FavoriteDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun folderDao(): FolderDao

    companion object {
        @Volatile
        private var INSTANCE: FavoriteDatabase? = null

        fun getDatabase(context: Context): FavoriteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FavoriteDatabase::class.java,
                    "favorite_database"
                )
                    .fallbackToDestructiveMigration() // 开发阶段直接重建
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun resetInstance() {
            INSTANCE = null
        }
    }
}