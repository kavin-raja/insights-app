package com.example.insightsapp.ui.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OnboardingViewModel : ViewModel() {
    // State for phone number input
    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    // Update phone number with basic digits-only validation
    fun onPhoneNumberChange(newNumber: String) {
        _phoneNumber.value = newNumber.filter { it.isDigit() }.take(10)
    }

    // Validate phone number length for enabling continue button
    fun isValidPhoneNumber(number: String): Boolean {
        return number.length == 10
    }

    // Add other states and logic for onboarding as needed
}