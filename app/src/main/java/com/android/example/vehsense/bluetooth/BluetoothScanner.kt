package com.android.example.vehsense.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class BluetoothScanner(
    private val context: Context,
    private val onDevicesUpdated: (List<BluetoothDevice>) -> Unit,
) {
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

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

    @Suppress("MissingPermission")
    fun startDiscovery() {
        if (bluetoothAdapter == null) {
            return
        }

        val findingFilter= IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(receiver, findingFilter)

        bluetoothAdapter?.startDiscovery()
    }

    @Suppress("MissingPermission")
    fun stopDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
    }

    fun getDeviceByAddress(macAddress: String): BluetoothDevice? {
        return bluetoothAdapter?.getRemoteDevice(macAddress)
    }

    fun cleanup() {
        try { context.unregisterReceiver(receiver) } catch (_: Exception) {}
    }
}
