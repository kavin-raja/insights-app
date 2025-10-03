package com.example.insightsapp.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.insightsapp.data.auth.AuthenticationService
import com.example.insightsapp.data.database.Survey
import com.example.insightsapp.data.database.Transaction
import com.example.insightsapp.data.database.User
import com.example.insightsapp.data.remote.RemoteDatabaseProvider
import com.example.insightsapp.data.repository.SurveyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    private val phoneNumber: String,
    private val surveyRepository: SurveyRepository = SurveyRepository.getInstance()
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

                // 1. Load user
                val user = databaseProvider.userRepository.getUserByPhoneNumber(phoneNumber)
                println("👤 User loaded: ${user?.fullName} (${user?.phoneNumber})")

                if (user == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    return@launch
                }

                // 2. Load transactions - DIRECT CALL, NO FLOWS
                val transactions = try {
                    val transactionRepo = databaseProvider.transactionRepository
                    // Call a method that returns List<Transaction> directly
                    getTransactionsDirectly(transactionRepo, user.userId)
                } catch (e: Exception) {
                    println("⚠️ Error loading transactions: ${e.message}")
                    emptyList()
                }

                val balance = calculateBalance(transactions)
                println("💰 Balance calculated: $balance from ${transactions.size} transactions")

                // 3. Load surveys from API - DIRECT CALL
                val surveyList = try {
                    val apiSurveys = surveyRepository.apiService.getSurveys()
                    apiSurveys.map { it.toSurvey() }
                } catch (e: Exception) {
                    println("⚠️ Error loading surveys: ${e.message}")
                    emptyList()
                }

                println("📋 Surveys loaded: ${surveyList.size} items")

                // 4. Update state
                _uiState.value = MainUIState(
                    user = user,
                    walletBalance = balance,
                    transactions = transactions,
                    surveys = surveyList,
                    isLoading = false
                )

                println("✅ MainViewModel: State updated successfully")

            } catch (e: Exception) {
                println("❌ Error loading user  ${e.message}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    // Helper method to get transactions without flows
    private suspend fun getTransactionsDirectly(
        transactionRepo: Any, // Replace with your actual transaction repository type
        userId: String
    ): List<com.example.insightsapp.data.database.Transaction> {
        return try {
            // You'll need to add a method to your transaction repository that returns List directly
            // For now, collect the flow once and return the list
            transactionRepo::class.java.getMethod("getTransactionsByUserIdDirect", String::class.java)
                .invoke(transactionRepo, userId) as List<com.example.insightsapp.data.database.Transaction>
        } catch (e: Exception) {
            println("⚠️ Direct transaction fetch failed, trying flow once: ${e.message}")
            // Fallback: collect flow once
            val method = transactionRepo::class.java.getMethod("getTransactionsByUserId", String::class.java)
            val flow = method.invoke(transactionRepo, userId) as kotlinx.coroutines.flow.Flow<List<com.example.insightsapp.data.database.Transaction>>
            flow.first()
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

    fun onCouponPurchased(couponCost: Double) {
        // Refresh user data to get updated transactions
        loadUserData()
    }
}
