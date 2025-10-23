package com.android.example.vehsense.ui.viewmodels

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeviceDiscoveryViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>()

    private val _devices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val devices: StateFlow<List<BluetoothDevice>> = _devices.asStateFlow()

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    private val receiver = object : BroadcastReceiver() {
        @Suppress("MissingPermission")
        override fun onReceive(ctx: Context, intent: Intent) {
            if (BluetoothDevice.ACTION_FOUND == intent.action) {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    if (!_devices.value.contains(it)) {
                        _devices.value += it
                    }
                }
            }
        }
    }

    fun startDiscovery() {
        if (bluetoothAdapter == null) {
            return
        }

        val findingFilter= IntentFilter(BluetoothDevice.ACTION_FOUND)
        getApplication<Application>().registerReceiver(receiver, findingFilter)

        viewModelScope.launch {
            bluetoothAdapter?.startDiscovery()
        }
    }

    fun stopDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
        try { context.unregisterReceiver(receiver) } catch (_: Exception) {}
    }
}