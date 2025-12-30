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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.example.vehsense.core.AppContainer
import com.android.example.vehsense.storage.BluetoothStorage
import com.android.example.vehsense.ui.components.TileData
import com.android.example.vehsense.ui.screens.DeviceDiscoveryScreen
import com.android.example.vehsense.ui.screens.DeviceOverviewScreen
import com.android.example.vehsense.ui.screens.ReportsScreen
import com.android.example.vehsense.ui.screens.RideScreen
import com.android.example.vehsense.ui.screens.RideUiState
import com.android.example.vehsense.ui.screens.UserScreen
import com.android.example.vehsense.ui.screens.UserUiState
import com.android.example.vehsense.ui.screens.VehicleAddScreen
import com.android.example.vehsense.ui.screens.VehicleUpdateScreen
import com.android.example.vehsense.ui.screens.VehiclesScreen
import com.android.example.vehsense.ui.screens.VehiclesUiState
import com.android.example.vehsense.ui.theme.VehSenseTheme
import com.android.example.vehsense.ui.viewmodels.AuthViewModel
import com.android.example.vehsense.ui.viewmodels.DeviceDiscoveryViewModel
import com.android.example.vehsense.ui.viewmodels.RideViewModel
import com.android.example.vehsense.ui.viewmodels.SplashViewModel
import com.android.example.vehsense.ui.viewmodels.UserViewModel
import com.android.example.vehsense.ui.viewmodels.VehicleAddViewModel
import com.android.example.vehsense.ui.viewmodels.VehicleUpdateViewModel
import com.android.example.vehsense.ui.viewmodels.VehicleViewModel
import com.android.example.vehsense.ui.viewmodels.utils.RideViewModelFactory
import com.android.example.vehsense.ui.viewmodels.utils.SharedBackendViewModelFactory
import com.android.example.vehsense.ui.viewmodels.utils.getMainViewModel

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
                                AppContainer.sessionManager.saveSession(
                                    it.token,
                                    it.refreshKey,
                                    it.localId
                                )

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
                                AppContainer.sessionManager.saveSession(
                                    it.token,
                                    it.refreshKey,
                                    it.localId
                                )

                                navController.navigate("dashboard") {
                                    popUpTo("signup") { inclusive = true }
                                }
                            }
                        }

                        val error by vm.errorMessage.collectAsState()

                        SignUpScreen(
                            onSignUpAttempt = { vm.signUp(it.name, it.email, it.password) },
                            errorMessage = error,
                            onGoToLogin = {
                                navController.navigate("login")
                            }
                        )
                    }
                    composable("dashboard") {
                        val vm = getMainViewModel()

                        val tilesArray = arrayOf(
                            TileData("Select your ELM327 Device", Icons.Filled.Bluetooth) { navController.navigate("btOverview")  },
                            TileData("Check out your vehicles", Icons.Filled.DirectionsCar) { navController.navigate("vehicles")  },
                            TileData("See your reports", Icons.AutoMirrored.Filled.Assignment) { navController.navigate("reports")  },
                            TileData("Driver info", Icons.Filled.Person) { navController.navigate("userScreen")  },
                        )

                        DashboardScreen(
                            viewModel = vm,
                            tilesArray,
                            onGoToRideScreen = { navController.navigate("ride") },
                        )
                    }
                    composable("btOverview") {
                        val vm = getMainViewModel()
                        val deviceInfo by vm.deviceInfo.collectAsState()

                        DeviceOverviewScreen(
                            deviceInfo = deviceInfo,
                            onGoToDiscoveryScreen = {
                                navController.navigate("deviceDiscovery")
                            },
                        )
                    }
                    composable("deviceDiscovery") {
                        val vm = getMainViewModel()
                        val btIsOn by vm.btIsOn.collectAsState()

                        val deviceVM: DeviceDiscoveryViewModel = viewModel()

                        DeviceDiscoveryScreen(
                            btIsOn = btIsOn,
                            deviceDiscoveryViewModel = deviceVM,
                            onSelectedDevice = {
                                vm.updateDeviceInfo(it)
                                navController.popBackStack()
                            },
                        )
                    }
                    composable("reports") {
                        ReportsScreen()
                    }
                    composable("vehicles") {
                        val mainVM = getMainViewModel()

                        val vm: VehicleViewModel = viewModel(
                            factory = SharedBackendViewModelFactory(AppContainer.sessionManager)
                        )

                        val vehiclesState by vm.vehiclesInfo.collectAsState()

                        VehiclesScreen(
                            uiState = VehiclesUiState(
                                vehiclesState = vehiclesState,
                                isPrivate = vm.isPrivate
                            ),
                            onRefresh = { vm.getVehicles() },
                            onSaveVehicle = { mainVM.setCurrentVehicle(it) },
                            onDelete = {
                                vm.deleteVehicle(it)
                                mainVM.setCurrentVehicle(null)
                            },
                            onGoToAddScreen = {
                                navController.navigate("vehicleAddScreen")
                            },
                            onGoToUpdateScreen = { navController.navigate("vehicleUpdateScreen/$it") }
                        )
                    }
                    composable("vehicleAddScreen") {
                        val vm: VehicleAddViewModel = viewModel(
                            factory = SharedBackendViewModelFactory(AppContainer.sessionManager)
                        )

                        val error by vm.errorMessage.collectAsState()
                        val isSuccess by vm.isSuccess.collectAsState()

                        LaunchedEffect(isSuccess) {
                            if (isSuccess == true) {
                                navController.popBackStack()
                            }
                        }

                        VehicleAddScreen(
                            onSubmit = { vm.addVehicle(it) },
                            errorMessage = error
                        )
                    }
                    composable("vehicleUpdateScreen/{id}") { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id")?.toIntOrNull()
                        id?.let { vehicleId ->
                            val vm: VehicleUpdateViewModel = viewModel(
                                factory = SharedBackendViewModelFactory(AppContainer.sessionManager)
                            )

                            val error by vm.errorMessage.collectAsState()
                            val isSuccess by vm.isSuccess.collectAsState()

                            LaunchedEffect(isSuccess) {
                                if (isSuccess == true) {
                                    navController.popBackStack()
                                }
                            }

                            VehicleUpdateScreen(
                                onSubmit = { vm.updateVehicle(it, vehicleId) },
                                errorMessage = error
                            )
                        }
                    }
                    composable("userScreen") {
                        val userVM = viewModel<UserViewModel>(
                            factory = SharedBackendViewModelFactory(AppContainer.sessionManager)
                        )

                        LaunchedEffect(Unit) {
                            userVM.loadUserInfo()
                        }

                        val isPrivate = userVM.isPrivate
                        val userInfo by userVM.userInfo.collectAsState()

                        UserScreen(
                            uiState = UserUiState(
                                isPrivate = isPrivate,
                                userInfo = userInfo,
                            ),
                            onGoBack = { navController.popBackStack() },
                            onLogout = {
                                AppContainer.sessionManager.logout()
                                navController.navigate("login") {
                                    popUpTo("dashboard") {
                                        inclusive = true
                                    }
                                }
                            }
                        )
                    }
                    composable("ride/{vehicleId}") { backStackEntry ->
                        val id = requireNotNull(backStackEntry.arguments?.getString("vehicleId")).toInt()
                        val vm = getMainViewModel()
                        val socket by vm.socket.collectAsState()

                        if (socket == null) {
                            navController.navigate("dashboard") {
                                popUpTo("ride") { inclusive = true }
                            }
                        } else {
                            val rideVM: RideViewModel = viewModel(
                                factory = RideViewModelFactory(
                                    vehicleId = id,
                                    sessionManager = AppContainer.sessionManager,
                                    communicator = AppContainer.backend,
                                    obdFrameDao = AppContainer.obdFrameDao,
                                    btSocket = socket!!
                                )
                            )

                            LaunchedEffect(Unit) {
                                rideVM.pollData()
                            }

                            val obdFrame by rideVM.obdFrame.collectAsState()
                            val connectionWasInterrupted by rideVM.connectionWasInterrupted.collectAsState()

                            RideScreen(
                                uiState = RideUiState(
                                    obdFrame = obdFrame,
                                    connectionWasInterrupted = connectionWasInterrupted
                                ),
                                onStopTheRide = {
                                    rideVM.stopPolling()
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
}
