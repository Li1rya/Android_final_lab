package com.example.animalwiki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.animalwiki.ui.screens.AnimalDetailScreen
import com.example.animalwiki.ui.screens.AnimalListScreen

import com.example.animalwiki.ui.screens.HomeScreen
import com.example.animalwiki.ui.theme.AnimalWikiTheme
import com.example.animalwiki.ui.viewmodel.AnimalViewModel

// ✅ 修复：图标类型改为 ImageVector
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "首页", Icons.Default.Home)
    object Category : Screen("category", "分类", Icons.AutoMirrored.Filled.List) // ✅ 修复：使用未弃用的AutoMirrored版本
    object Favorites : Screen("favorites", "收藏", Icons.Default.Favorite)
    object History : Screen("history", "历史", Icons.Default.History)
    object Detail : Screen("detail/{animalId}", "详情", Icons.Default.Home)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnimalWikiTheme {
                val navController = rememberNavController()
                val viewModel: AnimalViewModel = viewModel()

                // 底部导航栏页面列表
                val bottomNavScreens = listOf(
                    Screen.Home,
                    Screen.Category,
                    Screen.Favorites,
                    Screen.History
                )

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination

                            bottomNavScreens.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = screen.title) },
                                    label = { Text(screen.title) },
                                    // ✅ 修复：正确的选中状态判断
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            // 避免创建多个实例
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
                        // 首页
                        composable(Screen.Home.route) {
                            HomeScreen(
                                viewModel = viewModel,
                                onAnimalClick = { animalId ->
                                    navController.navigate("detail/$animalId")
                                },
                                onCategoryClick = { categoryId ->
                                    // 点击分类跳转到分类页
                                    navController.navigate(Screen.Category.route)
                                }
                            )
                        }

                        // 分类页（复用你现有的AnimalListScreen）
                        composable(Screen.Category.route) {
                            AnimalListScreen(
                                viewModel = viewModel,
                                onAnimalClick = { animalId ->
                                    navController.navigate("detail/$animalId")
                                }
                            )
                        }

                        // 收藏页
                        composable(Screen.Favorites.route) {

                        }

                        // 历史页
                        composable(Screen.History.route) {

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