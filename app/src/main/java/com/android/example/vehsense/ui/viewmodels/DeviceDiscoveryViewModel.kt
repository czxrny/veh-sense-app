package com.android.example.vehsense.ui.viewmodels

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.vehsense.bluetooth.BluetoothScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeviceDiscoveryViewModel(application: Application) : AndroidViewModel(application) {
    private val _devices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val devices: StateFlow<List<BluetoothDevice>> = _devices.asStateFlow()

    private val btScanner: BluetoothScanner = BluetoothScanner(
        getApplication<Application>(),
        onDevicesUpdated = { _devices.value = it }
    )

    fun startDiscovery() {
        viewModelScope.launch {
            btScanner.startDiscovery()
        }
    }

    fun stopDiscovery() {
        btScanner.stopDiscovery()
    }
}