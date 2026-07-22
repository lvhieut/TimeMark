package com.example.timemarkbase.utils

import android.content.Context

object UserPrefs {
    private const val PREFS_NAME = "user_prefs"
    private const val KEY_USER_ID = "key_user_id"
    private const val KEY_PASSWORD = "key_password"
    private const val KEY_ENABLE_IMAGE_GOOGLE_MAP = "key_enable_image_google_map"
    private const val KEY_ENABLE_FULL_NAME = "key_enable_full_name"

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

    fun saveEnableImageGoogleMap(context: Context, enable: Boolean) {
        prefs(context).edit()
            .putBoolean(KEY_ENABLE_IMAGE_GOOGLE_MAP, enable)
            .apply()
    }

    fun isEnableImageGoogleMap(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_ENABLE_IMAGE_GOOGLE_MAP, false)
    }

    fun saveEnableFullName(context: Context, enable: Boolean) {
        prefs(context).edit()
            .putBoolean(KEY_ENABLE_FULL_NAME, enable)
            .apply()
    }

    fun isEnableFullName(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_ENABLE_FULL_NAME, false)
    }

    // Clear user credentials only so feature preferences can survive app relaunches.
    fun clearUser(context: Context) {
        prefs(context).edit()
            .remove(KEY_USER_ID)
            .remove(KEY_PASSWORD)
            .apply()
    }
}
