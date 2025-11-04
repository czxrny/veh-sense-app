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
import com.android.example.vehsense.bluetooth.ELMCommander
import com.android.example.vehsense.model.DeviceInfo
import com.android.example.vehsense.model.Vehicle
import com.android.example.vehsense.network.CurrentVehicleManager
import com.android.example.vehsense.storage.BluetoothStorage
import com.android.example.vehsense.storage.VehicleStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

class MainViewModel(
    application: Application,
    private val currentVehicleManager: CurrentVehicleManager
) : AndroidViewModel(application) {
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val _btIsOn = MutableStateFlow(false)
    val btIsOn: StateFlow<Boolean> = _btIsOn.asStateFlow()

    private val _socket = MutableStateFlow<BluetoothSocket?>(null)
    val socket: StateFlow<BluetoothSocket?> = _socket.asStateFlow()

    private val _deviceInfo = MutableStateFlow<DeviceInfo?>(null)
    val deviceInfo: StateFlow<DeviceInfo?> = _deviceInfo.asStateFlow()

    private val _isConnected = MutableStateFlow<Boolean?>(false)
    val isConnected: StateFlow<Boolean?> = _isConnected.asStateFlow()

    private val _selectedVehicle = MutableStateFlow<Vehicle?>(null)
    val selectedVehicle: StateFlow<Vehicle?> = _selectedVehicle.asStateFlow()

    fun disconnectFromDevice() {
        val socket = _socket.value
        try {
            socket?.close()
        } catch (e: IOException) {
            Log.d("VehsenseBTSocket", "Error while closing socket: ${e.message}")
        } finally {
            Log.d("VehsenseBTSocket", "Disconnected from device")
            _socket.value = null
            _isConnected.value = false
        }
    }

    fun connectToDevice(bluetoothDevice: BluetoothDevice) {
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

                    val elmCommander = ELMCommander(socket = socket)
                    val valid = elmCommander.isELM()
                    if (!valid) throw Exception("Not a valid ELM device")

                    _socket.value = socket
                }

                _isConnected.value = true
                Log.d("VehsenseBTSocket", "Connected to ${bluetoothDevice.name}")
            } catch (e: Exception) {
                Log.d("VehsenseBTSocket", "Error while connecting to a socket: ", e)
                _socket.value = null
                _isConnected.value = false
            }
        }
    }

    fun updateDeviceInfo(deviceInfo: DeviceInfo) {
        _deviceInfo.value = deviceInfo
        BluetoothStorage.saveDeviceInfo(deviceInfo)
    }

    fun setCurrentVehicle(vehicle: Vehicle?) {
        _selectedVehicle.value = vehicle

        when(vehicle){
            null -> currentVehicleManager.clear()
            else -> currentVehicleManager.setNewVehicle(vehicle)
        }
    }

    private fun loadCurrentVehicle() {
        viewModelScope.launch {
            val vehicle = currentVehicleManager.getVehicle()
            _selectedVehicle.value = vehicle
        }
    }

    fun connectToSavedDevice() {
        val deviceInfo = BluetoothStorage.getSavedDeviceInfo()
        Log.d("VehsenseBTSocket", "Connecting to socket by address")
        if (deviceInfo != null) {
            _deviceInfo.value = deviceInfo
            Log.d("VehsenseBTSocket", "Address: ${deviceInfo.address}")
            Log.d("VehsenseBTSocket", "Name: ${deviceInfo.name}")
            val btAdapter = BluetoothAdapter.getDefaultAdapter()
            val device = btAdapter.getRemoteDevice(deviceInfo.address)
            if (device != null) {
                connectToDevice(device)
            }
        } else {
            _isConnected.value = false
            Log.d("VehsenseBTSocket", "No bluetooth device saved in storage")
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
                        connectToSavedDevice()
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

        _deviceInfo.value = BluetoothStorage.getSavedDeviceInfo()
        loadCurrentVehicle()

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        _btIsOn.value = bluetoothAdapter?.isEnabled == true
    }

    override fun onCleared() {
        super.onCleared()
        _socket.value?.close()
        getApplication<Application>().unregisterReceiver(bluetoothStateReceiver)
    }
}