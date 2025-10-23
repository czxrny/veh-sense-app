package com.android.example.vehsense.ui.screens

import android.provider.Settings
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.example.vehsense.model.DeviceInfo
import com.android.example.vehsense.ui.viewmodels.DeviceDiscoveryViewModel
import com.android.example.vehsense.ui.viewmodels.utils.getMainViewModel

@Composable
fun DeviceDiscoveryScreen(
    deviceDiscoveryViewModel: DeviceDiscoveryViewModel = viewModel(),
    onSelectedDevice: () -> Unit,
) {
    val mainViewModel = getMainViewModel()
    val btIsOn by mainViewModel.btIsOn.collectAsState()

    val devices by deviceDiscoveryViewModel.devices.collectAsState()
    var message by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(btIsOn) {
        if (btIsOn) deviceDiscoveryViewModel.startDiscovery()
    }
    DisposableEffect(Unit) {
        onDispose { deviceDiscoveryViewModel.stopDiscovery() }
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
                            device.name ?: "Unknown device"
                        } catch (e: SecurityException) {
                            Log.d("SecurityEx", "as")
                            return@items
                        }
                        Button(onClick = {
                            deviceDiscoveryViewModel.stopDiscovery()
                            mainViewModel.updateDeviceInfo(DeviceInfo(name, device.address))
                            onSelectedDevice()
                        }) {
                            Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text(text = name)
                                Text(text = device.address, fontSize = 10.sp)
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                    }
                }
            }
        }
    }
    if (message.isNotEmpty()) {
        Text("Status: $message")
    }
}
