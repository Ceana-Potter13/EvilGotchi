package com.example.zybooks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

                    // ADDED HOMESCREEN ROUTE
                    composable("homescreen") {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Welcome to the Home Screen!")
                        }
                    }
                }
            }
        }
    }
}
