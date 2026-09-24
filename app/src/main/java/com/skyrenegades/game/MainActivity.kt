package com.skyrenegades.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.skyrenegades.game.ui.GameOverScreen
import com.skyrenegades.game.ui.GameScreen
import com.skyrenegades.game.ui.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SkyRenegadesApp()
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun SkyRenegadesApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onPlayClicked = { navController.navigate("game") }
            )
        }
        composable("game") {
            GameScreen(
                onGameOver = { score ->
                    navController.navigate("gameOver/$score") {
                        popUpTo("home")
                    }
                }
            )
        }
        composable("gameOver/{score}") { backStackEntry ->
            val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
            GameOverScreen(
                score = score,
                onPlayAgain = { navController.navigate("game") { popUpTo("home") } },
                onHome = { navController.navigate("home") { popUpTo("home") { inclusive = true } } }
            )
        }
    }
}
