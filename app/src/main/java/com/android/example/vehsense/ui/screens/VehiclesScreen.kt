package com.android.example.vehsense.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.example.vehsense.model.Vehicle
import androidx.compose.ui.unit.sp
import com.android.example.vehsense.ui.components.CircularProgressionScreen
import com.android.example.vehsense.ui.components.FadePopup
import com.android.example.vehsense.ui.viewmodels.VehicleViewModel

data class VehiclesUiState(
    val vehiclesState: VehicleViewModel.VehiclesState,
    val isPrivate: Boolean,
)

@Composable
fun VehiclesScreen(
    uiState: VehiclesUiState,
    onRefresh: () -> Unit,
    onSaveVehicle: (Vehicle) -> Unit,
    onGoToUpdateScreen: (Int) -> Unit,
    onDelete: (Vehicle) -> Unit,
    onGoToAddScreen: () -> Unit
) {
    var selectedVehicle by remember { mutableStateOf<Vehicle?>(null) }

    LaunchedEffect(Unit) { onRefresh() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("My Vehicles", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        when(uiState.vehiclesState) {
            is VehicleViewModel.VehiclesState.Loading -> CircularProgressionScreen()
            is VehicleViewModel.VehiclesState.Error -> {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.vehiclesState.message,
                    color = Color.Red,
                )
            }
            is VehicleViewModel.VehiclesState.Success -> {
                uiState.vehiclesState.vehicles.forEach { vehicle ->
                    Text("${vehicle.brand} ${vehicle.model}")
                    Button(
                        onClick = {
                            selectedVehicle = vehicle
                        }
                    ) {
                        Text("See details", style = MaterialTheme.typography.bodyLarge)
                    }
                    Button(
                        onClick = {
                            onSaveVehicle(vehicle)
                        }
                    ) {
                        Text("Set as current vehicle", style = MaterialTheme.typography.bodyLarge)
                    }
                    if(uiState.isPrivate) {
                        Button(
                            onClick = {
                                onGoToAddScreen()
                            }
                        ) {
                            Text("Add new vehicle", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }

    selectedVehicle?.let {
        ShowVehicleDetails(
            isPrivate = uiState.isPrivate,
            vehicle = selectedVehicle,
            onGoToUpdateScreen = onGoToUpdateScreen,
            onDelete = onDelete,
            onDismiss = {
                selectedVehicle = null
            }
        )
    }
}

@Composable
fun ShowVehicleDetails(
    isPrivate: Boolean,
    vehicle: Vehicle?,
    onGoToUpdateScreen: (Int) -> Unit,
    onDelete: (Vehicle) -> Unit,
    onDismiss: () -> Unit
) {
    var showDeletePopup by remember { mutableStateOf(false) }

    var isActive by remember { mutableStateOf(true) }

    vehicle?.let { v ->
        FadePopup(
            isActive = isActive,
            onBack = {
                isActive = false
            },
            onDismiss = onDismiss
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text("Vehicle details", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(v.brand, fontSize = 20.sp)
                Spacer(Modifier.height(8.dp))
                Text(v.model, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Text(v.year.toString(), fontSize = 112.sp)
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    isActive = false
                }) {
                    Text("Close", style = MaterialTheme.typography.bodyLarge)
                }
                if (isPrivate) {
                    Button(onClick = { onGoToUpdateScreen(vehicle.id) }) {
                        Text("Edit", style = MaterialTheme.typography.bodyLarge)
                    }
                    Button(
                        onClick = { showDeletePopup = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        )
                    ) { 
                        Text("Delete", style = MaterialTheme.typography.bodyLarge) 
                    }
                }
            }

            FadePopup(
                isActive = showDeletePopup,
                onBack = {
                    showDeletePopup = false
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Are you sure you want to delete this vehicle?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onDelete(vehicle)
                                showDeletePopup = false
                                isActive = false
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
                                showDeletePopup = false
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
}