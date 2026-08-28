package com.example.timemarkbase.utils

import android.content.Context

object MainFeaturePrefs {
    private const val PREFS_NAME = "main_feature_prefs"
    private const val KEY_FULL_NAME = "key_full_name"
    private const val KEY_LATITUDE = "key_latitude"
    private const val KEY_LONGITUDE = "key_longitude"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveFullName(context: Context, fullName: String) {
        prefs(context).edit()
            .putString(KEY_FULL_NAME, fullName)
            .apply()
    }

    fun getFullName(context: Context): String? {
        return prefs(context).getString(KEY_FULL_NAME, null)
    }

    fun saveLatLon(context: Context, latitude: Double, longitude: Double) {
        prefs(context).edit()
            .putString(KEY_LATITUDE, latitude.toString())
            .putString(KEY_LONGITUDE, longitude.toString())
            .apply()
    }

    fun getLatLon(context: Context): Pair<Double, Double>? {
        val latitude = prefs(context).getString(KEY_LATITUDE, null)?.toDoubleOrNull()
        val longitude = prefs(context).getString(KEY_LONGITUDE, null)?.toDoubleOrNull()

        return if (latitude != null && longitude != null) {
            Pair(latitude, longitude)
        } else {
            null
        }
    }
}
