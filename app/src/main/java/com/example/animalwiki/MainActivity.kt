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

// 只保留三个核心页面
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "首页", Icons.Default.Home)
    object Favorites : Screen("favorites", "收藏", Icons.Default.Favorite)
    object History : Screen("history", "历史", Icons.Default.History)
    object Detail : Screen("detail/{animalId}", "详情", Icons.Default.Home)
    object List : Screen("list", "动物列表", Icons.Default.Home)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnimalWikiTheme {
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
                        NavigationBar {
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
                        // 首页 ✅ 已注释拍照功能参数
                        composable(Screen.Home.route) {
                            HomeScreen(
                                viewModel = viewModel,
                                onAnimalClick = { animalId ->
                                    navController.navigate("detail/$animalId")
                                },
                                // onCameraClick = { /* 后续添加拍照功能 */ },
                                onCategoryClick = { categoryId ->
                                    // 点击分类跳转到动物列表页
                                    navController.navigate(Screen.List.route)
                                }
                            )
                        }

                        // 动物列表页
                        composable(Screen.List.route) {
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