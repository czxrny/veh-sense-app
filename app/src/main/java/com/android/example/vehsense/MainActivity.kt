package com.android.example.vehsense

import  com.android.example.vehsense.ui.screens.SplashScreen
import  com.android.example.vehsense.ui.screens.DashboardScreen
import  com.android.example.vehsense.ui.screens.BTConnectScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

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
                    DashboardScreen(onGoToBT = { navController.navigate("btconnect") })
                }
                composable("btconnect") {
                    BTConnectScreen()
                }
            }
        }
    }
}
