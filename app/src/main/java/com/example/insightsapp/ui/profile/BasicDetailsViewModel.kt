package com.example.insightsapp.ui.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.insightsapp.data.creditScore.CreditScoreService
import com.example.insightsapp.data.database.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class BasicDetailsState(
    val fullName: String = "",
    val dateOfBirth: String = "",
    val selectedGender: String = "",
    val panNumber: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val creditScoreLoading: Boolean = false,
    val creditScore: Int = 0
)

class BasicDetailsViewModel(
    private val phoneNumber: String,
    private val context: Context
) : ViewModel() {

    private val creditScoreService = CreditScoreService()

    private val _state = MutableStateFlow(BasicDetailsState())
    val state: StateFlow<BasicDetailsState> = _state.asStateFlow()

    fun updateFullName(name: String) {
        _state.value = _state.value.copy(fullName = name, errorMessage = null)
    }

    fun updateDateOfBirth(date: String) {
        _state.value = _state.value.copy(dateOfBirth = date, errorMessage = null)
    }

    fun updateGender(gender: String) {
        _state.value = _state.value.copy(selectedGender = gender, errorMessage = null)
    }

    fun updatePanNumber(pan: String) {
        val formattedPan = pan.uppercase().take(10)
        _state.value = _state.value.copy(panNumber = formattedPan, errorMessage = null)
    }

    fun validateInputs(): Boolean {
        val currentState = _state.value

        return when {
            currentState.fullName.trim().length < 2 -> {
                _state.value = currentState.copy(errorMessage = "Please enter a valid full name")
                false
            }
            currentState.dateOfBirth.isEmpty() || !isValidDate(currentState.dateOfBirth) -> {
                _state.value = currentState.copy(errorMessage = "Please enter a valid date of birth")
                false
            }
            currentState.selectedGender.isEmpty() -> {
                _state.value = currentState.copy(errorMessage = "Please select your gender")
                false
            }
            currentState.panNumber.length != 10 || !isValidPAN(currentState.panNumber) -> {
                _state.value = currentState.copy(errorMessage = "Please enter a valid PAN number")
                false
            }
            else -> true
        }
    }

    fun fetchCreditScore(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(creditScoreLoading = true, errorMessage = null)

            try {
                val result = creditScoreService.getCreditScore(
                    panNumber = _state.value.panNumber, // ✅ Fixed: use _state.value
                    fullName = _state.value.fullName, // ✅ Fixed: use _state.value
                    dateOfBirth = _state.value.dateOfBirth, // ✅ Fixed: use _state.value
                )

                result.fold(
                    onSuccess = { response ->
                        // Update state with credit score FIRST
                        _state.value = _state.value.copy(
                            creditScoreLoading = false,
                            creditScore = response.score,
                            errorMessage = null
                        )

                        // ✅ SAVE ALL BASIC DETAILS TO DATABASE
                        saveBasicDetailsToDatabase()

                        println("✅ Basic details saved and credit score fetched: ${response.score}")
                        onComplete(true)
                    },
                    onFailure = { exception ->
                        _state.value = _state.value.copy(
                            creditScoreLoading = false,
                            errorMessage = exception.message ?: "Failed to fetch credit score"
                        )
                        println("❌ Credit score fetch failed: ${exception.message}")
                        onComplete(false)
                    }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    creditScoreLoading = false,
                    errorMessage = e.message ?: "An error occurred"
                )
                println("❌ Exception in fetchCreditScore: ${e.message}")
                onComplete(false)
            }
        }
    }

    // ✅ Add this method to save basic details
    private suspend fun saveBasicDetailsToDatabase() {
        try {
            val database = AppDatabase.getDatabase(context)
            val currentState = _state.value

            // Update user with basic details
            database.userDao().updateUserBasicDetails(
                phoneNumber = phoneNumber,
                fullName = currentState.fullName,
                dateOfBirth = currentState.dateOfBirth,
                gender = currentState.selectedGender, // ✅ Fixed: use selectedGender
                panNumber = currentState.panNumber,
                creditScore = currentState.creditScore,
                completed = true,
                timestamp = System.currentTimeMillis()
            )

            println("✅ Basic details saved to database:")
            println("   - Name: ${currentState.fullName}")
            println("   - DOB: ${currentState.dateOfBirth}")
            println("   - Gender: ${currentState.selectedGender}")
            println("   - PAN: ${currentState.panNumber}")
            println("   - Credit Score: ${currentState.creditScore}")

        } catch (e: Exception) {
            println("❌ Error saving basic details: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun isValidDate(date: String): Boolean {
        return try {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            format.isLenient = false
            val parsedDate = format.parse(date)

            // Check if date is not in future and person is at least 18 years old
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -18)
            val eighteenYearsAgo = calendar.time

            parsedDate != null && parsedDate.before(Date()) && parsedDate.before(eighteenYearsAgo)
        } catch (e: Exception) {
            false
        }
    }

    private fun isValidPAN(pan: String): Boolean {
        val panRegex = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$".toRegex()
        return panRegex.matches(pan)
    }
}

// ✅ Add ViewModelFactory
class BasicDetailsViewModelFactory(
    private val phoneNumber: String,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BasicDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BasicDetailsViewModel(phoneNumber, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
