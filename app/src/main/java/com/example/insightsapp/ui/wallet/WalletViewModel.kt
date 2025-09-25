package com.example.insightsapp.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.insightsapp.data.database.Transaction
import com.example.insightsapp.data.database.TransactionDao
import com.example.insightsapp.data.database.UserDao
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
    private val userDao: UserDao,
    private val transactionDao: TransactionDao,
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
                // ✅ Collect transactions flow
                transactionDao.getTransactionsByPhoneNumber(phoneNumber).collect { transactions ->
                    // ✅ Calculate balance from transactions
                    val balance = calculateBalance(transactions)

                    _state.value = WalletState(
                        currentBalance = balance,
                        transactions = transactions,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                println("Error loading wallet  ${e.message}")
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
                // Check if signup reward already exists
                val existingTransactions = _state.value.transactions
                val hasSignupReward = existingTransactions.any {
                    it.description == "Signup Reward"
                }

                if (!hasSignupReward) {
                    // Add signup reward transaction
                    val rewardTransaction = Transaction(
                        phoneNumber = phoneNumber,
                        type = "CREDIT",
                        amount = 500.0,
                        description = "Signup Reward",
                        timestamp = System.currentTimeMillis(),
                        status = "SUCCESS"
                    )

                    transactionDao.insertTransaction(rewardTransaction)
                    println("Signup reward of ₹500 added for $phoneNumber")
                }
            } catch (e: Exception) {
                println("Error adding signup reward: ${e.message}")
            }
        }
    }
}
