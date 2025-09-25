package com.example.insightsapp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val phoneNumber: String,
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long? = null,
    val fcmToken: String? = null,

    //Permissions fields
    val hasCallLogPermission: Boolean = false,
    val hasMessagesPermission: Boolean = false,
    val hasStoragePermission: Boolean = false,
    val hasDeviceInfoPermission: Boolean = false,
    val privacyConsentGiven: Boolean = false,
    val privacyConsentTimestamp: Long = 0L,

    //Details
    val fullName: String = "",
    val dateOfBirth: String = "", // Format: DD/MM/YYYY
    val gender: String = "", // Male, Female, Other
    val panNumber: String = "",
    val basicDetailsCompleted: Boolean = false,
    val basicDetailsTimestamp: Long = 0L,

    //Credit score fields
    val creditScore: Int = 0,
    val creditScoreProvider: String = "", // e.g., "CIBIL", "Experian"
    val creditScoreLastUpdated: Long = 0L,
    val creditScoreStatus: String = "", // "SUCCESS", "FAILED", "PENDING"

    //Wallet fields
    val walletBalance: Double = 0.0,
    val totalEarned: Double = 0.0,

    //Account Status Fields
    val isAccountComplete: Boolean = false, // Has completed full onboarding
    val hasReceivedSignupReward: Boolean = false, // Already got signup reward
    val signupRewardTimestamp: Long = 0L

    )
