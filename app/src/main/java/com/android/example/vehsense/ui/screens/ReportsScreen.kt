package com.android.example.vehsense.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ReportsScreen(
    userId: Int,
    token: String
) {
    Text("This is the REPORTS SCREEN", style = MaterialTheme.typography.titleLarge)
}