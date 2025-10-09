package com.android.example.vehsense.ui.screens

import android.app.Activity
import android.provider.Settings
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
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
fun BTConnectScreen() {
    class BluetoothStateReceiver(
        private val onBluetoothChange: (Boolean) -> Unit
    ) : BroadcastReceiver() {
        @Suppress("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_ON -> onBluetoothChange(true)
                    BluetoothAdapter.STATE_OFF -> onBluetoothChange(false)
                }
            }
        }
    }

    val context = LocalContext.current
    var devices by remember { mutableStateOf(listOf<BluetoothDevice>()) }
    var bluetoothIsOn by remember { mutableStateOf(false) }
    var hasPermission by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val bluetoothHandler = remember(context) {
        BluetoothHandler(
            context = context,
            onMessage = { message = it },
            onDevicesUpdated = { devices = it },
            onFrameUpdate = { /* skipping polling for now */ }
        )
    }

    val btReceiver = BluetoothStateReceiver(onBluetoothChange = {bluetoothIsOn = it})
    val stateChangingFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
    context.registerReceiver(btReceiver, stateChangingFilter)

    DisposableEffect(Unit) {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(btReceiver, filter)
        onDispose {
            context.unregisterReceiver(btReceiver)
            bluetoothHandler.cleanup()
        }
    }

    LaunchedEffect(Unit) {
        bluetoothIsOn = bluetoothHandler.bluetoothIsEnabled()
        hasPermission = bluetoothHandler.hasPermissions(context as Activity)
    }

    Column(modifier = Modifier.padding(16.dp)) {
        if (!hasPermission) {
            Button(onClick = {
                bluetoothHandler.checkAndRequestPermissions(context as Activity) {
                    hasPermission = true
                    bluetoothHandler.startDiscovery()
                }
            }) { Text("Grant Bluetooth Permission") }
        } else if (!bluetoothIsOn) {
            Button(onClick = {
                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
            }) { Text("Enable Bluetooth") }
        } else {
            Text("Available Devices:")
            LazyColumn {
                items(devices) { device ->
                    val name = try {
                        device.name ?: "Unknown Device"
                    } catch (e: SecurityException) {
                        Log.d("SecurityEx", "as")
                        return@items
                    }
                    Button(onClick = { bluetoothHandler.connectToDevice(device) }) {
                        Text(name)
                    }
                }
            }
        }

        if (message.isNotEmpty()) {
            Text("Status: $message")
        }
    }
}
