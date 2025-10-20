package com.android.example.vehsense.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

@Composable
fun RideScreen(
    onForceBack: () -> Unit,
    btIsOn: State<Boolean>,
    hasPermission: State<Boolean>,
    ) {
    if(!btIsOn.value || !hasPermission.value) {
        onForceBack()
    }
    Text("This is the ride screen", style = MaterialTheme.typography.titleLarge)
}