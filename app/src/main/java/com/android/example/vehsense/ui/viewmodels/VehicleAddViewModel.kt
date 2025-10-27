package com.android.example.vehsense.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.vehsense.model.NewVehicleForm
import com.android.example.vehsense.model.VehicleAddRequest
import com.android.example.vehsense.network.BackendCommunicator
import com.android.example.vehsense.network.SessionManager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VehicleAddViewModel(
    private val sessionManager: SessionManager,
    private val communicator: BackendCommunicator = BackendCommunicator(),
): ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun addVehicle(
        form: NewVehicleForm,
        onSuccess: () -> Unit
    ) {
        fun String.capitalizeFirst(): String {
            return this.lowercase().replaceFirstChar { it.uppercase() }
        }

        viewModelScope.launch {
            try {
                val formattedBrand = form.brand.capitalizeFirst()
                val formattedModel = form.model.capitalizeFirst()

                if (formattedBrand.isEmpty()) {
                    _errorMessage.value = "Brand must not be empty"
                    return@launch
                }

                if (formattedModel.isEmpty()) {
                    _errorMessage.value = "Model must not be empty"
                    return@launch
                }

                val yearInt = form.year.toIntOrNull()
                val engineCapacityInt = form.engineCapacity.toIntOrNull()
                val enginePowerInt = form.enginePower.toIntOrNull()
                val expectedFuelDouble = form.expectedFuel.toDoubleOrNull()

                if (yearInt == null || engineCapacityInt == null || enginePowerInt == null || expectedFuelDouble == null) {
                    _errorMessage.value = "Invalid number format"
                    return@launch
                }

                val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

                if (yearInt !in 1997..currentYear) {
                    _errorMessage.value = "Year must be between 1997 and $currentYear"
                    return@launch
                }

                if (engineCapacityInt !in 500..8000) {
                    _errorMessage.value = "Engine capacity must be between 500cc and 8000cc"
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

                val request = VehicleAddRequest(
                    brand = formattedBrand,
                    model = formattedModel,
                    year = yearInt,
                    engineCapacity = engineCapacityInt,
                    enginePower = enginePowerInt,
                    plates = form.plates.ifBlank { null },
                    expectedFuel = expectedFuelDouble
                )

                val token = sessionManager.getToken()
                if (token == null) {
                    _errorMessage.value = "Could not authorize"
                    return@launch
                }

                val response = communicator.addVehicle(request, token)

                if (response.isSuccess) {
                    onSuccess()
                } else {
                    _errorMessage.value = response.exceptionOrNull()?.message ?: "Unknown error"
                }

            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
            }
        }
    }

}

