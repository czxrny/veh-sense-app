package com.android.example.vehsense.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.vehsense.model.VehicleAddRequest
import com.android.example.vehsense.network.BackendCommunicator

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VehicleAddViewModel(
    private val userId: Int,
    private val token: String,
    private val communicator: BackendCommunicator = BackendCommunicator(),
): ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun addVehicle(
        brand: String,
        model: String,
        year: String,
        engineCapacity: String,
        enginePower: String,
        plates: String?,
        expectedFuel: String,
        onSuccess: () -> Unit
    ) {
        fun String.capitalizeFirst(): String {
            return this.lowercase().replaceFirstChar { it.uppercase() }
        }

        viewModelScope.launch {
            try {
                val formattedBrand = brand.capitalizeFirst()
                val formattedModel = model.capitalizeFirst()

                if (formattedBrand.isEmpty()) {
                    _errorMessage.value = "Brand must not be empty"
                    return@launch
                }

                if (formattedModel.isEmpty()) {
                    _errorMessage.value = "Model must not be empty"
                    return@launch
                }

                val yearInt = year.toIntOrNull()
                val engineCapacityInt = engineCapacity.toIntOrNull()
                val enginePowerInt = enginePower.toIntOrNull()
                val expectedFuelDouble = expectedFuel.toDoubleOrNull()

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
                    plates = plates?.ifBlank { null },
                    expectedFuel = expectedFuelDouble
                )

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

