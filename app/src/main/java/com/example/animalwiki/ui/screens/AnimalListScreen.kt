package com.example.animalwiki.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.animalwiki.data.model.Animal
import com.example.animalwiki.ui.viewmodel.AnimalViewModel

// 分类ID到生物分类名称的映射（保持不变）
private val categoryMap = mapOf(
    "mammals" to "哺乳纲",
    "birds" to "鸟纲",
    "reptiles" to "爬行纲",
    "amphibians" to "两栖纲",
    "insects" to "昆虫纲",
    "marine" to "海洋生物",
    "others" to "其他生物"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun AnimalListScreen(
    viewModel: AnimalViewModel,
    categoryId: String, // 从首页传过来的分类ID
    categoryName: String, // 从首页传过来的分类显示名称
    onAnimalClick: (String) -> Unit,
    onBackClick: () -> Unit // 返回首页的回调
) {
    val animals by viewModel.animals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val filteredAnimals = remember(animals, categoryId) {
        when (categoryId) {
            // 鱼类：同时包含软骨鱼纲和辐鳍鱼纲
            "fish" -> animals.filter { it.classification.className.endsWith("鱼纲") }

            // ✅ 海洋生物：包含所有海洋生物的生物纲
            "marine" -> animals.filter { animal ->
                animal.classification.className in listOf(
                    "头足纲", "钵水母纲", "立方水母纲", "珊瑚虫纲",
                    "海星纲", "海参纲", "海胆纲", "海百合纲", "甲壳纲"
                )
            }

            "others" -> animals.filter {
                it.classification.className in listOf(
                    "寡毛纲", "蛭纲", "腹足纲", "双壳纲"
                )
            }

            // 其他分类保持原有精确匹配
            else -> {
                val targetClassName = categoryMap[categoryId] ?: ""
                animals.filter { it.classification.className == targetClassName }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName) }, // ✅ 左上角显示分类名称
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (filteredAnimals.isEmpty()) {
                // 分类下暂无动物的空状态
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "该分类下暂无动物",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredAnimals) { animal ->
                        AnimalListItem(
                            animal = animal,
                            viewModel = viewModel,
                            onClick = { onAnimalClick(animal.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AnimalListItem(
    animal: Animal,
    viewModel: AnimalViewModel,
    onClick: () -> Unit
) {
    val imageResId = remember(animal.latinName) {
        viewModel.getAnimalImage(animal, 1)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图片
            if (imageResId != 0) {
                GlideImage(
                    model = imageResId,
                    contentDescription = animal.cnname.firstOrNull(),
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                // 占位图
                Surface(
                    modifier = Modifier.size(80.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = animal.cnname.firstOrNull()?.take(1) ?: "?",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 文字信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = animal.cnname.firstOrNull() ?: animal.latinName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = animal.latinName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = animal.classification.className,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}