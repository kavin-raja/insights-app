package com.example.insightsapp.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.insightsapp.data.database.Transaction
import com.example.insightsapp.data.remote.RemoteDatabaseProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WalletState(
    val currentBalance: Double = 0.0,
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false
)

class WalletViewModel(
    private val databaseProvider: RemoteDatabaseProvider,
    private val phoneNumber: String
) : ViewModel() {

    private val _state = MutableStateFlow(WalletState())
    val state: StateFlow<WalletState> = _state.asStateFlow()

    init {
        loadWalletData()
    }

    private fun loadWalletData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            try {
                println("🔄 WalletViewModel: Loading wallet data for $phoneNumber")

                // ✅ Get user by phone number to get userId
                val user = databaseProvider.userRepository.getUserByPhoneNumber(phoneNumber)

                if (user != null) {
                    println("👤 User found: ${user.userId}")

                    // ✅ Collect transactions flow using userId
                    databaseProvider.transactionRepository.getTransactionsByUserId(user.userId).collect { transactions ->
                        // ✅ Calculate balance from transactions
                        val balance = calculateBalance(transactions)

                        println("💰 Wallet data loaded:")
                        println("   - Balance: $balance")
                        println("   - Transactions: ${transactions.size}")

                        _state.value = WalletState(
                            currentBalance = balance,
                            transactions = transactions,
                            isLoading = false
                        )
                    }
                } else {
                    println("❌ User not found for phone number: $phoneNumber")
                    _state.value = _state.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                println("❌ Error loading wallet  ${e.message}")
                e.printStackTrace()
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    // ✅ Helper function to calculate balance
    private fun calculateBalance(transactions: List<Transaction>): Double {
        var balance = 0.0
        transactions.forEach { transaction ->
            when (transaction.type) {
                "CREDIT" -> balance += transaction.amount
                "DEBIT" -> balance -= transaction.amount
            }
        }
        return balance
    }

    fun addSignupReward() {
        viewModelScope.launch {
            try {
                println("🎁 Adding signup reward for $phoneNumber")

                // Get user by phone number to get userId
                val user = databaseProvider.userRepository.getUserByPhoneNumber(phoneNumber)

                if (user != null) {
                    // Check if signup reward already exists
                    val existingRewards = databaseProvider.transactionRepository.getTransactionsByType(
                        user.userId, "Signup Reward"
                    )

                    if (existingRewards.isEmpty()) {
                        // Add signup reward transaction
                        val rewardTransaction = Transaction(
                            userId = user.userId, // ✅ Use userId instead of phoneNumber
                            type = "CREDIT",
                            amount = 500.0,
                            description = "Signup Reward",
                            timestamp = System.currentTimeMillis(),
                            status = "SUCCESS"
                        )

                        databaseProvider.transactionRepository.insertTransaction(rewardTransaction)
                        println("✅ Signup reward of ₹500 added for $phoneNumber")

                        // Reload wallet data to reflect the new transaction
                        loadWalletData()
                    } else {
                        println("⚠️ Signup reward already exists for $phoneNumber")
                    }
                } else {
                    println("❌ User not found when adding signup reward: $phoneNumber")
                }
            } catch (e: Exception) {
                println("❌ Error adding signup reward: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}

// ✅ Add ViewModelFactory
class WalletViewModelFactory(
    private val databaseProvider: RemoteDatabaseProvider,
    private val phoneNumber: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WalletViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WalletViewModel(databaseProvider, phoneNumber) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
