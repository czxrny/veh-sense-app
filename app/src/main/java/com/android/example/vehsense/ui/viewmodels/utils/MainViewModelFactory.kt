package com.android.example.vehsense.ui.viewmodels.utils

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.example.vehsense.network.CurrentVehicleManager
import com.android.example.vehsense.ui.viewmodels.MainViewModel

class MainViewModelFactory(
    private val application: Application,
    private val currentVehicleManager: CurrentVehicleManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, currentVehicleManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
