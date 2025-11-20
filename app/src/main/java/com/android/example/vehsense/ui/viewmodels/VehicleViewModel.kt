package com.android.example.vehsense.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.vehsense.model.Vehicle
import com.android.example.vehsense.network.BackendCommunicator
import com.android.example.vehsense.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VehicleViewModel(
    private val sessionManager: SessionManager
): ViewModel() {
    sealed class VehiclesState {
        data object Loading : VehiclesState()
        data class Success(
            val vehicles: List<Vehicle>
        ) : VehiclesState()
        data class Error(val message: String) : VehiclesState()
    }

    private val _vehiclesInfo = MutableStateFlow<VehiclesState>(VehiclesState.Loading)
    val vehiclesInfo: StateFlow<VehiclesState> = _vehiclesInfo

    val isPrivate: Boolean = requireNotNull(sessionManager.getPrivateStatus())

    private val communicator: BackendCommunicator = BackendCommunicator()

    fun getVehicles() {
        _vehiclesInfo.value = VehiclesState.Loading
        viewModelScope.launch {
            try {
                val token = sessionManager.getToken()
                if (token == null) {
                    _vehiclesInfo.value = VehiclesState.Error("Could not authorize")
                    return@launch
                }

                val response = communicator.getVehicles(token)
                response.onSuccess { res ->
                    _vehiclesInfo.value = VehiclesState.Success(res)
                }.onFailure { e ->
                    _vehiclesInfo.value = VehiclesState.Error(e.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _vehiclesInfo.value = VehiclesState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun deleteVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            try {
                val token = sessionManager.getToken()
                if (token == null) {
                    _vehiclesInfo.value = VehiclesState.Error("Could not authorize")
                    return@launch
                }

                val response = communicator.deleteVehicle(vehicle.id, token)
                response.onSuccess {
                    _vehiclesInfo.value = VehiclesState.Success(
                            (vehiclesInfo.value as? VehiclesState.Success)?.vehicles.orEmpty()
                                .filterNot { it.id == vehicle.id }
                            )
                }.onFailure { e ->
                    _vehiclesInfo.value = VehiclesState.Error(e.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _vehiclesInfo.value = VehiclesState.Error(e.message ?: "Unknown error")
            }
        }
    }
}