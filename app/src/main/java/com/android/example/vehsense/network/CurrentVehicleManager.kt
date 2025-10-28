package com.android.example.vehsense.network

import android.util.Log
import com.android.example.vehsense.model.Vehicle
import com.android.example.vehsense.storage.VehicleStorage

class CurrentVehicleManager(
    private val sessionManager: SessionManager,
    private val backendCommunicator: BackendCommunicator,
    private val vehicleStorage: VehicleStorage
) {
    private var vehicle: Vehicle? = null

    private var vehicleId: Int?
        get() = vehicleStorage.getVehicleId()
        set(value) {
            if (value != null) vehicleStorage.saveVehicleId(value)
            else vehicleStorage.clearVehicleId()
        }

    // Get the Vehicle - if run for the first time of current app life (and vehicle was previously selected) - fetch from API
    suspend fun getVehicle(): Vehicle? {
        if(vehicle != null){
            return vehicle
        }

        vehicleId?.let {
            fetchFromBackend(it)
            return vehicle
        }

        Log.d("CurrentVehicleManager", "Error: no vehicle id in storage to fetch - returning null")
        return null
    }

    fun setNewVehicle(newVehicle: Vehicle) {
        vehicle = newVehicle
        vehicleId = newVehicle.id
    }

    private suspend fun fetchFromBackend(id: Int): Boolean {
        return try {
            val token = sessionManager.getToken()

            if (token != null) {
                vehicle = backendCommunicator.getVehicleById(token, id).getOrThrow()
                return true
            } else {
                return false
            }
        } catch (e: Exception) {
            Log.d("CurrentVehicleManager", "Error while fetching the vehicle: $e")
            false
        }
    }

    fun clear() {
        vehicleId = null
        vehicle = null
    }
}