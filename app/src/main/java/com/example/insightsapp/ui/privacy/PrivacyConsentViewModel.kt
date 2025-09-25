package com.example.insightsapp.ui.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.insightsapp.data.repository.AuthRepository
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

class PrivacyConsentViewModel : ViewModel() {
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

    suspend fun savePermissionsToDatabase(phoneNumber: String, authRepository: AuthRepository) {
        _isLoading.value = true
        try {
            val permissions = _permissions.value
            authRepository.updateUserPermissions(
                phoneNumber = phoneNumber,
                callLog = permissions.callLogPermission,
                messages = permissions.messagesPermission,
                storage = permissions.storagePermission,
                deviceInfo = permissions.deviceInfoPermission,
                consentGiven = _isConsentGiven.value
            )
        } finally {
            _isLoading.value = false
        }
    }
}
