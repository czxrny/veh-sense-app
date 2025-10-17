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
import com.android.example.vehsense.storage.UserStorage
import com.android.example.vehsense.ui.screens.ReportsScreen
import com.android.example.vehsense.ui.screens.VehiclesScreen
import com.android.example.vehsense.ui.viewmodels.AuthViewModel
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

        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "splash") {
                composable("splash") {
                    val context = LocalContext.current
                    val userStorage = UserStorage(context)
                    val scope = rememberCoroutineScope()
                    SplashScreen(onFinished = {
                        if (userStorage.wasPreviouslyLoggedIn()) {
                            val session = userStorage.getSession()
                            if (session != null) {
                                val backend = BackendCommunicator()
                                val userId: Int = session.userId.toInt()
                                val refreshKey = session.refreshKey
                                scope.launch {
                                    try {
                                        val authResponse = backend.getFreshToken(userId, refreshKey).getOrThrow()
                                        userStorage.saveSession(authResponse.localId, authResponse.refreshKey)

                                        navController.navigate("dashboard/${authResponse.localId}/${authResponse.token}") {
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
                    val context = LocalContext.current
                    val userStorage = UserStorage(context)

                    LoginScreen(
                        onGoToSignUp = {
                            navController.navigate("signup")
                        },
                        onLoginSuccess = {
                            userStorage.saveSession(it.localId, it.refreshKey)
                            navController.navigate("dashboard/${it.localId}/${it.token}") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }
                composable("signup") {
                    val context = LocalContext.current
                    val userStorage = UserStorage(context)

                    SignUpScreen(
                        onGoBack = { navController.popBackStack() },
                        onSignUpSuccess = {
                            userStorage.saveSession(it.localId, it.refreshKey)
                            navController.navigate("dashboard/${it.localId}/${it.token}") {
                                popUpTo("signup") { inclusive = true }
                            }
                        }
                    )
                }
                composable("dashboard/{userId}/{token}", arguments = listOf(
                    navArgument("userId") { type = NavType.IntType },
                    navArgument("token") { type = NavType.StringType }
                )) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getInt("userId")
                    val token = backStackEntry.arguments?.getString("token")

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
                    ReportsScreen()
                }
                composable("vehicles") {
                    VehiclesScreen()
                }
            }
        }
    }
}
