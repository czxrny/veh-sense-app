package com.android.example.vehsense

import  com.android.example.vehsense.ui.screens.SplashScreen
import  com.android.example.vehsense.ui.screens.DashboardScreen
import  com.android.example.vehsense.ui.screens.BTConnectScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.example.vehsense.ui.screens.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "splash") {
                composable("splash") {
                    SplashScreen(onFinished = { navController.navigate("dashboard") })
                }
                composable("dashboard") {
                    val viewModel: MainViewModel = viewModel()
                    DashboardScreen(viewModel, onGoToBT = { navController.navigate("btconnect") })
                }
                composable("btconnect") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("dashboard")
                    }
                    val viewModel: MainViewModel = viewModel(parentEntry)
                    BTConnectScreen(viewModel)
                }
            }
        }
    }
}
