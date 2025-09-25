
package com.example.insightsapp.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE phoneNumber = :phoneNumber ORDER BY timestamp DESC")
    fun getTransactionsByPhoneNumber(phoneNumber: String): Flow<List<Transaction>>

    @Query("SELECT SUM(amount) FROM transactions WHERE phoneNumber = :phoneNumber AND type = 'CREDIT'")
    suspend fun getTotalCredits(phoneNumber: String): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE phoneNumber = :phoneNumber AND type = 'DEBIT'")
    suspend fun getTotalDebits(phoneNumber: String): Double?

    @Query("SELECT * FROM transactions WHERE phoneNumber = :phoneNumber AND description = :description")
    suspend fun getTransactionsByType(phoneNumber: String, description: String): List<Transaction>

}
