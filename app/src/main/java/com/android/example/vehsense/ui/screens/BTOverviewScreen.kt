package com.android.example.vehsense.ui.screens

import android.provider.Settings
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.example.vehsense.storage.BluetoothStorage
import com.android.example.vehsense.ui.viewmodels.BTConnectViewModel
import com.android.example.vehsense.ui.viewmodels.DashboardBTViewModel
import com.android.example.vehsense.ui.viewmodels.utils.getMainViewModel

@Composable
fun BTOverviewScreen(
    onGoToConnectScreen: () -> Unit
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
            onGoToConnectScreen()
        }) {
            Text("Select new device")
        }
    }
}
