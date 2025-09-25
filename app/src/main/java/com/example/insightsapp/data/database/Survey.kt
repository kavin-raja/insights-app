package com.example.insightsapp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "surveys")
data class Survey(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val brandName: String,
    val reward: Double,
    val duration: Int, // in minutes
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
