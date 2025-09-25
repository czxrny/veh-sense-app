package com.android.example.vehsense

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.*

class BluetoothHandler(
    private val context: Context,
    private val onMessage: (String) -> Unit,
    private val onDevicesUpdated: (List<BluetoothDevice>) -> Unit,
    private val onRPMUpdated: (Int) -> Unit,
    private val onBluetoothStateChange: (Boolean) -> Unit
) {

    private val REQUEST_CODE_BT = 1001
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    private var socket: BluetoothSocket? = null
    private var reader: BufferedReader? = null
    private var writer: BufferedWriter? = null
    private val devicesFound = mutableListOf<BluetoothDevice>()

    private val receiver = object : BroadcastReceiver() {
        @Suppress("MissingPermission")
        override fun onReceive(ctx: Context, intent: Intent) {
            if (BluetoothDevice.ACTION_FOUND == intent.action) {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    if (!devicesFound.contains(it)) {
                        devicesFound.add(it)
                        onDevicesUpdated(devicesFound.toList())
                    }
                }
            }
        }
    }

    private val btChangeReceiver = object : BroadcastReceiver() {
        @Suppress("MissingPermission")
        override fun onReceive(ctx: Context, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    when (state) {
                        BluetoothAdapter.STATE_OFF -> {
                            onBluetoothStateChange(false)
                        }
                        BluetoothAdapter.STATE_ON -> {
                            onBluetoothStateChange(true)
                        }
                    }
                }
            }
        }
    }

    private fun getMissingPermissions(activity: android.app.Activity): List<String> {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        return missing
    }

    fun hasPermissions(activity: android.app.Activity): Boolean {
        return getMissingPermissions(activity).isEmpty()
    }

    fun bluetoothIsEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    fun checkAndRequestPermissions(activity: android.app.Activity, onGranted: () -> Unit) {
        val missing = getMissingPermissions(activity)

        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                missing.toTypedArray(),
                REQUEST_CODE_BT
            )
        } else {
            onGranted()
        }
    }

    @Suppress("MissingPermission")
    fun startDiscovery() {
        if (bluetoothAdapter == null) {
            onMessage("Bluetooth not available")
            return
        }

        val findingFilter= IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(receiver, findingFilter)

        val stateChangingFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(btChangeReceiver, stateChangingFilter)

        bluetoothAdapter?.startDiscovery()
    }

    @Suppress("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        onMessage("Connecting to ${device.name}")

        Thread {
            try {
                socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                bluetoothAdapter?.cancelDiscovery()
                socket?.connect()

                writer = BufferedWriter(OutputStreamWriter(socket!!.outputStream))
                reader = BufferedReader(InputStreamReader(socket!!.inputStream))

                // ELM327 basic setup
                sendCommand("ATZ")   // reset
                Thread.sleep(1000)
                sendCommand("ATE0")  // echo off
                Thread.sleep(200)
                sendCommand("ATL0")  // linefeed off
                Thread.sleep(200)

                startRPMPolling()

                onMessage("Connected to ${device.name}")

            } catch (e: IOException) {
                Log.e("BT", "Connection Error", e)
                onMessage("Could not connect to ${device.name}")
            }
        }.start()
    }

    private fun sendCommand(cmd: String) {
        try {
            writer?.apply {
                write(cmd + "\r")
                flush()
            }
        } catch (e: IOException) {
            Log.e("ELM", "Command send error", e)
        }
    }

    private fun startRPMPolling() {
        Thread {
            while (socket?.isConnected == true) {
                sendCommand("010C") // request RPM
                val response = readResponse()
                val rpm = parseRPM(response)
                onRPMUpdated(rpm)
                Thread.sleep(1000)
            }
        }.start()
    }

    private fun readResponse(): String {
        val sb = StringBuilder()
        try {
            while (reader?.ready() == true) {
                sb.append(reader!!.readLine())
            }
        } catch (e: IOException) {
            Log.e("ELM", "Read error", e)
        }
        return sb.toString()
    }

    private fun parseRPM(response: String): Int {
        return try {
            val parts = response.trim().split(" ")
            if (parts.size >= 4) {
                val A = parts[2].toInt(16)
                val B = parts[3].toInt(16)
                ((A * 256 + B) / 4)
            } else 0
        } catch (e: Exception) {
            0
        }
    }

    fun cleanup() {
        try { context.unregisterReceiver(receiver) } catch (_: Exception) {}
        try { context.unregisterReceiver(btChangeReceiver) } catch (_: Exception) {}
        try { socket?.close() } catch (_: Exception) {}
    }
}
