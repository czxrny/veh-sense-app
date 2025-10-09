package com.android.example.vehsense.ui.viewmodels

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.AndroidViewModel
import com.android.example.vehsense.bluetooth.BluetoothHandler
import com.android.example.vehsense.storage.BluetoothStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DashboardBTViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = BluetoothStorage(getApplication<Application>())
    private val btHandler = BluetoothHandler(context = getApplication<Application>(), onFrameUpdate = {}, onDevicesUpdated =  {}, onMessage = {} )

    private val _btIsOn = MutableStateFlow(false)
    val btIsOn: StateFlow<Boolean> = _btIsOn.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    private val _socket = MutableStateFlow<BluetoothSocket?>(null)
    val socket: StateFlow<BluetoothSocket?> = _socket.asStateFlow()

    fun updateSocket(newSocket: BluetoothSocket?) { _socket.value = newSocket }
    fun updatePermissionState(hasPerm: Boolean) { _hasPermission.value = hasPerm }
    fun saveDeviceAddress(address: String) { storage.saveDeviceAddress(address) }

    private fun updateSocketByAddress() {
        val address = storage.getSavedDeviceAddress()
        if (address != null) {
            val newSocket = btHandler.connectToDeviceByAddress(address)
            if (newSocket != null) {
                updateSocket(newSocket)
            }
        }
    }

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        @Suppress("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_ON -> {
                        _btIsOn.value = true
                        updateSocketByAddress()
                    }
                    BluetoothAdapter.STATE_OFF -> {
                        _btIsOn.value = false
                        updateSocket(null) }
                }
            }
        }
    }

    init {
        val stateChangingFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        getApplication<Application>().registerReceiver(bluetoothStateReceiver, stateChangingFilter)

        updatePermissionState(btHandler.hasPermissions())

        updateSocketByAddress()

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        _btIsOn.value = bluetoothAdapter?.isEnabled == true
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unregisterReceiver(bluetoothStateReceiver)
    }
}