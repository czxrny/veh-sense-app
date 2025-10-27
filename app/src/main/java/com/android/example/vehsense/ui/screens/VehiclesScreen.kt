package com.android.example.vehsense.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.example.vehsense.core.AppContainer
import com.android.example.vehsense.model.Vehicle
import com.android.example.vehsense.ui.viewmodels.VehicleViewModel
import com.android.example.vehsense.ui.viewmodels.utils.SharedBackendViewModelFactory

@Composable
fun VehiclesScreen(
    onGoToAddScreen: () -> Unit
) {
    val vehicleViewModel: VehicleViewModel = viewModel(
        factory = SharedBackendViewModelFactory(AppContainer.sessionManager)
    )

    val vehicles by vehicleViewModel.vehicles.collectAsState()
    val error by vehicleViewModel.errorMessage.collectAsState()

    var showDeletePopup by remember { mutableStateOf<Vehicle?>(null) }

    LaunchedEffect(Unit) {
        vehicleViewModel.getVehicles()
    }

    showDeletePopup?.let { vehicle ->
        BackHandler() {
            showDeletePopup = null
        }

        Popup(
            alignment = Alignment.Center,
            onDismissRequest = { showDeletePopup = null }
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp)
                    .background(color = Color.White, shape = RectangleShape)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Are you sure you want to delete this vehicle?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                vehicleViewModel.deleteVehicle(vehicle)
                                showDeletePopup = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            )
                        ) {
                            Text("YES", style = MaterialTheme.typography.bodyLarge)
                        }
                        Button(
                            onClick = {
                                showDeletePopup = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Gray,
                                contentColor = Color.White
                            )
                        ) {
                            Text("NO", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("My Vehicles", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(vehicles) { vehicle ->
                Row {
                    Text("${vehicle.brand} ${vehicle.model}")
                    Button(
                        onClick = {
                            showDeletePopup = vehicle
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Delete", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Spacer(Modifier.height(2.dp))
            }
            }
        Button(
            onClick = {
                onGoToAddScreen()
            }
        ) {
            Text("Add new vehicle", style = MaterialTheme.typography.bodyLarge)
        }
        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = Color.Red,
            )
        }
    }
}

