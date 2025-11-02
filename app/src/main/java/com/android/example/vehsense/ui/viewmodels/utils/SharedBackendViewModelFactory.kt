package com.android.example.vehsense.ui.viewmodels.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.example.vehsense.network.SessionManager
import com.android.example.vehsense.ui.screens.SplashScreen
import com.android.example.vehsense.ui.viewmodels.SplashViewModel
import com.android.example.vehsense.ui.viewmodels.UserViewModel
import com.android.example.vehsense.ui.viewmodels.VehicleAddViewModel
import com.android.example.vehsense.ui.viewmodels.VehicleUpdateViewModel
import com.android.example.vehsense.ui.viewmodels.VehicleViewModel

class SharedBackendViewModelFactory(
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SplashViewModel::class.java) ->
                SplashViewModel(sessionManager) as T

            modelClass.isAssignableFrom(VehicleViewModel::class.java) ->
                VehicleViewModel(sessionManager) as T

            modelClass.isAssignableFrom(VehicleAddViewModel::class.java) ->
                VehicleAddViewModel(sessionManager) as T

            modelClass.isAssignableFrom(VehicleUpdateViewModel::class.java) ->
                VehicleUpdateViewModel(sessionManager) as T

            modelClass.isAssignableFrom(UserViewModel::class.java) ->
                UserViewModel(sessionManager) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        }
    }
}
