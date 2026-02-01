package com.example.timemarkbase.utils

import android.content.Context

object UserPrefs {
    private const val PREFS_NAME = "user_prefs"
    private const val KEY_USER_ID = "key_user_id"
    private const val KEY_PASSWORD = "key_password"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveUser(context: Context, userId: String, password: String) {
        prefs(context).edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_PASSWORD, password)
            apply()
        }
    }

    fun getUserId(context: Context): String? {
        return prefs(context).getString(KEY_USER_ID, null)
    }

    fun getPassword(context: Context): String? {
        return prefs(context).getString(KEY_PASSWORD, null)
    }

    // Clear all
    fun clearUser(context: Context) {
        prefs(context).edit().clear().apply()
    }
}