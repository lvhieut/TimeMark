package com.example.timemarkbase.utils

import android.content.Context

object MainFeaturePrefs {
    private const val PREFS_NAME = "main_feature_prefs"
    private const val KEY_FULL_NAME = "key_full_name"

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
}
