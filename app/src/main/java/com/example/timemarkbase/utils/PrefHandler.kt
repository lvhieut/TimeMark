package com.example.timemarkbase.utils

import android.content.Context
import android.content.SharedPreferences

object PrefHelper {

    private const val PREF_NAME = "app_prefs"
    private const val KEY_NAME = "key_name"
    private const val KEY_ADDRESS = "key_address"

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

    // Get
    fun getName(context: Context): String {
        return getPrefs(context).getString(KEY_NAME, "") ?: ""
    }

    fun getAddress(context: Context): String {
        return getPrefs(context).getString(KEY_ADDRESS, "") ?: ""
    }
}