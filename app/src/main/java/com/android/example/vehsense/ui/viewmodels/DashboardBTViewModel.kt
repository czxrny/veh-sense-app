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
import com.android.example.vehsense.model.DeviceInfo
import com.android.example.vehsense.storage.BluetoothStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

class DashboardBTViewModel(application: Application) : AndroidViewModel(application) {
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val _btIsOn = MutableStateFlow(false)
    val btIsOn: StateFlow<Boolean> = _btIsOn.asStateFlow()

    private val _socket = MutableStateFlow<BluetoothSocket?>(null)
    val socket: StateFlow<BluetoothSocket?> = _socket.asStateFlow()

    private val _deviceInfo = MutableStateFlow<DeviceInfo?>(null)
    val deviceInfo: StateFlow<DeviceInfo?> = _deviceInfo.asStateFlow()

    private var currentDeviceInfo: DeviceInfo? = null

    private val _isConnected = MutableStateFlow<Boolean?>(null)
    val isConnected: StateFlow<Boolean?> = _isConnected.asStateFlow()

    fun updateSocket(bluetoothDevice: BluetoothDevice) {
        viewModelScope.launch {
            _isConnected.value = null

            try {
                withContext(Dispatchers.IO) {
                    val socket = bluetoothDevice.createRfcommSocketToServiceRecord(SPP_UUID)
                        ?: throw Exception("Unable to create socket")

                    socket.connect()

                    if (!socket.isConnected) {
                        throw Exception("Unable to connect")
                    }

                    val elmCommander = ELMCommander(socket)
                    val valid = elmCommander.isELM()
                    if (!valid) throw Exception("Not a valid ELM device")

                    elmCommander.runConfig()
                    _socket.value = socket
                }

                _isConnected.value = true
                Log.d("SocketUpdate", "Connected to ${bluetoothDevice.name}")
            } catch (e: Exception) {
                Log.d("SocketUpdate", "Error while connecting to a socket: ", e)
                _isConnected.value = false
            }
        }
    }

    fun updateDeviceInfo(deviceInfo: DeviceInfo) {
        _deviceInfo.value = deviceInfo
        BluetoothStorage.saveDeviceInfo(deviceInfo)
    }

    fun getBtSocket(): BluetoothSocket? { return _socket.value }

    fun updateSocketByAddress() {
        val deviceInfo = BluetoothStorage.getSavedDeviceInfo()
        Log.d("SocketUpdate", "Connecting to socket by address")
        if (deviceInfo != null) {
            _deviceInfo.value = deviceInfo
            Log.d("SocketUpdate", "Address: ${deviceInfo.address}")
            Log.d("SocketUpdate", "Name: ${deviceInfo.name}")
            val btScanner = BluetoothScanner(getApplication(), onDevicesUpdated = {})
            val device = btScanner.getDeviceByAddress(deviceInfo.address)
            if (device != null) {
                updateSocket(device)
            }
        } else {
            _isConnected.value = false
            Log.d("SocketUpdate", "No bluetooth device saved in storage")
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
                        _socket.value = null
                        _isConnected.value = false
                    }
                }
            }
        }
    }

    init {
        val stateChangingFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        getApplication<Application>().registerReceiver(bluetoothStateReceiver, stateChangingFilter)

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        _btIsOn.value = bluetoothAdapter?.isEnabled == true
    }

    override fun onCleared() {
        super.onCleared()
        _socket.value?.close()
        getApplication<Application>().unregisterReceiver(bluetoothStateReceiver)
    }
}