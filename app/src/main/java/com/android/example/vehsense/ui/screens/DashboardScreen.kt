package com.android.example.vehsense.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.android.example.vehsense.ui.components.DashboardTile
import com.android.example.vehsense.ui.components.TileData
import com.android.example.vehsense.ui.viewmodels.MainViewModel

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    tiles: Array<TileData>,
    onGoToRideScreen: (vehicleId: Int) -> Unit
) {
    val btIsOn by viewModel.btIsOn.collectAsState()
    val socket by viewModel.socket.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val selectedVehicle by viewModel.selectedVehicle.collectAsState()

    val elmMessageSuffix = "Connection state:"
    var elmMessage by remember { mutableStateOf("") }
    var vehicleMessage by remember { mutableStateOf("") }

    LaunchedEffect(isConnected, btIsOn) {
        val shouldPing = isConnected == true && btIsOn
        viewModel.setELMHeartbeat(shouldPing)
    }

    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text("VehSense Dashboard", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(32.dp))
        tiles.forEach { tileData ->
            DashboardTile(tileData)
            Spacer(Modifier.height(32.dp))
        }

        elmMessage = when(isConnected) {
            true -> "$elmMessageSuffix OK"
            false -> "$elmMessageSuffix NOT CONNECTED"
            null -> "$elmMessageSuffix CONNECTING..."
        }

        vehicleMessage = when(selectedVehicle) {
            null -> "No vehicle selected. Select the vehicle in vehicle tab."
            else -> "Selected vehicle: ${selectedVehicle!!.brand} ${selectedVehicle!!.model}"
        }

        val btStatusText = if (btIsOn) "Active" else "Inactive"
        Text("Bluetooth: $btStatusText")
        Text(elmMessage)
        Text(vehicleMessage)
        Spacer(Modifier.height(8.dp))

        if (isConnected == null || isConnected == false) {
            Button(
                onClick = { viewModel.connectToSavedDevice() },
                enabled = btIsOn && deviceInfo != null && isConnected != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.LightGray
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Connect to the OBD-II", style = MaterialTheme.typography.bodyLarge)
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
                Text("Disconnect from device", style = MaterialTheme.typography.bodyLarge)
            }
        }

        Button(
            onClick = {
                viewModel.setELMHeartbeat(enabled = false)
                onGoToRideScreen(selectedVehicle!!.id)
            },
            enabled = btIsOn && socket != null && socket!!.isConnected && selectedVehicle != null,
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
