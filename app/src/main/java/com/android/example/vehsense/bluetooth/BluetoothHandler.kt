package com.android.example.vehsense.bluetooth

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
import com.android.example.vehsense.model.ObdFrame
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
    private val onFrameUpdate: (ObdFrame) -> Unit,
) {
    private val REQUEST_CODE_BT = 1001
    // Standard UUID For Bluetooth Serial Port Profile
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

    private fun getMissingPermissions(): List<String> {
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

    fun hasPermissions(): Boolean {
        return getMissingPermissions().isEmpty()
    }

    @Suppress("MissingPermission")
    fun startDiscovery() {
        if (bluetoothAdapter == null) {
            onMessage("Bluetooth not available")
            return
        }

        val findingFilter= IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(receiver, findingFilter)

        bluetoothAdapter?.startDiscovery()
    }

    @Suppress("MissingPermission")
    fun connectToDeviceByAddress(macAddress: String): BluetoothSocket? {
        val device: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(macAddress)
        if (device != null) {
            return getBtSocket(device)
        }
        return null
    }

    @Suppress("MissingPermission")
    fun getBtSocket(device: BluetoothDevice): BluetoothSocket? {
        Thread {
            try {
                socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                bluetoothAdapter?.cancelDiscovery()
                socket?.connect()

                writer = BufferedWriter(OutputStreamWriter(socket!!.outputStream))
                reader = BufferedReader(InputStreamReader(socket!!.inputStream))

                sendCommand("ATI")
                val isELM = readResponse().contains("ELM", ignoreCase = true)
                if (isELM) {
                    for (cfg in ObdConfig.entries) {
                        sendCommand(cfg.command)
                        Thread.sleep(500)
                        readResponse()
                    }
                } else {
                    throw IllegalStateException("Not an ELM327 Device")
                }
            } catch (e: IOException) {
                Log.e("BT", "Connection Error", e)
                onMessage("Could not connect to ${device.name}")
            }
        }.start()
        return socket
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

    private fun startObdPolling() {
        Thread {
            while (socket?.isConnected == true) {
                val obdValues: MutableMap<String, Int> = mutableMapOf()
                for (command in ObdCommand.entries) {
                    sendCommand(command.code)
                    val rawResponse = readResponse()
                    val response = command.parse(rawResponse)
                    obdValues[command.name] = response
                    Thread.sleep(200)
                }
                Log.d("OBD", "OBD FRAME:\n" +
                        obdValues.toString() )
                onFrameUpdate(ObdFrame(obdValues))
            }
        }.start()
    }

    fun cleanup() {
        try { context.unregisterReceiver(receiver) } catch (_: Exception) {}
        try { socket?.close() } catch (_: Exception) {}
    }
}
