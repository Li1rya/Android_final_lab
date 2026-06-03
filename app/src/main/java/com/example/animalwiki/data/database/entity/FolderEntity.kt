package com.example.animalwiki.data.database.entity

// com.example.animalwiki.data.database.entity.FolderEntity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // ✅ 保留自动生成ID
    val name: String,
    val createTime: Long = System.currentTimeMillis(),
    val isDefault: Boolean = false // ✅ 新增：标记是否为默认收藏夹
)