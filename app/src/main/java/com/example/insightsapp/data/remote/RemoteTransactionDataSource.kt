package com.example.insightsapp.data.remote

import com.example.insightsapp.data.database.Transaction
import com.example.insightsapp.data.remote.dto.toSupabaseTransaction
import com.example.insightsapp.data.remote.dto.toTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*

class RemoteTransactionDataSource(
    private val httpClient: SupabaseHttpClient = SupabaseHttpClient.getInstance()
) {

    fun getTransactionsByUserId(userId: String): Flow<List<Transaction>> = flow {
        try {
            println("🔍 Fetching transactions for user: $userId")

            val result = httpClient.selectTransactions(
                filter = "user_id=eq.$userId",
                order = "timestamp.desc"
            )

            val transactions = result.map { transaction -> transaction.toTransaction() }
            println("💰 Found ${transactions.size} transactions")
            emit(transactions)
        } catch (e: Exception) {
            println("❌ Error fetching transactions: ${e.message}")
            e.printStackTrace()
            emit(emptyList())
        }
    }

    suspend fun insertTransaction(transaction: Transaction) {
        try {
            val transactionId = UUID.randomUUID().toString()
            val newTransaction = transaction.copy(transactionId = transactionId)
            val supabaseTransaction = newTransaction.toSupabaseTransaction()

            val success = httpClient.insertTransaction(supabaseTransaction)

            if (success) {
                println("✅ Transaction created: ${transaction.description} for ${transaction.userId}")
            } else {
                throw Exception("Failed to insert transaction")
            }
        } catch (e: Exception) {
            println("❌ Error creating transaction: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    suspend fun getTransactionsByType(userId: String, description: String): List<Transaction> {
        return try {
            val result = httpClient.selectTransactions(
                filter = "user_id=eq.$userId&description=eq.$description"
            )

            result.map { transaction -> transaction.toTransaction() }
        } catch (e: Exception) {
            println("❌ Error fetching transactions by type: ${e.message}")
            emptyList()
        }
    }
}
