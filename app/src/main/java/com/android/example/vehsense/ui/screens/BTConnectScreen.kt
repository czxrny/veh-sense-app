package com.android.example.vehsense.ui.screens

import android.app.Activity
import android.provider.Settings
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import com.android.example.vehsense.bluetooth.BluetoothHandler

@Composable
fun BTConnectScreen(
    btIsOn: State<Boolean>,
    hasPermission: State<Boolean>,
    onConnect: (BluetoothSocket) -> Unit
) {
    val context = LocalContext.current
    var devices by remember { mutableStateOf(listOf<BluetoothDevice>()) }
    var message by remember { mutableStateOf("") }

    val bluetoothHandler = remember(context) {
        BluetoothHandler(
            context = context,
            onMessage = { message = it },
            onDevicesUpdated = { devices = it },
            onFrameUpdate = { /* skipping polling for now */ }
        )
    }

    Column(modifier = Modifier.padding(16.dp)) {
        if (!hasPermission.value) {
            Text("Please grant BT Permissions and restart the App")
        } else {
            if (!btIsOn.value) {
                Button(onClick = {
                    context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                }) { Text("Enable Bluetooth") }
            } else {
                Button(onClick = {
                    bluetoothHandler.startDiscovery()
                }) { Text("Start discovery") }
                Text("Available Devices:")
                LazyColumn {
                    items(devices) { device ->
                        val name = try {
                            device.name ?: "Unknown Device"
                        } catch (e: SecurityException) {
                            Log.d("SecurityEx", "as")
                            return@items
                        }
                        Button(onClick = {
                            val socket = bluetoothHandler.getBtSocket(device)
                            if (socket != null) {
                                onConnect(socket)
                            } else {
                                // INFORM THAT THE DEVICE IS INCORRECT
                            }
                        }) {
                            Text(name)
                        }
                    }
                }
            }
        }
        if (message.isNotEmpty()) {
            Text("Status: $message")
        }
    }
}
