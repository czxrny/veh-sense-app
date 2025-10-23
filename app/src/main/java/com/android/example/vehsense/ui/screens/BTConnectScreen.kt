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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.example.vehsense.model.DeviceInfo
import com.android.example.vehsense.ui.viewmodels.BTConnectViewModel
import com.android.example.vehsense.ui.viewmodels.utils.getMainViewModel

@Composable
fun BTConnectScreen(
    btConnectViewModel: BTConnectViewModel = viewModel(),
    onSelectedDevice: () -> Unit,
) {
    val mainViewModel = getMainViewModel()
    val btIsOn by mainViewModel.btIsOn.collectAsState()

    val devices by btConnectViewModel.devices.collectAsState()
    var message by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(btIsOn) {
        if (btIsOn) btConnectViewModel.startDiscovery()
    }
    DisposableEffect(Unit) {
        onDispose { btConnectViewModel.stopDiscovery() }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        if (!btIsOn) {
            Button(onClick = {
                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
            }) { Text("Enable Bluetooth") }
        } else {
            Text("Available Devices:")
            if (devices.isNotEmpty()) {
                LazyColumn {
                    items(devices) { device ->
                        val name = try {
                            device.name ?: "Unknown Device"
                        } catch (e: SecurityException) {
                            Log.d("SecurityEx", "as")
                            return@items
                        }
                        Button(onClick = {
                            btConnectViewModel.stopDiscovery()
                            mainViewModel.updateDeviceInfo(DeviceInfo(device.name, device.address))
                            onSelectedDevice()
                        }) {
                            Text(name)
                        }
                    }
                }
            }
        }
    }
    if (message.isNotEmpty()) {
        Text("Status: $message")
    }
}
