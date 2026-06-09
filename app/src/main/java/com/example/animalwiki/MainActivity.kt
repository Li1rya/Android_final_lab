package com.example.animalwiki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.animalwiki.ui.screens.ProfileScreen
import com.example.animalwiki.ui.screens.SearchScreen
import com.example.animalwiki.ui.theme.AnimalWikiTheme
import com.example.animalwiki.ui.viewmodel.AnimalViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "首页", Icons.Default.Home)
    object Profile : Screen("profile", "个人中心", Icons.Default.Person)
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

                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    val hideBottomBarRoutes = setOf(
                        "camera",
                        "search",
                        Screen.Detail.route,
                        "list/{categoryId}/{categoryName}",
                        "favorites",
                        "history"
                    )
                    val showBottomBar = currentRoute !in hideBottomBarRoutes

                    Scaffold(
                        bottomBar = {
                            if (showBottomBar) {
                                BottomAppBar(
                                    containerColor = Color.White,
                                    tonalElevation = 0.dp
                                ) {
                                    val currentDestination = navBackStackEntry?.destination

                                    // 首页
                                    IconButton(
                                        onClick = {
                                            navController.navigate(Screen.Home.route) {
                                                popUpTo(navController.graph.id) {
                                                    inclusive = false
                                                }
                                                launchSingleTop = true
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.Default.Home,
                                                contentDescription = "首页",
                                                tint = if (currentDestination?.hierarchy?.any { it.route == Screen.Home.route } == true)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "首页",
                                                fontSize = 12.sp,
                                                color = if (currentDestination?.hierarchy?.any { it.route == Screen.Home.route } == true)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // 拍照识别（居中，突出显示，嵌入在导航栏内）
                                    IconButton(
                                        onClick = {
                                            navController.navigate("camera")
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(56.dp)
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Camera,
                                                    contentDescription = "拍照识别",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }
                                    }

                                    // 个人中心
                                    IconButton(
                                        onClick = {
                                            navController.navigate(Screen.Profile.route) {
                                                popUpTo(Screen.Home.route) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "个人中心",
                                                tint = if (currentDestination?.hierarchy?.any { it.route == Screen.Profile.route } == true)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "个人中心",
                                                fontSize = 12.sp,
                                                color = if (currentDestination?.hierarchy?.any { it.route == Screen.Profile.route } == true)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
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
                                    },
                                    onSearchClick = {
                                        navController.navigate("search")
                                    }
                                )
                            }

                            composable("search") {
                                SearchScreen(navController = navController)
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

                            composable(Screen.Profile.route) {
                                ProfileScreen(
                                    navController = navController,
                                    viewModel = viewModel
                                )
                            }

                            composable("favorites") {
                                FavoriteScreen(
                                    viewModel = viewModel,
                                    onFavoriteItemClick = { animalId ->
                                        navController.navigate("detail/$animalId")
                                    }
                                )
                            }

                            composable("history") {
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