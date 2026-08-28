package com.example.timemarkbase.utils

import android.content.Context
import com.example.timemarkbase.time_mark.TimeMarkStyle

object MainFeaturePrefs {
    private const val PREFS_NAME = "main_feature_prefs"
    private const val KEY_FULL_NAME = "key_full_name"
    private const val KEY_COMPANY_NAME = "key_company_name"
    private const val KEY_ENABLE_COMPANY_NAME = "key_enable_company_name"
    private const val KEY_LATITUDE = "key_latitude"
    private const val KEY_LONGITUDE = "key_longitude"
    private const val KEY_TIME_MARK_STYLE = "key_time_mark_style"

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

    fun saveCompanyName(context: Context, companyName: String) {
        prefs(context).edit()
            .putString(KEY_COMPANY_NAME, companyName)
            .apply()
    }

    fun getCompanyName(context: Context): String? {
        return prefs(context).getString(KEY_COMPANY_NAME, null)
    }

    fun saveEnableCompanyName(context: Context, enable: Boolean) {
        prefs(context).edit()
            .putBoolean(KEY_ENABLE_COMPANY_NAME, enable)
            .apply()
    }

    fun isEnableCompanyName(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_ENABLE_COMPANY_NAME, true)
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

    fun saveTimeMarkStyle(context: Context, style: TimeMarkStyle) {
        prefs(context).edit()
            .putString(KEY_TIME_MARK_STYLE, style.name)
            .apply()
    }

    fun getTimeMarkStyle(context: Context): TimeMarkStyle {
        return TimeMarkStyle.fromName(prefs(context).getString(KEY_TIME_MARK_STYLE, null))
    }
}
