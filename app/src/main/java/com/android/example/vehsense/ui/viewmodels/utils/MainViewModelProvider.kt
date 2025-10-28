package com.android.example.vehsense.ui.viewmodels.utils

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.example.vehsense.core.AppContainer
import com.android.example.vehsense.ui.viewmodels.MainViewModel

// Get or create a new MainViewModel instance for the Activity
@SuppressLint("ContextCastToActivity")
@Composable
fun getMainViewModel(): MainViewModel {
    val activity = (LocalContext.current as ComponentActivity)

    val factory = remember {
        MainViewModelFactory(
            application = activity.application,
            currentVehicleManager = AppContainer.currentVehicleManager
        )
    }

    return viewModel(
        activity,
        factory = factory
    )
}
