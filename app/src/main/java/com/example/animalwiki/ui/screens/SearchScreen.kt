package com.example.animalwiki.ui.screens
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.animalwiki.data.model.Animal
import com.example.animalwiki.data.model.ClassificationLevel
import com.example.animalwiki.data.model.Folder
import com.example.animalwiki.data.model.SearchFilter
import com.example.animalwiki.data.model.SearchMode
import com.example.animalwiki.ui.viewmodel.AnimalViewModel
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: AnimalViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val searchMode by viewModel.searchMode.collectAsState()
    val searchFilter by viewModel.searchFilter.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val folderList by viewModel.folderList.collectAsState()
    val kingdomOptions by viewModel.kingdomOptions.collectAsState()
    val phylumOptions by viewModel.phylumOptions.collectAsState()
    val classOptions by viewModel.classOptions.collectAsState()
    val orderOptions by viewModel.orderOptions.collectAsState()
    val familyOptions by viewModel.familyOptions.collectAsState()
    val genusOptions by viewModel.genusOptions.collectAsState()
    val speciesOptions by viewModel.speciesOptions.collectAsState()
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    var showFilterPanel by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .height(56.dp),
                placeholder = { Text("搜索动物名称...", fontSize = 15.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(
                            onClick = { viewModel.onSearchQueryChange("") },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "清空",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            IconButton(
                onClick = { navController.navigate("camera") },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Camera,
                    contentDescription = "拍照识别",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchMode.entries.forEach { mode ->
                FilterChip(
                    selected = searchMode == mode,
                    onClick = { viewModel.onSearchModeChange(mode) },
                    label = { Text(mode.label, fontSize = 13.sp) },
                    modifier = Modifier.height(32.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = { showFilterPanel = !showFilterPanel },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterAlt,
                    contentDescription = "筛选",
                    tint = if (searchFilter.hasAnyFilter()) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        if (showFilterPanel) {
            FilterPanel(
                searchFilter = searchFilter,
                folderList = folderList,
                kingdomOptions = kingdomOptions,
                phylumOptions = phylumOptions,
                classOptions = classOptions,
                orderOptions = orderOptions,
                familyOptions = familyOptions,
                genusOptions = genusOptions,
                speciesOptions = speciesOptions,
                viewModel = viewModel,
                onClose = { showFilterPanel = false }
            )
        }
        if (searchFilter.hasAnyFilter()) {
            ActiveFilterChips(
                searchFilter = searchFilter,
                folderList = folderList,
                viewModel = viewModel
            )
        }
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            searchQuery.isBlank() && !searchFilter.hasAnyFilter() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        Text(
                            text = "输入关键词搜索动物",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 16.dp),
                            fontSize = 15.sp
                        )
                        Text(
                            text = "支持中文名、拉丁名、分类搜索",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            searchResults.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "未找到相关动物",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontSize = 15.sp
                    )
                }
            }
            else -> {
                Text(
                    text = "共 ${searchResults.size} 个结果",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp, top = 4.dp),
                    fontSize = 12.sp
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(searchResults, key = { it.id }) { animal ->
                        AnimalSearchItem(
                            animal = animal,
                            viewModel = viewModel,
                            onClick = { navController.navigate("detail/${animal.id}") }
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun FilterPanel(
    searchFilter: SearchFilter,
    folderList: List<Folder>,
    kingdomOptions: List<String>,
    phylumOptions: List<String>,
    classOptions: List<String>,
    orderOptions: List<String>,
    familyOptions: List<String>,
    genusOptions: List<String>,
    speciesOptions: List<String>,
    viewModel: AnimalViewModel,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "筛选条件",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = { viewModel.clearAllFilters() }) {
                    Text("清除全部", fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "收藏夹筛选",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            DropdownSelector(
                label = "收藏夹",
                selectedValue = searchFilter.favoriteFolderId?.let { id ->
                    folderList.find { it.id == id }?.name
                },
                options = listOf("全部") + folderList.map { it.name },
                onOptionSelected = { option ->
                    if (option == "全部") {
                        viewModel.onFavoriteFolderSelected(null)
                    } else {
                        folderList.find { it.name == option }?.let { folder ->
                            viewModel.onFavoriteFolderSelected(folder.id)
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "分类筛选",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            DropdownSelector(
                label = ClassificationLevel.KINGDOM.label,
                selectedValue = searchFilter.kingdom,
                options = kingdomOptions,
                onOptionSelected = { viewModel.onKingdomSelected(it) }
            )
            if (phylumOptions.isNotEmpty()) {
                DropdownSelector(
                    label = ClassificationLevel.PHYLUM.label,
                    selectedValue = searchFilter.phylum,
                    options = phylumOptions,
                    onOptionSelected = { viewModel.onPhylumSelected(it) }
                )
            }
            if (classOptions.isNotEmpty()) {
                DropdownSelector(
                    label = ClassificationLevel.CLASS.label,
                    selectedValue = searchFilter.className,
                    options = classOptions,
                    onOptionSelected = { viewModel.onClassSelected(it) }
                )
            }
            if (orderOptions.isNotEmpty()) {
                DropdownSelector(
                    label = ClassificationLevel.ORDER.label,
                    selectedValue = searchFilter.order,
                    options = orderOptions,
                    onOptionSelected = { viewModel.onOrderSelected(it) }
                )
            }
            if (familyOptions.isNotEmpty()) {
                DropdownSelector(
                    label = ClassificationLevel.FAMILY.label,
                    selectedValue = searchFilter.family,
                    options = familyOptions,
                    onOptionSelected = { viewModel.onFamilySelected(it) }
                )
            }
            if (genusOptions.isNotEmpty()) {
                DropdownSelector(
                    label = ClassificationLevel.GENUS.label,
                    selectedValue = searchFilter.genus,
                    options = genusOptions,
                    onOptionSelected = { viewModel.onGenusSelected(it) }
                )
            }
            if (speciesOptions.isNotEmpty()) {
                DropdownSelector(
                    label = ClassificationLevel.SPECIES.label,
                    selectedValue = searchFilter.species,
                    options = speciesOptions,
                    onOptionSelected = { viewModel.onSpeciesSelected(it) }
                )
            }
        }
    }
}
@Composable
private fun DropdownSelector(
    label: String,
    selectedValue: String?,
    options: List<String>,
    onOptionSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.padding(bottom = 8.dp)) {
        AssistChip(
            onClick = { expanded = true },
            label = {
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$label: ${selectedValue ?: "全部"}",
                        fontSize = 13.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            DropdownMenuItem(
                text = { Text("全部", fontSize = 14.sp) },
                onClick = {
                    onOptionSelected(null)
                    expanded = false
                }
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontSize = 14.sp) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
@Composable
private fun ActiveFilterChips(
    searchFilter: SearchFilter,
    folderList: List<Folder>,
    viewModel: AnimalViewModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        searchFilter.favoriteFolderId?.let { folderId ->
            val folderName = folderList.find { it.id == folderId }?.name ?: "收藏夹"
            FilterChip(
                selected = true,
                onClick = { viewModel.onFavoriteFolderSelected(null) },
                label = { Text("收藏夹: $folderName", fontSize = 12.sp) },
                trailingIcon = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "移除",
                        modifier = Modifier.size(14.dp)
                    )
                },
                modifier = Modifier.height(28.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
        searchFilter.kingdom?.let {
            FilterChip(
                selected = true,
                onClick = { viewModel.onKingdomSelected(null) },
                label = { Text("界: $it", fontSize = 12.sp) },
                trailingIcon = {
                    Icon(Icons.Default.Close, contentDescription = "移除", modifier = Modifier.size(14.dp))
                },
                modifier = Modifier.height(28.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
        searchFilter.phylum?.let {
            FilterChip(
                selected = true,
                onClick = { viewModel.onPhylumSelected(null) },
                label = { Text("门: $it", fontSize = 12.sp) },
                trailingIcon = {
                    Icon(Icons.Default.Close, contentDescription = "移除", modifier = Modifier.size(14.dp))
                },
                modifier = Modifier.height(28.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
        searchFilter.className?.let {
            FilterChip(
                selected = true,
                onClick = { viewModel.onClassSelected(null) },
                label = { Text("纲: $it", fontSize = 12.sp) },
                trailingIcon = {
                    Icon(Icons.Default.Close, contentDescription = "移除", modifier = Modifier.size(14.dp))
                },
                modifier = Modifier.height(28.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
        searchFilter.order?.let {
            FilterChip(
                selected = true,
                onClick = { viewModel.onOrderSelected(null) },
                label = { Text("目: $it", fontSize = 12.sp) },
                trailingIcon = {
                    Icon(Icons.Default.Close, contentDescription = "移除", modifier = Modifier.size(14.dp))
                },
                modifier = Modifier.height(28.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
        searchFilter.family?.let {
            FilterChip(
                selected = true,
                onClick = { viewModel.onFamilySelected(null) },
                label = { Text("科: $it", fontSize = 12.sp) },
                trailingIcon = {
                    Icon(Icons.Default.Close, contentDescription = "移除", modifier = Modifier.size(14.dp))
                },
                modifier = Modifier.height(28.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
    }
}
@Composable
private fun AnimalSearchItem(
    animal: Animal,
    viewModel: AnimalViewModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                val imageRes = viewModel.getAnimalImage(animal)
                if (imageRes != 0) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = animal.cnname.firstOrNull(),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = animal.cnname.firstOrNull()?.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = animal.cnname.firstOrNull() ?: "未知",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = animal.latinName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Text(
                    text = "${animal.classification.className} · ${animal.classification.order}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "查看详情",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
