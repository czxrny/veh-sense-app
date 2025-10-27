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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.android.example.vehsense.ui.screens.VehiclesUiState
import com.android.example.vehsense.ui.theme.VehSenseTheme
import com.android.example.vehsense.ui.viewmodels.AuthViewModel
import com.android.example.vehsense.ui.viewmodels.SplashViewModel
import com.android.example.vehsense.ui.viewmodels.VehicleAddViewModel
import com.android.example.vehsense.ui.viewmodels.VehicleViewModel
import com.android.example.vehsense.ui.viewmodels.utils.SharedBackendViewModelFactory
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
                        val vm: SplashViewModel = viewModel(
                            factory = SharedBackendViewModelFactory(AppContainer.sessionManager)
                        )

                        val isSessionValid by vm.isSessionValid.collectAsState()

                        SplashScreen()

                        LaunchedEffect(isSessionValid) {
                            when (isSessionValid) {
                                true -> navController.navigate("dashboard") {
                                    popUpTo("splash") { inclusive = true }
                                }

                                false -> navController.navigate("login") {
                                    popUpTo("splash") { inclusive = true }
                                }

                                null -> Unit
                            }
                        }
                    }
                    composable("login") {
                        val vm: AuthViewModel = viewModel()

                        val currentSession by vm.currentSession.collectAsState()

                        LaunchedEffect(currentSession) {
                            currentSession?.let {
                                AppContainer.sessionManager.saveSession(it.token, it.refreshKey, it.localId)

                                navController.navigate("dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        }

                        val error by vm.errorMessage.collectAsState()

                        LoginScreen(
                            onLoginAttempt = { vm.login(it.email, it.password) },
                            errorMessage = error,
                            onGoToSignUp = {
                                navController.navigate("signup")
                            }
                        )
                    }
                    composable("signup") {
                        val vm: AuthViewModel = viewModel()

                        val currentSession by vm.currentSession.collectAsState()

                        LaunchedEffect(currentSession) {
                            currentSession?.let {
                                AppContainer.sessionManager.saveSession(it.token, it.refreshKey, it.localId)

                                navController.navigate("dashboard") {
                                    popUpTo("signup") { inclusive = true }
                                }
                            }
                        }

                        val error by vm.errorMessage.collectAsState()

                        SignUpScreen(
                            onSignUpAttempt = { vm.login(it.email, it.password) },
                            errorMessage = error,
                            onGoToLogin = {
                                navController.navigate("login")
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
                        val vm: VehicleViewModel = viewModel(
                            factory = SharedBackendViewModelFactory(AppContainer.sessionManager)
                        )
                        val vehicles by vm.vehicles.collectAsState()
                        val error by vm.errorMessage.collectAsState()

                        VehiclesScreen(
                            uiState = VehiclesUiState(
                                vehicles = vehicles,
                                error = error
                            ),
                            onRefresh = { vm.getVehicles() },
                            onDelete = { vm.deleteVehicle(it) },
                            onGoToAddScreen = {
                                navController.navigate("vehicleAddScreen")
                            }
                        )
                    }
                    composable("vehicleAddScreen") {
                        val vm: VehicleAddViewModel = viewModel(
                            factory = SharedBackendViewModelFactory(AppContainer.sessionManager)
                        )

                        val error by vm.errorMessage.collectAsState()

                        VehicleAddScreen(
                            onSubmit = { vm.addVehicle(
                                it,
                                onSuccess = { navController.popBackStack() }
                            ) },
                            errorMessage = error
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
