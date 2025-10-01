package com.android.example.vehsense

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
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
import com.android.example.vehsense.bluetooth.BluetoothHandler
import com.android.example.vehsense.model.ObdFrame

class MainActivity : ComponentActivity() {
    private lateinit var bluetoothHandler: BluetoothHandler
    private lateinit var btReceiver: BroadcastReceiver

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val localContext = LocalContext.current
            var devices by remember { mutableStateOf(listOf<BluetoothDevice>()) }
            var obdFrame by remember { mutableStateOf(ObdFrame())}
            var bluetoothIsOn by remember { mutableStateOf(false) }
            var hasPermission by remember { mutableStateOf(false) }

            val btReceiver = BluetoothStateReceiver(onBluetoothChange = {bluetoothIsOn = it})
            val stateChangingFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            localContext.registerReceiver(btReceiver, stateChangingFilter)

            bluetoothHandler = BluetoothHandler(
                context = localContext,
                onMessage = { msg ->
                    (localContext as Activity).runOnUiThread {
                        Toast.makeText(localContext, msg, Toast.LENGTH_SHORT).show()
                    }
                },
                onDevicesUpdated = { list ->
                    devices = list
                },
                onFrameUpdate = { newFrame ->
                    obdFrame = newFrame
                },
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
                            Text("RPM: ${obdFrame.rpm}", style = MaterialTheme.typography.bodyMedium)
                            Text("Load: ${obdFrame.rpm}%", style = MaterialTheme.typography.bodyMedium)
                            Text("Speed: ${obdFrame.rpm} km/h", style = MaterialTheme.typography.bodyMedium)

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
        try { this.unregisterReceiver(btReceiver) } catch (_: Exception) {}
        bluetoothHandler.cleanup()
    }
}
