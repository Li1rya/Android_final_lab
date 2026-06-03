package com.example.animalwiki.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.compose.material3.ButtonDefaults
import com.example.animalwiki.data.model.Folder
import com.example.animalwiki.ui.viewmodel.AnimalViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    viewModel: AnimalViewModel,
    onFavoriteItemClick: (String) -> Unit
) {
    val folderList by viewModel.folderList.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 新建收藏夹弹窗状态
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    // 更多菜单和确认对话框状态
    var showMenu by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showDeleteFolderDialog by remember { mutableStateOf(false) }
    var folderToDelete by remember { mutableStateOf<Folder?>(null) }

    // 收藏夹展开状态：key=收藏夹id，value=是否展开
    val expandedFolders = remember { mutableStateOf<Map<Long, Boolean>>(emptyMap()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的收藏") },
                actions = {
                    // 新建收藏夹按钮
                    IconButton(onClick = { showNewFolderDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "新建收藏夹"
                        )
                    }

                    // 更多菜单按钮
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "更多选项"
                        )
                    }

                    // 下拉菜单
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("清除所有收藏") },
                            onClick = {
                                showMenu = false
                                showClearDialog = true
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (folderList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无收藏夹",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(folderList) { folder ->
                    // 单个收藏夹卡片
                    FolderItem(
                        folder = folder,
                        isExpanded = expandedFolders.value[folder.id] ?: false,
                        onToggleExpand = {
                            expandedFolders.value = expandedFolders.value.toMutableMap().apply {
                                put(folder.id, !getOrDefault(folder.id, false))
                            }
                        },
                        onDeleteClick = {
                            folderToDelete = folder
                            showDeleteFolderDialog = true
                        },
                        viewModel = viewModel,
                        onFavoriteItemClick = onFavoriteItemClick
                    )
                }
            }
        }
    }

    // 新建收藏夹弹窗
    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = { Text("新建收藏夹") },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("收藏夹名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            viewModel.insertFolder(newFolderName)
                            newFolderName = ""
                            showNewFolderDialog = false
                            Toast.makeText(context, "收藏夹创建成功", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("创建")
                }
            },
            dismissButton = {
                Button(onClick = { showNewFolderDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 清除所有收藏确认对话框
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清除所有收藏") },
            text = { Text("确定要清除所有收藏吗？此操作不可恢复。") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllFavorites()
                        showClearDialog = false
                        Toast.makeText(context, "已清除所有收藏", Toast.LENGTH_SHORT).show()
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                Button(onClick = { showClearDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 删除收藏夹确认对话框
    if (showDeleteFolderDialog && folderToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteFolderDialog = false },
            title = { Text("删除收藏夹") },
            text = {
                if (folderToDelete!!.isDefault) {
                    Text("默认收藏夹无法删除")
                } else {
                    Text("确定要删除收藏夹「${folderToDelete!!.name}」吗？里面的动物会自动移到默认收藏夹。")
                }
            },
            confirmButton = {
                if (!folderToDelete!!.isDefault) {
                    Button(
                        onClick = {
                            viewModel.viewModelScope.launch {
                                val isDeleted = viewModel.deleteFolder(folderToDelete!!)
                                showDeleteFolderDialog = false
                                folderToDelete = null
                                if (isDeleted) {
                                    Toast.makeText(context, "收藏夹已删除", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "默认收藏夹无法删除", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("删除")
                    }
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDeleteFolderDialog = false
                    folderToDelete = null
                }) {
                    Text("取消")
                }
            }
        )
    }
}

// 单个收藏夹组件
// 单个收藏夹组件
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderItem(
    folder: Folder,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onDeleteClick: () -> Unit,
    viewModel: AnimalViewModel,
    onFavoriteItemClick: (String) -> Unit
) {
    // 获取当前收藏夹的收藏列表
    val favorites by viewModel.getFavoritesByFolder(folder.id).collectAsStateWithLifecycle(initialValue = emptyList())

    // 箭头旋转动画
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "arrow rotation"
    )

    // ✅ 外层只有一个Card，包裹整个收藏夹
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ✅ 收藏夹标题行：直接在Column里，没有额外的Card包裹
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 展开箭头
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "收起" else "展开",
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(arrowRotation),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 收藏夹名称和数量
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = folder.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${favorites.size} 个收藏",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 删除按钮：只有非默认收藏夹才显示
                if (!folder.isDefault) {
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除收藏夹",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // ✅ 展开的收藏列表：直接在Column里，没有额外的Card包裹
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (favorites.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "暂无收藏",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        favorites.forEach { favorite ->
                            // ✅ 只有单个收藏项才用Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onFavoriteItemClick(favorite.animalId) },
                                shape = MaterialTheme.shapes.small,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = favorite.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = favorite.category,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}