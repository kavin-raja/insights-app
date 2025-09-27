package com.example.insightsapp.data.database

data class Transaction(
    val transactionId: String = "",
    val userId: String, // Changed from phoneNumber to userId
    val type: String,
    val amount: Double,
    val description: String,
    val timestamp: Long,
    val status: String = "SUCCESS"
)
