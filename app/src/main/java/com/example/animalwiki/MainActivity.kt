package com.example.animalwiki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.animalwiki.ui.screens.AnimalDetailScreen
import com.example.animalwiki.ui.screens.AnimalListScreen
import com.example.animalwiki.ui.theme.AnimalWikiTheme
import com.example.animalwiki.ui.viewmodel.AnimalViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnimalWikiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: AnimalViewModel = viewModel()

                    NavHost(
                        navController = navController,
                        startDestination = "list"
                    ) {
                        composable("list") {
                            AnimalListScreen(
                                viewModel = viewModel,
                                onAnimalClick = { animalId ->
                                    navController.navigate("detail/$animalId")
                                }
                            )
                        }
                        composable("detail/{animalId}") { backStackEntry ->
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