package com.android.example.vehsense.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.example.vehsense.ui.viewmodels.BackendViewModel
import com.android.example.vehsense.ui.viewmodels.BackendViewModelFactory

@Composable
fun ReportsScreen(
    userId: Int,
    token: String
) {
    val backendViewModel: BackendViewModel = viewModel(
        factory = BackendViewModelFactory(userId, token)
    )
    Text("This is the REPORTS SCREEN")
}