package com.android.example.vehsense.ui.viewmodels

import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.vehsense.bluetooth.ELMPoller
import com.android.example.vehsense.local.ObdFrameDao
import com.android.example.vehsense.local.ObdFrameEntity
import com.android.example.vehsense.model.ObdFrame
import com.android.example.vehsense.model.toEntity
import com.android.example.vehsense.network.BackendCommunicator
import com.android.example.vehsense.network.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class RideViewModel(
    private val vehicleId: Int,
    private val sessionManager: SessionManager,
    private val communicator: BackendCommunicator,
    private val obdFrameDao: ObdFrameDao,
    private val btSocket: BluetoothSocket
): ViewModel() {
    private val _uploadFailed = MutableStateFlow(false)
    val uploadFailed = _uploadFailed.asStateFlow()

    private val frameBuffer = mutableListOf<ObdFrameEntity>()

    private val _obdFrame = MutableStateFlow(ObdFrame())
    val obdFrame: StateFlow<ObdFrame> = _obdFrame.asStateFlow()

    private val _connectionWasInterrupted = MutableStateFlow(false)
    val connectionWasInterrupted = _connectionWasInterrupted.asStateFlow()

    private var uploadAttempted = false

    private val elmPoller: ELMPoller = ELMPoller(
        onFrameUpdate = { it ->
            _obdFrame.value = it
            frameBuffer.add(it.toEntity())
            if (frameBuffer.size >= 20) {
                val batch = frameBuffer.toList()
                frameBuffer.clear()
                viewModelScope.launch(Dispatchers.IO) {
                    obdFrameDao.insertAll(batch.map { it })
                }
            }
            Log.d("OBDDATA", it.toString())},
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
                    _connectionWasInterrupted.value = true
                }
            }
        }
    }

    fun pollData() {
        pollJob?.cancel()

        pollJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                elmPoller.pollDevice()
            } catch (e: IOException) {
                Log.d("RideViewModel", "Device connection was interrupted: $e")
                _connectionWasInterrupted.value = true
            } catch (e: Exception) {
                Log.d("RideViewModel", "An issue occurred: $e")
                _connectionWasInterrupted.value = true
            } finally {
                Log.d("RideViewModel", "Device polling was stopped.")
            }
        }
    }

    fun stopPolling() {
        viewModelScope.launch(Dispatchers.IO) {
            if (pollJob?.isActive == true) {
                Log.d("RideViewModel", "Cancelling OBD poll job...")
                pollJob?.cancel()
            }
            pollJob = null
            sendDataToBackend()
        }
    }

    private suspend fun sendDataToBackend(): Boolean {
        if (!uploadAttempted) {
            uploadAttempted = true
            val frameList = obdFrameDao.getAll()

            val token = sessionManager.getToken()
                ?: throw IllegalStateException("No auth token")

            val response = communicator.sendRideData(vehicleId, frameList, token)

            return if (response.isSuccess) {
                obdFrameDao.deleteAll()
                true
            } else {
                Log.d("RideViewModel", "Upload failed: $response")
                _uploadFailed.value = true
                false
            }
        } else {
            return false
        }
    }

    suspend fun tryUploadIfNeeded() {
        if (obdFrameDao.count() > 0) {
            sendDataToBackend()
        }
    }
}