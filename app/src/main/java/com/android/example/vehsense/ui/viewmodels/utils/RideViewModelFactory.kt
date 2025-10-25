package com.android.example.vehsense.ui.viewmodels.utils

import android.bluetooth.BluetoothSocket
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.example.vehsense.network.SessionManager
import com.android.example.vehsense.ui.viewmodels.RideViewModel

class RideViewModelFactory(
    private val sessionManager: SessionManager,
    private val btSocket: BluetoothSocket
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RideViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RideViewModel(sessionManager, btSocket) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
