package com.android.example.vehsense.ui.viewmodels

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.vehsense.bluetooth.BluetoothScanner
import com.android.example.vehsense.bluetooth.ELMCommander
import com.android.example.vehsense.storage.BluetoothStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

class DashboardBTViewModel(application: Application) : AndroidViewModel(application) {
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val storage = BluetoothStorage(getApplication<Application>())

    private val _btIsOn = MutableStateFlow(false)
    val btIsOn: StateFlow<Boolean> = _btIsOn.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    private val _socket = MutableStateFlow<BluetoothSocket?>(null)
    val socket: StateFlow<BluetoothSocket?> = _socket.asStateFlow()

    fun updateSocket(bluetoothDevice: BluetoothDevice) {
        val socket = bluetoothDevice.createRfcommSocketToServiceRecord(SPP_UUID)
        viewModelScope.launch {
            try {
                socket?.connect()
                val elmCommander = ELMCommander(socket)
                val valid = elmCommander.isELM()
                if (!valid) {
                    return@launch
                }

                elmCommander.runConfig()
            } catch (e: IOException) {
                Log.d("SocketUpdate", "Error while connecting to a socket: ", e)
            }
        }
    }

    fun saveDeviceAddress(address: String) { storage.saveDeviceAddress(address) }
    fun getBtSocket(): BluetoothSocket? { return _socket.value }

    private fun updateSocketByAddress() {
        val address = storage.getSavedDeviceAddress()
        if (address != null) {
            val btScanner = BluetoothScanner(getApplication(), onDevicesUpdated = {})
            val device = btScanner.getDeviceByAddress(address)
            if (device != null) {
                updateSocket(device)
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
                        _socket.value = null }
                }
            }
        }
    }

    init {
        val stateChangingFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        getApplication<Application>().registerReceiver(bluetoothStateReceiver, stateChangingFilter)

        updateSocketByAddress()

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        _btIsOn.value = bluetoothAdapter?.isEnabled == true
    }

    override fun onCleared() {
        super.onCleared()
        _socket.value?.close()
        getApplication<Application>().unregisterReceiver(bluetoothStateReceiver)
    }
}