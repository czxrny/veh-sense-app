package com.android.example.vehsense.ui.viewmodels.utils

import android.bluetooth.BluetoothSocket
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.example.vehsense.local.ObdFrameDao
import com.android.example.vehsense.network.BackendCommunicator
import com.android.example.vehsense.network.SessionManager
import com.android.example.vehsense.ui.viewmodels.RideViewModel

class RideViewModelFactory(
    private val vehicleId: Int,
    private val sessionManager: SessionManager,
    private val communicator: BackendCommunicator,
    private val obdFrameDao: ObdFrameDao,
    private val btSocket: BluetoothSocket
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RideViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RideViewModel(
                vehicleId = vehicleId,
                sessionManager = sessionManager,
                communicator = communicator,
                obdFrameDao = obdFrameDao,
                btSocket = btSocket
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
