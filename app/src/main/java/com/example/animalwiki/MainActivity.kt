package com.example.animalwiki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.animalwiki.ui.screens.AnimalDetailScreen
import com.example.animalwiki.ui.screens.AnimalListScreen
import com.example.animalwiki.ui.screens.CameraScreen // ✅ 添加相机页面导入
import com.example.animalwiki.ui.screens.FavoriteScreen
import com.example.animalwiki.ui.screens.HistoryScreen
import com.example.animalwiki.ui.screens.HomeScreen
import com.example.animalwiki.ui.theme.AnimalWikiTheme
import com.example.animalwiki.ui.viewmodel.AnimalViewModel

// 只保留三个核心页面
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "首页", Icons.Default.Home)
    object Favorites : Screen("favorites", "收藏", Icons.Default.Favorite)
    object History : Screen("history", "历史", Icons.Default.History)
    object Detail : Screen("detail/{animalId}", "详情", Icons.Default.Home)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnimalWikiTheme {
                // 强制最外层背景为纯白色，彻底解决黑屏
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    val navController = rememberNavController()
                    val viewModel: AnimalViewModel = viewModel()

                    // 底部导航栏只显示三个页面
                    val bottomNavScreens = listOf(
                        Screen.Home,
                        Screen.Favorites,
                        Screen.History
                    )

                    Scaffold(
                        bottomBar = {
                            NavigationBar(
                                containerColor = Color.White // 底部导航栏也设为白色
                            ) {
                                val navBackStackEntry by navController.currentBackStackEntryAsState()
                                val currentDestination = navBackStackEntry?.destination

                                bottomNavScreens.forEach { screen ->
                                    NavigationBarItem(
                                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                                        label = { Text(screen.title) },
                                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Home.route,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            // 首页 ✅ 已添加相机按钮回调
                            composable(Screen.Home.route) {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onAnimalClick = { animalId ->
                                        navController.navigate("detail/$animalId")
                                    },
                                    onCategoryClick = { categoryId, categoryName ->
                                        // 传递分类ID和名称到列表页
                                        navController.navigate("list/$categoryId/$categoryName")
                                    },
                                    onCameraClick = {
                                        // ✅ 点击相机按钮跳转到相机页面
                                        navController.navigate("camera")
                                    }
                                )
                            }

                            // 分类浏览页面（带参数路由）
                            composable("list/{categoryId}/{categoryName}") { backStackEntry ->
                                val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
                                val categoryName = backStackEntry.arguments?.getString("categoryName") ?: "动物列表"

                                AnimalListScreen(
                                    viewModel = viewModel,
                                    categoryId = categoryId,
                                    categoryName = categoryName,
                                    onAnimalClick = { animalId ->
                                        navController.navigate("detail/$animalId")
                                    },
                                    onBackClick = { navController.popBackStack() }
                                )
                            }

                            // ✅ 相机页面路由
                            composable("camera") {
                                CameraScreen(navController = navController)
                            }

                            // 收藏页
                            composable(Screen.Favorites.route) {
                                FavoriteScreen(
                                    viewModel = viewModel,
                                    onFavoriteItemClick = { animalId ->
                                        navController.navigate("detail/$animalId")
                                    }
                                )
                            }

                            // 历史页
                            composable(Screen.History.route) {
                                HistoryScreen(
                                    viewModel = viewModel,
                                    onHistoryItemClick = { animalId ->
                                        // 点击历史记录跳转到详情页
                                        navController.navigate("detail/$animalId")
                                    }
                                )
                            }

                            // 详情页
                            composable(Screen.Detail.route) { backStackEntry ->
                                val animalId = backStackEntry.arguments?.getString("animalId") ?: ""
                                AnimalDetailScreen(
                                    viewModel = viewModel,
                                    animalId = animalId,
                                    onBackClick = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}