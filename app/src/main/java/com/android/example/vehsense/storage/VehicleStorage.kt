package com.android.example.vehsense.storage

import android.content.Context
import android.content.SharedPreferences

class VehicleStorage(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences("veh_prefs", Context.MODE_PRIVATE)

    fun saveVehicleId(id: Int) {
        prefs.edit().putString("veh_id", id.toString()).apply()
    }

    fun getVehicleId(): Int? {
        val id = prefs.getString("veh_id", null)

        return id?.toInt()
    }

    fun clearVehicleId() {
        prefs.edit().remove("veh_id").apply()
    }
}