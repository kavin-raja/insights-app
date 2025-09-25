package com.example.insightsapp.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class OnboardingViewModel : ViewModel() {
    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    val isValid: StateFlow<Boolean> = _phoneNumber
        .map { it.length == 10 } // Example validation: 10 digits
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun onPhoneNumberChange(newNumber: String) {
        _phoneNumber.value = newNumber.filter { it.isDigit() }.take(10)
    }

    fun sendOtp() {
        if (isValid.value) {
            // TODO: Implement actual OTP sending logic (e.g., call an API)
            println("OTP Sent to: ${phoneNumber.value}")
        }
    }
}
