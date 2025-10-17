package com.android.example.vehsense.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BackendViewModelFactory(private val userId: Int, private var token: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BackendViewModel::class.java)) {
            return BackendViewModel(userId, token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}