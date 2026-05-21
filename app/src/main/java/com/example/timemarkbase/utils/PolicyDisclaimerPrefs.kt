package com.example.timemarkbase.utils

import android.content.Context

object PolicyDisclaimerPrefs {
    private const val PREFS_NAME = "policy_disclaimer_prefs"
    private const val KEY_HAS_ACCEPTED_POLICY = "key_has_accepted_policy"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasAcceptedPolicy(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_HAS_ACCEPTED_POLICY, false)
    }

    fun setAcceptedPolicy(context: Context) {
        prefs(context).edit()
            .putBoolean(KEY_HAS_ACCEPTED_POLICY, true)
            .apply()
    }
}
