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
import com.example.animalwiki.ui.screens.CameraScreen
import com.example.animalwiki.ui.screens.FavoriteScreen
import com.example.animalwiki.ui.screens.HistoryScreen
import com.example.animalwiki.ui.screens.HomeScreen
import com.example.animalwiki.ui.theme.AnimalWikiTheme
import com.example.animalwiki.ui.viewmodel.AnimalViewModel

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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    val navController = rememberNavController()
                    val viewModel: AnimalViewModel = viewModel()

                    val bottomNavScreens = listOf(
                        Screen.Home,
                        Screen.Favorites,
                        Screen.History
                    )

                    // 监听当前路由，判断是否需要隐藏底部栏
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    val hideBottomBarRoutes = setOf(
                        "camera",
                        Screen.Detail.route,
                        "list/{categoryId}/{categoryName}"
                    )
                    val showBottomBar = currentRoute !in hideBottomBarRoutes

                    Scaffold(
                        bottomBar = {
                            if (showBottomBar) {
                                NavigationBar(
                                    containerColor = Color.White
                                ) {
                                    val currentDestination = navBackStackEntry?.destination

                                    bottomNavScreens.forEach { screen ->
                                        NavigationBarItem(
                                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                                            label = { Text(screen.title) },
                                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                            onClick = {
                                                if (screen.route == Screen.Home.route) {
                                                    navController.navigate(Screen.Home.route) {
                                                        popUpTo(navController.graph.id) {
                                                            inclusive = false
                                                        }
                                                        launchSingleTop = true
                                                    }
                                                } else {
                                                    navController.navigate(screen.route) {
                                                        popUpTo(Screen.Home.route) {
                                                            saveState = true
                                                        }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            }
                                        )
                                    }
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
                            composable(Screen.Home.route) {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onAnimalClick = { animalId ->
                                        navController.navigate("detail/$animalId")
                                    },
                                    onCategoryClick = { categoryId, categoryName ->
                                        navController.navigate("list/$categoryId/$categoryName")
                                    },
                                    onCameraClick = {
                                        navController.navigate("camera")
                                    }
                                )
                            }

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

                            composable("camera") {
                                CameraScreen(navController = navController)
                            }

                            composable(Screen.Favorites.route) {
                                FavoriteScreen(
                                    viewModel = viewModel,
                                    onFavoriteItemClick = { animalId ->
                                        navController.navigate("detail/$animalId")
                                    }
                                )
                            }

                            composable(Screen.History.route) {
                                HistoryScreen(
                                    viewModel = viewModel,
                                    onHistoryItemClick = { animalId ->
                                        navController.navigate("detail/$animalId")
                                    }
                                )
                            }

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