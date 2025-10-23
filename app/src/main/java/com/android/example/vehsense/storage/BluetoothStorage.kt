package com.android.example.vehsense.storage

import android.content.Context
import android.content.SharedPreferences
import com.android.example.vehsense.model.DeviceInfo

object BluetoothStorage {
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("bt_prefs", Context.MODE_PRIVATE)
    }

    fun saveDeviceInfo(deviceInfo: DeviceInfo) {
        prefs.edit().putString("device_name", deviceInfo.name).apply()
        prefs.edit().putString("device_address", deviceInfo.address).apply()
    }

    fun getSavedDeviceInfo(): DeviceInfo? {
        val name = prefs.getString("device_name", null)
        val address = prefs.getString("device_address", null)

        return if (address != null && name != null) {
            DeviceInfo(name, address)
        } else {
            null
        }
    }

    fun clearDeviceAddress() {
        prefs.edit().remove("device_name").apply()
        prefs.edit().remove("device_address").apply()
    }
}