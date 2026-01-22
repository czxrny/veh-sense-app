package com.android.example.vehsense.ui.screens

import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.example.vehsense.model.DeviceInfo
import com.android.example.vehsense.ui.components.StandardScreen
import com.android.example.vehsense.ui.viewmodels.DeviceDiscoveryViewModel

@Composable
fun DeviceDiscoveryScreen(
    btIsOn: Boolean,
    deviceDiscoveryViewModel: DeviceDiscoveryViewModel,
    onSelectedDevice: (DeviceInfo) -> Unit,
) {
    val devices by deviceDiscoveryViewModel.devices.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(btIsOn) {
        if (btIsOn) deviceDiscoveryViewModel.startDiscovery()
    }
    DisposableEffect(Unit) {
        onDispose { deviceDiscoveryViewModel.stopDiscovery() }
    }

    StandardScreen(
        topText = "Device Discovery"
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (!btIsOn) {
                Button(onClick = {
                    context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                }) { Text("Enable Bluetooth") }
            } else {
                Text("Available Devices:")
                Spacer(modifier = Modifier.height(8.dp))
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
                                onSelectedDevice(DeviceInfo(device.name, device.address))
                            },
                            ) {
                                Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                    Text(text = name)
                                    Text(text = device.address, fontSize = 10.sp)
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                        }
                    }
                } else {

                }
            }
        }
    }
}
