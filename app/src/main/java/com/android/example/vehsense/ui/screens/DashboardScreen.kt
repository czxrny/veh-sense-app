package com.android.example.vehsense.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.example.vehsense.ui.viewmodels.DashboardBTViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardBTViewModel,
    onGoToBT: () -> Unit,
    onGoToVehicles: () -> Unit,
    onGoToReports: () -> Unit
) {
    val btIsOn by viewModel.btIsOn.collectAsState()
    val socket by viewModel.socket.collectAsState()
    var rideIsActive by remember { mutableStateOf(false) }

    if (!btIsOn) {
        Text("Please enable the Bluetooth to proceed")
    }

    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Dashboard", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onGoToBT,
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
        if (!rideIsActive) {
            Button(onClick = {
                rideIsActive = true
                },
                enabled = btIsOn && socket != null && socket!!.isConnected,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Green,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.DarkGray
                )) {
                Text("Start The Ride!", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Button(onClick = {
                rideIsActive = false
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )) {
                Text("Stop The Ride", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }


}
