package com.example.insightsapp.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.insightsapp.data.database.Transaction
import com.example.insightsapp.data.remote.RemoteDatabaseProvider
import com.example.insightsapp.data.remote.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WalletState(
    val currentBalance: Double = 0.0,
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false
)

class WalletViewModel(
    private val databaseProvider: RemoteDatabaseProvider,
    private val phoneNumber: String
) : ViewModel() {

    private val _state = MutableStateFlow(WalletState())
    val state: StateFlow<WalletState> = _state.asStateFlow()

    private val walletRepository = WalletRepository.instance()
    private var userId: String? = null

    init {
        loadWalletData()
    }

    private fun loadWalletData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            try {
                println("🔄 WalletViewModel: Loading wallet data for $phoneNumber")

                val user = databaseProvider.userRepository.getUserByPhoneNumber(phoneNumber)

                if (user != null) {
                    userId = user.userId
                    println("👤 User found: ${user.userId}")

                    // ✅ Load transactions and calculate balance from them
                    loadTransactions(user.userId)

                } else {
                    println("❌ User not found for phone number: $phoneNumber")
                    _state.value = _state.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                println("❌ Error loading wallet: ${e.message}")
                e.printStackTrace()
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    private fun loadTransactions(userId: String) {
        viewModelScope.launch {
            try {
                databaseProvider.transactionRepository.getTransactionsByUserId(userId).collect { transactions ->
                    println("💰 WalletViewModel: Transactions loaded: ${transactions.size}")

                    // ✅ Calculate balance from transactions
                    val calculatedBalance = calculateBalance(transactions)

                    _state.value = _state.value.copy(
                        transactions = transactions,
                        currentBalance = calculatedBalance,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                println("❌ Error loading transactions: ${e.message}")
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    // ✅ Calculate balance from list of transactions
    private fun calculateBalance(transactions: List<Transaction>): Double {
        var balance = 0.0
        transactions.forEach { transaction ->
            when (transaction.type) {
                "CREDIT" -> balance += transaction.amount
                "DEBIT" -> balance -= transaction.amount
            }
        }
        println("📊 Balance calculation: ${transactions.size} transactions = $balance")
        return balance
    }

    // ✅ Force refresh wallet data
    fun refreshWalletData() {
        userId?.let { id ->
            println("🔄 WalletViewModel: Force refreshing wallet data")
            loadTransactions(id)
        }
    }

    // ✅ Called after coupon purchase for immediate update
    fun onCouponPurchased(couponCost: Double) {
        _state.value = _state.value.copy(
            currentBalance = _state.value.currentBalance - couponCost
        )

        // Also trigger a refresh to sync with server
        refreshWalletData()
    }

    fun addSignupReward() {
        viewModelScope.launch {
            try {
                println("🎁 Adding signup reward for $phoneNumber")

                val user = databaseProvider.userRepository.getUserByPhoneNumber(phoneNumber)

                if (user != null) {
                    val existingRewards = databaseProvider.transactionRepository.getTransactionsByType(
                        user.userId, "Signup Reward"
                    )

                    if (existingRewards.isEmpty()) {
                        val rewardTransaction = Transaction(
                            userId = user.userId,
                            type = "CREDIT",
                            amount = 500.0,
                            description = "Signup Reward",
                            timestamp = System.currentTimeMillis(),
                            status = "SUCCESS"
                        )

                        databaseProvider.transactionRepository.insertTransaction(rewardTransaction)
                        println("✅ Signup reward of ₹500 added for $phoneNumber")

                        // Refresh wallet data
                        refreshWalletData()
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

// ViewModelFactory for WalletViewModel
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
