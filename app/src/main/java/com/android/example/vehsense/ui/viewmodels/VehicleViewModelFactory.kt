package com.android.example.vehsense.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class VehicleViewModelFactory(private val userId: Int, private var token: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VehicleViewModel::class.java)) {
            return VehicleViewModel(userId, token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}