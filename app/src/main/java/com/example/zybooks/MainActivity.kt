package com.example.zybooks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.zybooks.ui.theme.ProjectFirstGoTheme
import com.example.zybooks.ui.theme.ui.EggHatch
import com.example.zybooks.ui.theme.ui.EggScreen
import com.example.zybooks.ui.theme.ui.LoadingScreen
import com.example.zybooks.ui.theme.ui.LogInScreen
import com.example.zybooks.ui.theme.ui.SignUpScreen
import com.example.zybooks.ui.theme.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjectFirstGoTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "loadingscreen"
                ) {
                    composable("loadingscreen") {
                        LoadingScreen(navController = navController)
                    }

                    composable("loginscreen") {
                        LogInScreen(navController = navController)
                    }
                    
                    composable("eggscreen"){
                        EggScreen(navController = navController)
                    }

                    composable(
                        route = "hatchscreen/{eggId}",
                        arguments = listOf(navArgument("eggId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val eggId = backStackEntry.arguments?.getInt("eggId") ?: 0
                        EggHatch(navController = navController, eggId = eggId)
                    }

                    composable("signupscreen"){
                        SignUpScreen(navController = navController)
                    }

                    composable("homescreen") {
                        HomeScreen(navController = navController)
                    }

                    composable("shopscreen") {
                        ShopScreen(navController = navController)
                    }

                    composable("cinemascreen") {
                        CinemaScreen(navController = navController)
                    }

                    composable("evolutionscreen") {
                        EvolutionScreen(navController = navController)
                    }
                }
            }
        }
    }
}
