package com.android.example.vehsense.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.example.vehsense.ui.viewmodels.BackendViewModel
import com.android.example.vehsense.ui.viewmodels.BackendViewModelFactory
import com.android.example.vehsense.ui.viewmodels.VehicleViewModel
import com.android.example.vehsense.ui.viewmodels.VehicleViewModelFactory

@Composable
fun VehiclesScreen(
    userId: Int,
    token: String
) {
    val viewModel: VehicleViewModel = viewModel(
        factory = VehicleViewModelFactory(userId, token)
    )

    val vehicles by viewModel.vehicles.collectAsState()
    val error by viewModel.errorMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("This is the VEHICLE SCREEN",)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(vehicles) { vehicle ->
                Text("${vehicle.brand} ${vehicle.model}")
                Divider()
            }
        }

        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = Color.Red,
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getVehicles()
    }
}
