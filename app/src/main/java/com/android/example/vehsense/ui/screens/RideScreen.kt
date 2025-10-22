package com.android.example.vehsense.ui.screens

import android.bluetooth.BluetoothSocket
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.example.vehsense.ui.viewmodels.RideViewModel
import com.android.example.vehsense.ui.viewmodels.RideViewModelFactory

@Composable
fun RideScreen(
    userId: Int,
    token: String,
    btSocket: BluetoothSocket,
    onForceBack: () -> Unit,
    btIsOn: State<Boolean>,
    ) {

    val viewModel = viewModel<RideViewModel>(
        factory = RideViewModelFactory(userId, token, btSocket)
    )
    val obdFrame by viewModel.obdFrame.collectAsState()

    if(!btIsOn.value) {
        onForceBack()
    }

    LaunchedEffect(Unit) {
        viewModel.pollData()
    }

    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text("This is the ride screen", style = MaterialTheme.typography.titleLarge)

        Text("RPM:${obdFrame.rpm}")
        Text("Engine Load:${obdFrame.engineLoad}")
        Text("Speed:${obdFrame.vehicleSpeed}")

        Button(
            onClick = {
                viewModel.stopPolling()
                onForceBack()
            }
        ) {
            Text("End the ride", style = MaterialTheme.typography.bodyLarge)
        }
    }
}