package com.android.example.vehsense.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.vehsense.model.Vehicle
import com.android.example.vehsense.network.BackendCommunicator
import com.android.example.vehsense.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VehicleViewModel(
    private val sessionManager: SessionManager
): ViewModel() {
    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    val isPrivate: Boolean = requireNotNull(sessionManager.getPrivateStatus())

    private val communicator: BackendCommunicator = BackendCommunicator()

    fun getVehicles() {
        viewModelScope.launch {
            try {
                val token = sessionManager.getToken()
                if (token == null) {
                    _errorMessage.value = "Could not authorize"
                    return@launch
                }

                val response = communicator.getVehicles(token)
                response.onSuccess { res ->
                    _vehicles.value = res
                }.onFailure { e ->
                    _errorMessage.value = e.message ?: "Unknown error"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
            }
        }
    }

    fun deleteVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            try {
                val token = sessionManager.getToken()
                if (token == null) {
                    _errorMessage.value = "Could not authorize"
                    return@launch
                }

                val response = communicator.deleteVehicle(vehicle.id, token)
                response.onSuccess {
                    _vehicles.value -= vehicle
                }.onFailure { e ->
                    _errorMessage.value = e.message ?: "Unknown error"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
            }
        }
    }
}