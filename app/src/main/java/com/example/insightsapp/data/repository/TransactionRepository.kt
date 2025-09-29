package com.example.insightsapp.data.repository

import com.example.insightsapp.data.database.Transaction
import com.example.insightsapp.data.remote.RemoteTransactionDataSource
import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val remoteDataSource: RemoteTransactionDataSource
) {

    fun getTransactionsByUserId(userId: String): Flow<List<Transaction>> {
        return remoteDataSource.getTransactionsByUserId(userId)
    }

    suspend fun insertTransaction(transaction: Transaction) {
        remoteDataSource.insertTransaction(transaction)
    }

    suspend fun getTransactionsByType(userId: String, description: String): List<Transaction> {
        return remoteDataSource.getTransactionsByType(userId, description)
    }
}
