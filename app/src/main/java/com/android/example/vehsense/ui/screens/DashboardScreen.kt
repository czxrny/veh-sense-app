package com.android.example.vehsense.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.example.vehsense.ui.viewmodels.utils.getMainViewModel

@Composable
fun DashboardScreen(
    onGoToBT: () -> Unit,
    onGoToVehicles: () -> Unit,
    onGoToReports: () -> Unit,
    onGoToRideScreen: () -> Unit
) {
    val viewModel = getMainViewModel()
    val btIsOn by viewModel.btIsOn.collectAsState()
    val socket by viewModel.socket.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()

    val deviceInfo by viewModel.deviceInfo.collectAsState()

    val elmMessageSuffix = "Connection state:"
    var elmMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Dashboard", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { onGoToBT() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Select Your BT Device", style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onGoToVehicles,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Check out your vehicles", style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onGoToReports,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("See your reports", style = MaterialTheme.typography.bodyLarge)
        }

        elmMessage = when(isConnected) {
            true -> "$elmMessageSuffix OK"
            false -> "$elmMessageSuffix NOT CONNECTED"
            null -> "$elmMessageSuffix CONNECTING..."
        }

        Text("Bluetooth State: $btIsOn")
        Text(elmMessage)
        Spacer(Modifier.height(8.dp))

        if (isConnected != null && isConnected == false && deviceInfo != null) {
            Button(
                onClick = { viewModel.connectToSavedDevice() },
                enabled = btIsOn,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Connect to the OBD-II")
            }
        } else if (isConnected == true) {
            Button(
                onClick = { viewModel.disconnectFromDevice() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Disconnect from device")
            }
        }

        Button(
            onClick = { onGoToRideScreen() },
            enabled = btIsOn && socket != null && socket!!.isConnected,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Green,
                contentColor = Color.White,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.DarkGray
            )) {
            Text("Start The Ride!", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
