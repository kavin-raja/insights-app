package com.example.insightsapp.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.insightsapp.data.auth.AuthenticationService
import com.example.insightsapp.data.database.Survey
import com.example.insightsapp.data.database.Transaction
import com.example.insightsapp.data.database.User
import com.example.insightsapp.data.remote.RemoteDatabaseProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MainUIState(
    val user: User? = null,
    val walletBalance: Double = 0.0,
    val transactions: List<Transaction> = emptyList(),
    val surveys: List<Survey> = emptyList(),
    val isLoading: Boolean = false
)

class MainViewModel(
    private val databaseProvider: RemoteDatabaseProvider,
    private val phoneNumber: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUIState())
    val uiState: StateFlow<MainUIState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                println("🔄 MainViewModel: Loading data for $phoneNumber")

                // ✅ Use repository instead of direct data source
                val user = databaseProvider.userRepository.getUserByPhoneNumber(phoneNumber)
                println("👤 User loaded: ${user?.fullName} (${user?.phoneNumber})")

                if (user != null) {
                    // Load transactions
                    databaseProvider.transactionRepository.getTransactionsByUserId(user.userId)
                        .collect { transactions ->
                            val balance = calculateBalance(transactions)
                            println("💰 Balance calculated: $balance from ${transactions.size} transactions")

                            // Load surveys
                            databaseProvider.surveyRepository.getActiveSurveys().collect { surveys ->
                                println("📋 Surveys loaded: ${surveys.size} items")

                                _uiState.value = MainUIState(
                                    user = user,
                                    walletBalance = balance,
                                    transactions = transactions,
                                    surveys = surveys,
                                    isLoading = false
                                )

                                println("✅ MainViewModel: State updated successfully")
                                return@collect
                            }
                            return@collect
                        }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                println("❌ Error loading user  ${e.message}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun calculateBalance(transactions: List<Transaction>): Double {
        return transactions.sumOf { transaction ->
            when (transaction.type) {
                "CREDIT" -> transaction.amount
                "DEBIT" -> -transaction.amount
                else -> 0.0
            }
        }
    }

    fun addSignupReward() {
        viewModelScope.launch {
            try {
                val authService = AuthenticationService(databaseProvider)
                authService.completeUserOnboarding(phoneNumber)

                loadUserData()

            } catch (e: Exception) {
                println("❌ Error in addSignupReward: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun initializeSampleSurveys() {
        viewModelScope.launch {
            try {
                // ✅ Use repository instead of direct data source
                val existingSurveysCount = databaseProvider.surveyRepository.getAllSurveysCount()
                println("🔍 Existing surveys count: $existingSurveysCount")

                if (existingSurveysCount == 0) {
                    val sampleSurveys = listOf(
                        Survey(
                            surveyId = "survey_001",
                            title = "Brand 1 Survey",
                            description = "Help us improve our product by sharing your thoughts.",
                            brandName = "Brand 1",
                            reward = 250.0,
                            durationMinutes = 3,
                            isActive = true
                        ),
                        Survey(
                            surveyId = "survey_002",
                            title = "Brand 2 Survey",
                            description = "Discuss your preferences and help us improve our products.",
                            brandName = "Brand 2",
                            reward = 250.0,
                            durationMinutes = 5,
                            isActive = true
                        ),
                        Survey(
                            surveyId = "survey_003",
                            title = "Brand 3 Survey",
                            description = "Share your thoughts on our latest products and services.",
                            brandName = "Brand 3",
                            reward = 500.0,
                            durationMinutes = 10,
                            isActive = true
                        ),
                        Survey(
                            surveyId = "survey_004",
                            title = "Brand 4 Survey",
                            description = "Provide feedback on our latest features.",
                            brandName = "Brand 4",
                            reward = 100.0,
                            durationMinutes = 4,
                            isActive = true
                        )
                    )

                    databaseProvider.surveyRepository.insertAllSurveys(sampleSurveys)
                    println("✅ Sample surveys initialized: ${sampleSurveys.size} surveys")
                } else {
                    println("✅ Surveys already exist: $existingSurveysCount surveys")
                }
            } catch (e: Exception) {
                println("❌ Error initializing surveys: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun onCouponPurchased(couponCost: Double) {
        // Refresh user data to get updated transactions
        loadUserData()
    }
}
