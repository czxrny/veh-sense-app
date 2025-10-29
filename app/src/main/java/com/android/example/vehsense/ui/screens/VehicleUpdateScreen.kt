package com.android.example.vehsense.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.android.example.vehsense.model.NewVehicleForm
import com.android.example.vehsense.model.VehicleUpdateForm

@Composable
fun VehicleUpdateScreen(
    onSubmit: (VehicleUpdateForm) -> Unit,
    errorMessage: String?,
) {
    var enginePower by rememberSaveable { mutableStateOf("") }
    var expectedFuel by rememberSaveable { mutableStateOf("") }
    var plates by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Edit vehicle")

        OutlinedTextField(
            value = enginePower,
            onValueChange = { enginePower = it },
            label = { Text("Engine power (HP)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = expectedFuel,
            onValueChange = { expectedFuel = it },
            label = { Text("Expected fuel (L/100km)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = plates,
            onValueChange = { plates = it },
            label = { Text("Plates (optional)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val form = VehicleUpdateForm(
                    enginePower = enginePower,
                    expectedFuel = expectedFuel,
                    plates = plates
                )
                onSubmit(form)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Edit")
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = Color.Red,
            )
        }
    }
}