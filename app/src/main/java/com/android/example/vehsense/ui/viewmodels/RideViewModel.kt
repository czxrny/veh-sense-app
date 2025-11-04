package com.android.example.vehsense.ui.viewmodels

import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.vehsense.bluetooth.ELMPoller
import com.android.example.vehsense.model.ObdFrame
import com.android.example.vehsense.network.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RideViewModel(
    private val sessionManager: SessionManager,
    private val btSocket: BluetoothSocket
): ViewModel() {
    private val _obdFrame = MutableStateFlow(ObdFrame())
    val obdFrame: StateFlow<ObdFrame> = _obdFrame.asStateFlow()

    private val elmPoller: ELMPoller = ELMPoller(
        onFrameUpdate = { _obdFrame.value = it
                        Log.d("OBDDATA", it.toString()) } ,
        btSocket
    )

    private var pollJob: Job? = null

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

    fun pollData() {
        pollJob?.cancel()

        pollJob = viewModelScope.launch {
            elmPoller.pollDevice()
        }
    }

    fun stopPolling() {
        if (pollJob?.isActive == true) {
            Log.d("RideViewModel", "Cancelling OBD poll job...")
            pollJob?.cancel()
        } else {
            Log.d("RideViewModel", "pollJob already inactive or null")
        }
        pollJob = null
    }

    fun sendDataToBackend() {

    }
}