package com.android.example.vehsense.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.vehsense.model.VehicleUpdateForm
import com.android.example.vehsense.model.VehicleUpdateRequest
import com.android.example.vehsense.network.BackendCommunicator
import com.android.example.vehsense.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VehicleUpdateViewModel (
    private val sessionManager: SessionManager,
    private val communicator: BackendCommunicator = BackendCommunicator(),
): ViewModel() {
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isSuccess = MutableStateFlow<Boolean?>(null)
    val isSuccess: StateFlow<Boolean?> = _isSuccess

    fun updateVehicle(
        form: VehicleUpdateForm,
        id: Int
    ) {
        viewModelScope.launch {
            try {
                val enginePowerInt = form.enginePower.toIntOrNull()
                val expectedFuelDouble = form.expectedFuel.toDoubleOrNull()

                if (enginePowerInt == null || expectedFuelDouble == null) {
                    _errorMessage.value = "Invalid number format"
                    return@launch
                }

                if (enginePowerInt <= 0) {
                    _errorMessage.value = "Engine power must be greater than 0"
                    return@launch
                }

                if (expectedFuelDouble <= 0.0) {
                    _errorMessage.value = "Expected fuel consumption must be greater than 0"
                    return@launch
                }

                val request = VehicleUpdateRequest(
                    enginePower = enginePowerInt,
                    plates = form.plates?.ifBlank { null },
                    expectedFuel = expectedFuelDouble
                )

                val token = sessionManager.getToken()
                if (token == null) {
                    _errorMessage.value = "Could not authorize"
                    return@launch
                }

                val response = communicator.updateVehicle(request, id, token)

                if (response.isSuccess) {
                    _isSuccess.value = true
                } else {
                    _errorMessage.value = response.exceptionOrNull()?.message ?: "Unknown error"
                    _isSuccess.value = false
                }

            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
                _isSuccess.value = false
            }
        }
    }
}