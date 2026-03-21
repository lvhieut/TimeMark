package com.example.timemarkbase.utils

import android.content.Context
import android.content.SharedPreferences

object PrefHelper {

    private const val PREF_NAME = "app_prefs"
    private const val KEY_NAME = "key_name"
    private const val KEY_ADDRESS = "key_address"
    private const val KEY_LAT = "key_lat"
    private const val KEY_LON = "key_lon"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // Save
    fun saveName(context: Context, name: String) {
        getPrefs(context).edit().putString(KEY_NAME, name).apply()
    }

    fun saveAddress(context: Context, address: String) {
        getPrefs(context).edit().putString(KEY_ADDRESS, address).apply()
    }

    fun saveLocation(context: Context, lat: Double, lon: Double) {
        getPrefs(context).edit()
            .putFloat(KEY_LAT, lat.toFloat())
            .putFloat(KEY_LON, lon.toFloat())
            .apply()
    }

    fun getLocation(context: Context): Pair<Double?, Double?> {
        val prefs = getPrefs(context)

        if (!prefs.contains(KEY_LAT) || !prefs.contains(KEY_LON)) {
            return null to null
        }

        val lat = prefs.getFloat(KEY_LAT, 0f).toDouble()
        val lon = prefs.getFloat(KEY_LON, 0f).toDouble()

        return lat to lon
    }


    // Get
    fun getName(context: Context): String {
        return getPrefs(context).getString(KEY_NAME, "") ?: ""
    }

    fun getAddress(context: Context): String {
        return getPrefs(context).getString(KEY_ADDRESS, "") ?: ""
    }
}