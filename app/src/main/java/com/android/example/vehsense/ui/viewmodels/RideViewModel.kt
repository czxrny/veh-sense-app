package com.android.example.vehsense.ui.viewmodels

import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.vehsense.model.ObdFrame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RideViewModel(
    private val userId: Int,
    private var token: String,
    private val btSocket: BluetoothSocket
): ViewModel() {
    private val _obdFrame = MutableStateFlow(ObdFrame())
    val obdFrame: StateFlow<ObdFrame> = _obdFrame.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (!btSocket.isConnected) {
                try {
                    btSocket.connect()
                } catch (e: Exception) {
                    Log.e("RideViewModel", "Connection failed: ${e.message}")
                }
            }
        }
    }

    suspend fun pollData() {

    }

    suspend fun sendDataToBackend() {

    }
}