package com.android.example.vehsense

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import com.android.example.vehsense.ui.screens.SplashScreen
import com.android.example.vehsense.ui.screens.DashboardScreen
import com.android.example.vehsense.ui.screens.LoginScreen
import com.android.example.vehsense.ui.screens.SignUpScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.example.vehsense.core.AppContainer
import com.android.example.vehsense.storage.BluetoothStorage
import com.android.example.vehsense.ui.screens.DeviceDiscoveryScreen
import com.android.example.vehsense.ui.screens.DeviceOverviewScreen
import com.android.example.vehsense.ui.screens.ReportsScreen
import com.android.example.vehsense.ui.screens.RideScreen
import com.android.example.vehsense.ui.screens.VehicleAddScreen
import com.android.example.vehsense.ui.screens.VehiclesScreen
import com.android.example.vehsense.ui.theme.VehSenseTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missing.toTypedArray(),
                1001
            )
        }

        AppContainer.init(applicationContext)
        BluetoothStorage.init(applicationContext)

        setContent {
            VehSenseTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") {
                        val scope = rememberCoroutineScope()
                        SplashScreen(onFinished = {
                            scope.launch {
                                val ok = AppContainer.sessionManager.loadSession()
                                if (ok) {
                                    navController.navigate("dashboard") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                } else {
                                    navController.navigate("login") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            }
                        }
                        )
                    }
                    composable("login") {
                        LoginScreen(
                            onGoToSignUp = {
                                navController.navigate("signup")
                            },
                            onLoginSuccess = {
                                AppContainer.sessionManager.saveSession(it.token, it.refreshKey, it.localId)

                                navController.navigate("dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("signup") {
                        SignUpScreen(
                            onGoBack = { navController.popBackStack() },
                            onSignUpSuccess = {
                                AppContainer.sessionManager.saveSession(it.token, it.refreshKey, it.localId)

                                navController.navigate("dashboard") {
                                    popUpTo("signup") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("dashboard") {
                        DashboardScreen(
                            onGoToBT = { navController.navigate("btOverview") },
                            onGoToVehicles = { navController.navigate("vehicles") },
                            onGoToReports = { navController.navigate("reports") },
                            onGoToRideScreen = { navController.navigate("ride") }
                        )
                    }
                    composable("btOverview") {
                        DeviceOverviewScreen(
                            onGoToDiscoveryScreen = {
                                navController.navigate("deviceDiscovery")
                            },
                        )
                    }
                    composable("deviceDiscovery") {
                        DeviceDiscoveryScreen(
                            onSelectedDevice = {
                                navController.popBackStack()
                            },
                        )
                    }
                    composable("reports") {
                        ReportsScreen()
                    }
                    composable("vehicles") {
                        VehiclesScreen(
                            onGoToAddScreen = {
                                navController.navigate("vehicleAddScreen")
                            }
                        )
                    }
                    composable("vehicleAddScreen") {
                        VehicleAddScreen(
                            onFinished = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable("ride") {
                        RideScreen(
                            onForceBack = {
                                navController.navigate("dashboard") {
                                    popUpTo("ride") { inclusive = true }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
