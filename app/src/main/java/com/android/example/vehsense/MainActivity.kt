package com.android.example.vehsense

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import  com.android.example.vehsense.ui.screens.SplashScreen
import  com.android.example.vehsense.ui.screens.DashboardScreen
import  com.android.example.vehsense.ui.screens.BTConnectScreen
import com.android.example.vehsense.ui.screens.LoginScreen
import com.android.example.vehsense.ui.screens.SignUpScreen
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.android.example.vehsense.network.BackendCommunicator
import com.android.example.vehsense.repository.BackendRepository
import com.android.example.vehsense.storage.UserStorage
import com.android.example.vehsense.ui.screens.ReportsScreen
import com.android.example.vehsense.ui.screens.VehiclesScreen
import com.android.example.vehsense.ui.theme.VehSenseTheme
import com.android.example.vehsense.ui.viewmodels.BackendViewModel
import com.android.example.vehsense.ui.viewmodels.BackendViewModelFactory
import com.android.example.vehsense.ui.viewmodels.DashboardBTViewModel
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

        UserStorage.init(applicationContext)

        setContent {
            VehSenseTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") {
                        val scope = rememberCoroutineScope()
                        SplashScreen(onFinished = {
                            if (UserStorage.wasPreviouslyLoggedIn()) {
                                val session = UserStorage.getSession()
                                if (session != null) {
                                    val backend = BackendCommunicator()
                                    val userId: Int = session.userId.toInt()
                                    val refreshKey = session.refreshKey
                                    scope.launch {
                                        try {
                                            val authResponse =
                                                backend.getFreshToken(userId, refreshKey)
                                                    .getOrThrow()
                                            UserStorage.saveSession(
                                                authResponse.localId,
                                                authResponse.refreshKey
                                            )

                                            BackendRepository.userId = authResponse.localId
                                            BackendRepository.token = authResponse.token

                                            navController.navigate("dashboard") {
                                                popUpTo("splash") { inclusive = true }
                                            }
                                        } catch (e: Exception) {
                                            Log.d("storage-login-error", e.toString())
                                            navController.navigate("login") {
                                                popUpTo("splash") { inclusive = true }
                                            }
                                        }
                                    }
                                }
                            } else {
                                navController.navigate("login") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        })
                    }
                    composable("login") {
                        LoginScreen(
                            onGoToSignUp = {
                                navController.navigate("signup")
                            },
                            onLoginSuccess = {
                                UserStorage.saveSession(it.localId, it.refreshKey)

                                BackendRepository.userId = it.localId
                                BackendRepository.token = it.token

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
                                UserStorage.saveSession(it.localId, it.refreshKey)

                                BackendRepository.userId = it.localId
                                BackendRepository.token = it.token

                                navController.navigate("dashboard") {
                                    popUpTo("signup") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("dashboard") {
                        if (BackendRepository.userId == null || BackendRepository.token == null) {
                            throw IllegalArgumentException("DashboardScreen requires Backend Repository to be initialized")
                        }

                        val viewModel: DashboardBTViewModel = viewModel()
                        DashboardScreen(
                            viewModel,
                            onGoToBT = { navController.navigate("btconnect") },
                            onGoToVehicles = { navController.navigate("vehicles") },
                            onGoToReports = { navController.navigate("reports") }
                        )
                    }
                    composable("btconnect") { backStackEntry ->
                        val parentEntry = remember(backStackEntry) {
                            navController.getBackStackEntry("dashboard")
                        }
                        val viewModel: DashboardBTViewModel = viewModel(parentEntry)
                        BTConnectScreen(
                            btIsOn = viewModel.btIsOn.collectAsState(),
                            hasPermission = viewModel.hasPermission.collectAsState(),
                            onConnect = { socket ->
                                viewModel.updateSocket(socket)
                                viewModel.saveDeviceAddress(socket.remoteDevice.address)
                            }
                        )
                    }
                    composable("reports") {
                        ReportsScreen(
                            userId = requireNotNull(BackendRepository.userId),
                            token = requireNotNull(BackendRepository.token)
                        )
                    }
                    composable("vehicles") {
                        VehiclesScreen(
                            userId = requireNotNull(BackendRepository.userId),
                            token = requireNotNull(BackendRepository.token)
                        )
                    }
                }
            }
        }
    }
}
