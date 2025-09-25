package com.example.insightsapp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val type: String, // "CREDIT" or "DEBIT"
    val amount: Double,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS" // SUCCESS, PENDING, FAILED
)
