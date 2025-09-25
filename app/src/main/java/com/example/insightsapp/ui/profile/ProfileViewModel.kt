package com.example.insightsapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.insightsapp.data.database.AppDatabase
import com.example.insightsapp.data.database.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val notificationsEnabled: Boolean = true,
    val isLoading: Boolean = false
)

class ProfileViewModel(
    private val database: AppDatabase,
    private val phoneNumber: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val user = database.userDao().getUserByPhoneNumber(phoneNumber)
                println("📱 Profile: Loading user data for $phoneNumber")
                println("👤 User: ${user?.fullName} (${user?.phoneNumber})")

                _uiState.value = _uiState.value.copy(
                    user = user,
                    isLoading = false
                )
            } catch (e: Exception) {
                println("❌ Error loading profile: ${e.message}")
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
        println("🔔 Notifications ${if (enabled) "enabled" else "disabled"}")

        // TODO: Save notification preference to database or SharedPreferences
        viewModelScope.launch {
            try {
                // You can add a notifications preference field to User entity
                // For now, we'll just log it
                println("💾 Saving notification preference: $enabled")
            } catch (e: Exception) {
                println("❌ Error saving notification preference: ${e.message}")
            }
        }
    }

    fun onPrivacyClick() {
        println("🔒 Privacy & Data Consent clicked")
        // TODO: Navigate to privacy screen
    }

    fun onCouponsClick() {
        println("🎫 My Coupons clicked")
        // TODO: Navigate to coupons screen
    }

    fun onBankAccountClick() {
        println("🏦 Link Bank Account clicked")
        // TODO: Navigate to bank linking screen
    }

    fun onHelpClick() {
        println("❓ Help & Support clicked")
        // TODO: Navigate to help screen or open support
    }
}

class ProfileViewModelFactory(
    private val database: AppDatabase,
    private val phoneNumber: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(database, phoneNumber) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
