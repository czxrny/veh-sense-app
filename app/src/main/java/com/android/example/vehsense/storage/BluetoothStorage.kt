package com.android.example.vehsense.storage

import android.content.Context

class BluetoothStorage(context: Context) {
    private val prefs = context.getSharedPreferences("bt_prefs", Context.MODE_PRIVATE)

    fun saveDeviceAddress(address: String) {
        prefs.edit().putString("device_address", address).apply()
    }

    fun getSavedDeviceAddress(): String? {
        return prefs.getString("device_address", null)
    }

    fun clearDeviceAddress() {
        prefs.edit().remove("device_address").apply()
    }
}