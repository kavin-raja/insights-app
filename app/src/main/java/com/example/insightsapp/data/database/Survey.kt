package com.example.insightsapp.data.database

data class Survey(
    val surveyId: String,
    val title: String,
    val description: String,
    val brandName: String,
    val reward: Double,
    val durationMinutes: Int, // Changed from duration
    val isActive: Boolean = true
)
