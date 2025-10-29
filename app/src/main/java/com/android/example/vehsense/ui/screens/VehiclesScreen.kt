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
import com.android.example.vehsense.model.Vehicle
import androidx.compose.animation.fadeIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.ui.unit.sp

data class VehiclesUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val isPrivate: Boolean,
    val error: String? = null
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

        Text("Private: ${uiState.isPrivate}")
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(uiState.vehicles) { vehicle ->
                Row {
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
        uiState.error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = Color.Red,
            )
        }
    }

    ShowVehicleDetails(
        vehicle = selectedVehicle,
        onGoToUpdateScreen = onGoToUpdateScreen,
        onDelete = onDelete,
        onDismiss = { selectedVehicle = null }
    )
}

@Composable
fun ShowVehicleDetails(
    vehicle: Vehicle?,
    onGoToUpdateScreen: (Int) -> Unit,
    onDelete: (Vehicle) -> Unit,
    onDismiss: () -> Unit,
) {
    var showDeletePopup by remember { mutableStateOf<Boolean>(false) }

    var visible by remember { mutableStateOf(false) }

    // For the animated fading transitions - when entering this func, the visibility changes and the box fades in
    LaunchedEffect(vehicle) {
        visible = vehicle != null
    }

    BackHandler(enabled = vehicle != null) {
        visible = false
    }

    vehicle?.let { v ->
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
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
                    Button(onClick = { visible = false }) {
                        Text("Close")
                    }
                    Button(onClick = { onGoToUpdateScreen(vehicle.id) }) {
                        Text("Edit")
                    }
                    Button(
                        onClick = { showDeletePopup = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White)
                    ) { Text("Delete") }
                }
            }
        }

        if(showDeletePopup) {
            BackHandler() {
                showDeletePopup = false
            }

            Popup(
                alignment = Alignment.Center,
                onDismissRequest = { showDeletePopup = false }
            ) {
                Box(
                    modifier = Modifier
                        .size(300.dp)
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
                                    onDelete(vehicle)
                                    showDeletePopup = false
                                    visible = false
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

    if (!visible) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(300)
            onDismiss()
        }
    }
}