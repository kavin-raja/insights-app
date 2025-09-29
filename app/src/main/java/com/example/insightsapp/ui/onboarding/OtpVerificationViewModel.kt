package com.example.insightsapp.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.insightsapp.data.auth.FirebaseAuthService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OtpVerificationViewModel : ViewModel() {

    // ✅ Initialize your existing Firebase service
    private val firebaseAuthService = FirebaseAuthService()

    private val _otpValue = MutableStateFlow("")
    val otpValue: StateFlow<String> = _otpValue.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _resendTimer = MutableStateFlow(60)
    val resendTimer: StateFlow<Int> = _resendTimer.asStateFlow()

    private var resendJob: Job? = null

    fun onOtpChange(value: String) {
        _otpValue.value = value
    }

    fun sendInitialOtp(phoneNumber: String, activity: android.app.Activity) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // ✅ Call your existing Firebase service method
                val result = firebaseAuthService.sendOtp(phoneNumber, activity)
                result.fold(
                    onSuccess = {
                        println("OTP sent successfully to $phoneNumber")
                        startResendTimer()
                    },
                    onFailure = { exception ->
                        _errorMessage.value = exception.message ?: "Failed to send OTP"
                        println("Failed to send OTP: ${exception.message}")
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to send OTP"
                println("Exception sending OTP: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resendOtp(phoneNumber: String, activity: android.app.Activity) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = firebaseAuthService.sendOtp(phoneNumber, activity)
                result.fold(
                    onSuccess = {
                        println("OTP resent successfully to $phoneNumber")
                        startResendTimer()
                    },
                    onFailure = { exception ->
                        _errorMessage.value = exception.message ?: "Failed to resend OTP"
                        println("Failed to resend OTP: ${exception.message}")
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to resend OTP"
                println("Exception resending OTP: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun verifyOtp(phoneNumber: String, otp: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = firebaseAuthService.verifyOtp(otp)
                result.fold(
                    onSuccess = { firebaseUser ->
                        _errorMessage.value = null
                        println("Firebase: OTP verified successfully for $phoneNumber")
                        onResult(true)
                    },
                    onFailure = { exception ->
                        val message = when {
                            exception.message?.contains("invalid", ignoreCase = true) == true -> "Invalid OTP code"
                            exception.message?.contains("expired", ignoreCase = true) == true -> "OTP has expired"
                            else -> exception.message ?: "Verification failed"
                        }
                        _errorMessage.value = message
                        println("Firebase: OTP verification failed - ${exception.message}")
                        onResult(false)
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Verification failed"
                println("Firebase: Exception verifying OTP - ${e.message}")
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun startResendTimer() {
        resendJob?.cancel()
        _resendTimer.value = 60
        resendJob = viewModelScope.launch {
            while (_resendTimer.value > 0) {
                delay(1000)
                _resendTimer.value = _resendTimer.value - 1
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        resendJob?.cancel()
    }
}
