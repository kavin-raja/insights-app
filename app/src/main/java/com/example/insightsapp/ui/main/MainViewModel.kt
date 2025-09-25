package com.example.insightsapp.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.insightsapp.data.auth.AuthenticationService
import com.example.insightsapp.data.database.AppDatabase
import com.example.insightsapp.data.database.Survey
import com.example.insightsapp.data.database.Transaction
import com.example.insightsapp.data.database.User
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
    private val database: AppDatabase,
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

                // ✅ Load user information first
                val user = database.userDao().getUserByPhoneNumber(phoneNumber)
                println("👤 User loaded: ${user?.fullName} (${user?.phoneNumber})")

                // ✅ Force collect transactions immediately, not as Flow
                val transactions = mutableListOf<Transaction>()
                database.transactionDao().getTransactionsByPhoneNumber(phoneNumber).collect { transactionList ->
                    transactions.clear()
                    transactions.addAll(transactionList)

                    val balance = calculateBalance(transactions)
                    println("💰 Balance calculated: $balance from ${transactions.size} transactions")

                    // ✅ Load surveys synchronously
                    val surveys = mutableListOf<Survey>()
                    database.surveyDao().getActiveSurveys().collect { surveyList ->
                        surveys.clear()
                        surveys.addAll(surveyList)

                        println("📋 Surveys loaded: ${surveys.size} items")

                        // ✅ Update state with all data
                        _uiState.value = MainUIState(
                            user = user,
                            walletBalance = balance,
                            transactions = transactions.toList(),
                            surveys = surveys.toList(),
                            isLoading = false
                        )

                        println("✅ MainViewModel: State updated successfully")
                        return@collect // Stop collecting to avoid infinite loop
                    }
                    return@collect
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
                val authService = AuthenticationService(database)
                authService.completeUserOnboarding(phoneNumber)

                // ✅ Reload data after adding reward
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
                // ✅ Get surveys count directly, not via Flow
                val existingSurveys = database.surveyDao().getAllSurveysCount()
                println("🔍 Existing surveys count: $existingSurveys")

                if (existingSurveys == 0) {
                    val sampleSurveys = listOf(
                        Survey(
                            id = "survey_001",
                            title = "Brand 1 Survey",
                            description = "Help us improve our product by sharing your thoughts.",
                            brandName = "Brand 1",
                            reward = 250.0,
                            duration = 3,
                            isActive = true
                        ),
                        Survey(
                            id = "survey_002",
                            title = "Brand 2 Survey",
                            description = "Discuss your preferences and help us improve our products.",
                            brandName = "Brand 2",
                            reward = 250.0,
                            duration = 5,
                            isActive = true
                        ),
                        Survey(
                            id = "survey_003",
                            title = "Brand 3 Survey",
                            description = "Share your thoughts on our latest products and services.",
                            brandName = "Brand 3",
                            reward = 500.0,
                            duration = 10,
                            isActive = true
                        ),
                        Survey(
                            id = "survey_004",
                            title = "Brand 4 Survey",
                            description = "Provide feedback on our latest features.",
                            brandName = "Brand 4",
                            reward = 100.0,
                            duration = 4,
                            isActive = true
                        )
                    )

                    database.surveyDao().insertAllSurveys(sampleSurveys)
                    println("✅ Sample surveys initialized: ${sampleSurveys.size} surveys")
                } else {
                    println("✅ Surveys already exist: $existingSurveys surveys")
                }
            } catch (e: Exception) {
                println("❌ Error initializing surveys: ${e.message}")
                e.printStackTrace()
            }
        }
    }

}
