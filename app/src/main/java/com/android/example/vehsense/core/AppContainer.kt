package com.android.example.vehsense.core

import android.content.Context
import com.android.example.vehsense.network.BackendCommunicator
import com.android.example.vehsense.network.CurrentVehicleManager
import com.android.example.vehsense.network.SessionManager
import com.android.example.vehsense.storage.UserStorage
import com.android.example.vehsense.storage.VehicleStorage

// Singleton containing all of the necessary objects for the backend communication part of the app
object AppContainer {
    lateinit var userStorage: UserStorage
    lateinit var vehicleStorage: VehicleStorage
    lateinit var backend: BackendCommunicator
    lateinit var currentVehicleManager: CurrentVehicleManager
    lateinit var sessionManager: SessionManager

    fun init(context: Context) {
        userStorage = UserStorage(context)
        vehicleStorage = VehicleStorage(context)
        backend = BackendCommunicator()
        sessionManager = SessionManager(backend, userStorage)
        currentVehicleManager = CurrentVehicleManager(sessionManager, backend, vehicleStorage)
    }
}