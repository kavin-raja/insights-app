package com.example.insightsapp.ui.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.insightsapp.data.remote.RemoteDatabaseProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PermissionState(
    val callLogPermission: Boolean = false,
    val messagesPermission: Boolean = false,
    val storagePermission: Boolean = false,
    val deviceInfoPermission: Boolean = false
)

class PrivacyConsentViewModel(
    private val databaseProvider: RemoteDatabaseProvider
) : ViewModel() {

    private val _isConsentGiven = MutableStateFlow(false)
    val isConsentGiven: StateFlow<Boolean> = _isConsentGiven.asStateFlow()

    private val _permissions = MutableStateFlow(PermissionState())
    val permissions: StateFlow<PermissionState> = _permissions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun toggleConsent() {
        _isConsentGiven.value = !_isConsentGiven.value
    }

    fun requestPermissions(onPermissionsRequested: () -> Unit) {
        if (_isConsentGiven.value) {
            onPermissionsRequested()
        }
    }

    fun updatePermissionGranted(permission: String, granted: Boolean) {
        viewModelScope.launch {
            val current = _permissions.value
            _permissions.value = when (permission) {
                android.Manifest.permission.READ_CALL_LOG ->
                    current.copy(callLogPermission = granted)
                android.Manifest.permission.READ_SMS ->
                    current.copy(messagesPermission = granted)
                android.Manifest.permission.READ_EXTERNAL_STORAGE ->
                    current.copy(storagePermission = granted)
                android.Manifest.permission.READ_PHONE_STATE ->
                    current.copy(deviceInfoPermission = granted)
                else -> current
            }
        }
    }

    // ✅ Updated method to use UserRepository instead of AuthRepository
    suspend fun savePermissionsToDatabase(phoneNumber: String) {
        _isLoading.value = true
        try {
            // Get user by phone number to get userId
            val user = databaseProvider.userRepository.getUserByPhoneNumber(phoneNumber)

            if (user != null) {
                val permissions = _permissions.value

                // ✅ Use your existing updateUserPermissions method
                databaseProvider.userRepository.updateUserPermissions(
                    userId = user.userId, // ✅ Use userId instead of phoneNumber
                    callLog = permissions.callLogPermission,
                    messages = permissions.messagesPermission,
                    storage = permissions.storagePermission,
                    deviceInfo = permissions.deviceInfoPermission,
                    consentGiven = _isConsentGiven.value,
                    timestamp = System.currentTimeMillis()
                )

                println("✅ Permissions saved to database for user: ${user.userId}")
                println("   - Call Log: ${permissions.callLogPermission}")
                println("   - Messages: ${permissions.messagesPermission}")
                println("   - Storage: ${permissions.storagePermission}")
                println("   - Device Info: ${permissions.deviceInfoPermission}")
                println("   - Consent Given: ${_isConsentGiven.value}")
            } else {
                println("❌ User not found for phone number: $phoneNumber")
            }
        } catch (e: Exception) {
            println("❌ Error saving permissions: ${e.message}")
            e.printStackTrace()
        } finally {
            _isLoading.value = false
        }
    }
}

// ✅ Add ViewModelFactory
class PrivacyConsentViewModelFactory(
    private val databaseProvider: RemoteDatabaseProvider
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrivacyConsentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PrivacyConsentViewModel(databaseProvider) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
