package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("kivopay_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LOGGED_USER_ID = "logged_user_id"
        private const val KEY_LAST_ACTIVE_TIME = "last_active_time"
        // Session timeout: 7 days of inactivity (604800000L ms)
        private const val SESSION_TIMEOUT_MS = 7L * 24 * 60 * 60 * 1000
    }

    fun saveUserSession(userId: String) {
        prefs.edit()
            .putString(KEY_LOGGED_USER_ID, userId)
            .putLong(KEY_LAST_ACTIVE_TIME, System.currentTimeMillis())
            .apply()
    }

    fun updateLastActive() {
        if (getSavedUserId() != null) {
            prefs.edit().putLong(KEY_LAST_ACTIVE_TIME, System.currentTimeMillis()).apply()
        }
    }

    fun getValidSessionUserId(): String? {
        val userId = prefs.getString(KEY_LOGGED_USER_ID, null) ?: return null
        val lastActive = prefs.getLong(KEY_LAST_ACTIVE_TIME, 0L)
        val now = System.currentTimeMillis()

        return if (now - lastActive > SESSION_TIMEOUT_MS) {
            // Session expired due to long inactivity
            clearSession()
            null
        } else {
            // Active session still valid, update timestamp
            updateLastActive()
            userId
        }
    }

    fun getSavedUserId(): String? {
        return prefs.getString(KEY_LOGGED_USER_ID, null)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
