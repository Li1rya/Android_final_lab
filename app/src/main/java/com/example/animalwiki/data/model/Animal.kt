package com.example.animalwiki.data.model

/**
 * 应用层使用的动物领域模型（对应本地 JSON 结构）
 */
data class Animal(
    val id: String,                     // MD5 哈希值
    val cnname: List<String>,             // 中文名
    val latinName: String,              // 拉丁学名
    val classification: Classification, // 界门纲目科属种
    val appearance: String,            // 外形
    val habits: String,                  // 习性
    val habitat: String,                // 栖息地
    val diet: String,                   // 食性
)

data class Classification(
    val kingdom: String,
    val phylum: String,
    val className: String,  // 注意：class 是 Kotlin 关键字，用 className
    val order: String,
    val family: String,
    val genus: String,
    val species: String
)