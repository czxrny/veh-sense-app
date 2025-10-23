package com.android.example.vehsense.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.example.vehsense.ui.viewmodels.utils.getMainViewModel

@Composable
fun DeviceOverviewScreen(
    onGoToDiscoveryScreen: () -> Unit
) {
    val viewModel = getMainViewModel()
    val deviceInfo by viewModel.deviceInfo.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        if (deviceInfo != null) {
            Text("Currently selected device", fontSize = 24.sp, style = MaterialTheme.typography.titleLarge)
            Text(deviceInfo!!.name, fontSize = 16.sp)
            Text(deviceInfo!!.address, fontSize = 10.sp)
        } else {
            Text("No selected device", fontSize = 24.sp, style = MaterialTheme.typography.titleLarge)
            Text("Select new device OBD-II ELM327 device to proceed", fontSize = 16.sp)
        }
        Button(onClick = {
            onGoToDiscoveryScreen()
        }) {
            Text("Select new device")
        }
    }
}
