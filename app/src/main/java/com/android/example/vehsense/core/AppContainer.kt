package com.android.example.vehsense.core

import android.content.Context
import androidx.room.Room
import com.android.example.vehsense.local.AppDatabase
import com.android.example.vehsense.local.ObdFrameDao
import com.android.example.vehsense.network.BackendCommunicator
import com.android.example.vehsense.network.CurrentVehicleManager
import com.android.example.vehsense.network.SessionManager
import com.android.example.vehsense.storage.UserStorage
import com.android.example.vehsense.storage.VehicleStorage

object AppContainer {
    lateinit var userStorage: UserStorage
    lateinit var vehicleStorage: VehicleStorage
    lateinit var backend: BackendCommunicator
    lateinit var currentVehicleManager: CurrentVehicleManager
    lateinit var sessionManager: SessionManager

    lateinit var database: AppDatabase
    lateinit var obdFrameDao: ObdFrameDao

    fun init(context: Context) {
        userStorage = UserStorage(context)
        vehicleStorage = VehicleStorage(context)
        backend = BackendCommunicator()
        sessionManager = SessionManager(backend, userStorage)
        currentVehicleManager = CurrentVehicleManager(sessionManager, backend, vehicleStorage)

        database = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "veh-sense-db"
        ).build()
        obdFrameDao = database.obdFrameDao()
    }
}
