package com.example.insightsapp.data.session

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class UserSessionManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "user_session_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // ✅ Save user session data
    fun saveUserSession(phoneNumber: String, isVerified: Boolean) {
        sharedPreferences.edit()
            .putString("phone_number", phoneNumber)
            .putBoolean("is_verified", isVerified)
            .apply()
    }

    // ✅ Get phone number
    fun getPhoneNumber(): String? {
        return sharedPreferences.getString("phone_number", null)
    }

    // ✅ Check if user is verified
    fun isUserVerified(): Boolean {
        return sharedPreferences.getBoolean("is_verified", false)
    }

    // ✅ Save user ID (NEW - no conflict)
    fun saveCurrentUserId(userId: String) {
        sharedPreferences.edit()
            .putString("current_user_id", userId)
            .apply()
        println("💾 User ID saved: $userId")
    }

    // ✅ Get user ID (NEW - renamed to avoid conflict)
    fun getCurrentUserId(): String? {
        val userId = sharedPreferences.getString("current_user_id", null)
        println("📱 Retrieved user ID: $userId")
        return userId
    }

    // ✅ Clear session
    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }

    // ✅ Check if user is logged in
    fun isLoggedIn(): Boolean {
        return getPhoneNumber() != null && isUserVerified()
    }
}
