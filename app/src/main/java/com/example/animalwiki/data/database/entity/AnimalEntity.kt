package com.example.animalwiki.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "animals")
data class AnimalEntity(
    @PrimaryKey
    val id: String,                     // MD5 哈希值

    val cnname: String,                 // JSON 数组字符串（中文名）
    val latinName: String,              // 拉丁学名

    // 分类信息
    val kingdom: String,
    val phylum: String,
    val className: String,
    val order: String,
    val family: String,
    val genus: String,
    val species: String,

    // 详细信息
    val appearance: String,             // 外形
    val habits: String,                 // 习性
    val habitat: String,                // 栖息地
    val diet: String,                   // 食性
)