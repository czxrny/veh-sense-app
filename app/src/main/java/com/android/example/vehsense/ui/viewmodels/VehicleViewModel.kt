package com.android.example.vehsense.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.vehsense.model.Vehicle
import com.android.example.vehsense.network.BackendCommunicator
import com.android.example.vehsense.repository.BackendRepository
import com.android.example.vehsense.repository.BackendRepository.token
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VehicleViewModel(
    private val userId: Int,
    private var token: String
): ViewModel() {
    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val communicator: BackendCommunicator = BackendCommunicator()

    fun getVehicles() {
        viewModelScope.launch {
            try {
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
        token = BackendRepository.token.toString()
        viewModelScope.launch {
            try {
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