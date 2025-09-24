package com.android.example.vehsense

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.*

class MainActivity : Activity() {
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    private lateinit var container: LinearLayout
    private lateinit var rpmTextView: TextView

    private val REQUEST_CODE_BT = 1001
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var socket: BluetoothSocket? = null
    private var reader: BufferedReader? = null
    private var writer: BufferedWriter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Simple UI
        container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        rpmTextView = TextView(this).apply {
            textSize = 24f
            text = "Select ELM327"
        }

        container.addView(rpmTextView)

        val scroll = ScrollView(this)
        scroll.addView(container)
        setContentView(scroll)

        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), REQUEST_CODE_BT)
        } else {
            startDiscovery()
        }
    }

    @Suppress("MissingPermission")
    private fun startDiscovery() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Please enable Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
        bluetoothAdapter?.startDiscovery()
    }

    private val receiver = object : BroadcastReceiver() {
        @Suppress("MissingPermission")
        override fun onReceive(context: Context, intent: android.content.Intent) {
            if (BluetoothDevice.ACTION_FOUND == intent.action) {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let { addDeviceButton(it) }
            }
        }
    }

    @Suppress("MissingPermission")
    private fun addDeviceButton(device: BluetoothDevice) {
        val button = Button(this).apply {
            text = "${device.name ?: "Unknown"}\n${device.address}"
            setOnClickListener { connectToDevice(device) }
        }
        container.addView(button)
    }

    @Suppress("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        Toast.makeText(this, "Connecting to ${device.name}", Toast.LENGTH_SHORT).show()

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

                runOnUiThread {
                    Toast.makeText(this, "Connected to ${device.name}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: IOException) {
                Log.e("BT", "Connection Error", e)
                runOnUiThread {
                    Toast.makeText(this, "Could not connect to selected device", Toast.LENGTH_SHORT).show()
                }
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
                runOnUiThread {
                    rpmTextView.text = "RPM: $rpm"
                }
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
        // Example: "41 0C 1A F8"
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_BT) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startDiscovery()
            } else {
                Toast.makeText(this, "Insufficient permissions", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(receiver) } catch (_: Exception) {}
        try { socket?.close() } catch (_: Exception) {}
    }
}
