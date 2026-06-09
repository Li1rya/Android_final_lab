package com.example.animalwiki.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.example.animalwiki.data.model.Animal
import com.example.animalwiki.data.model.History
import com.example.animalwiki.ui.viewmodel.AnimalViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalDetailScreen(
    viewModel: AnimalViewModel,
    animalId: String,
    onBackClick: () -> Unit
) {
    val currentAnimal by viewModel.currentAnimal.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val folderList by viewModel.folderList.collectAsState()

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showSelectFolderSheet by remember { mutableStateOf(false) }

    LaunchedEffect(animalId) {
        viewModel.getAnimalById(animalId)
    }

    LaunchedEffect(currentAnimal) {
        currentAnimal?.let { animal ->
            viewModel.checkIsFavorite(animal.id)
            val history = History(
                animalId = animal.id,
                name = animal.cnname.firstOrNull() ?: "未知动物",
                category = "${animal.classification.className} ${animal.classification.order}",
                viewTime = System.currentTimeMillis()
            )
            viewModel.insertHistory(history)
        }
    }

    currentAnimal?.let { animal ->
        val imageResIds = remember(animal.id) {
            viewModel.getAnimalImages(animal)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(animal.cnname.firstOrNull() ?: "动物详情") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回"
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                viewModel.viewModelScope.launch {
                                    if (isFavorite) {
                                        viewModel.toggleFavorite(animal)
                                    } else {
                                        showSelectFolderSheet = true
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isFavorite) "取消收藏" else "收藏",
                                tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
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
                    .verticalScroll(rememberScrollState())
            ) {
                if (imageResIds.isNotEmpty()) {
                    HorizontalImagePager(imageResIds = imageResIds)
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = animal.cnname.firstOrNull()?.take(1) ?: "?",
                                style = MaterialTheme.typography.displayLarge
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = animal.cnname.firstOrNull() ?: "未知",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (animal.cnname.size > 1) {
                        Text(
                            text = "别名：${animal.cnname.drop(1).joinToString("、")}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Text(
                        text = "拉丁学名：${animal.latinName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Divider(modifier = Modifier.padding(vertical = 16.dp))

                    ClassificationCard(classification = animal.classification)

                    Divider(modifier = Modifier.padding(vertical = 16.dp))

                    InfoSection(title = "外形", content = animal.appearance)
                    InfoSection(title = "习性", content = animal.habits)
                    InfoSection(title = "栖息地", content = animal.habitat)
                    InfoSection(title = "食性", content = animal.diet)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onBackClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("返回列表")
                    }
                }
            }
        }

        if (showSelectFolderSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSelectFolderSheet = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "选择收藏夹",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    folderList.forEach { folder ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.viewModelScope.launch {
                                        viewModel.addToFavorite(animal, folder.id)
                                    }
                                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                                        showSelectFolderSheet = false
                                    }
                                }
                        ) {
                            Text(text = folder.name, modifier = Modifier.padding(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    } ?: run {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun HorizontalImagePager(imageResIds: List<Int>) {
    val pagerState = remember { androidx.compose.foundation.pager.PagerState { imageResIds.size } }

    Column {
        androidx.compose.foundation.pager.HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) { page ->
            Image(
                painter = painterResource(id = imageResIds[page]),
                contentDescription = "动物图片 ${page + 1}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        if (imageResIds.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                imageResIds.forEachIndexed { index, _ ->
                    val color = if (index == pagerState.currentPage) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    }
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(8.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = color,
                            modifier = Modifier.fillMaxSize()
                        ) {}
                    }
                }
            }
        }
    }
}

@Composable
fun ClassificationCard(classification: com.example.animalwiki.data.model.Classification) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "分类信息",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            ClassificationRow(label = "界", value = classification.kingdom)
            ClassificationRow(label = "门", value = classification.phylum)
            ClassificationRow(label = "纲", value = classification.className)
            ClassificationRow(label = "目", value = classification.order)
            ClassificationRow(label = "科", value = classification.family)
            ClassificationRow(label = "属", value = classification.genus)
            ClassificationRow(label = "种", value = classification.species)
        }
    }
}

@Composable
fun ClassificationRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = "$label：",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(40.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun InfoSection(title: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "【$title】",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3f,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}