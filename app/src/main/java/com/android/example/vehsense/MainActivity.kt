package com.android.example.vehsense

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.bluetooth.BluetoothDevice
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private lateinit var bluetoothHandler: BluetoothHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val localContext = LocalContext.current
            var devices by remember { mutableStateOf(listOf<BluetoothDevice>()) }
            var rpm by remember { mutableIntStateOf(0) }
            var bluetoothIsOn by remember { mutableStateOf(false) }
            var hasPermission by remember { mutableStateOf(false) }

            bluetoothHandler = BluetoothHandler(
                context = localContext,
                onMessage = { msg ->
                    Toast.makeText(localContext, msg, Toast.LENGTH_SHORT).show()
                },
                onDevicesUpdated = { list ->
                    devices = list
                },
                onRPMUpdated = { newRPM ->
                    rpm = newRPM
                },
                onBluetoothStateChange = { isOn ->
                    bluetoothIsOn = isOn
                }
            )

            Scaffold { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    if (!hasPermission) {
                        Button(
                            onClick = {
                                bluetoothHandler.checkAndRequestPermissions(this@MainActivity) {
                                    if (bluetoothHandler.hasPermissions(this@MainActivity)) {
                                        hasPermission = true
                                        bluetoothHandler.startDiscovery()
                                    }
                                }
                            }
                        ) {
                            Text("Grant Bluetooth Permission")
                        }
                    } else {
                        if (bluetoothIsOn) {
                            Text("Available Devices:", style = MaterialTheme.typography.titleMedium)

                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(devices) { device ->
                                    val name = try {
                                        device.name ?: "Unknown Device"
                                    } catch (e: SecurityException) {
                                        Toast.makeText(
                                            localContext,
                                            "Permission required",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@items
                                    }

                                    Button(
                                        onClick = { bluetoothHandler.connectToDevice(device) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Text(name)
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            Text("RPM: $rpm", style = MaterialTheme.typography.titleLarge)

                        } else {
                            Text("Please enable Bluetooth to proceed.")
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                                    localContext.startActivity(intent)
                                }
                            ) {
                                Text("Enable Bluetooth")
                            }
                        }
                    }
                }
            }

            LaunchedEffect(Unit) {
                bluetoothIsOn = bluetoothHandler.bluetoothIsEnabled()
                hasPermission = bluetoothHandler.hasPermissions(this@MainActivity)
                if (hasPermission && bluetoothIsOn) {
                    bluetoothHandler.startDiscovery()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        bluetoothHandler.cleanup()
    }
}
