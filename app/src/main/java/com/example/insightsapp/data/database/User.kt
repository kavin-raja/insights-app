package com.example.insightsapp.data.database

data class User(
    val userId: String = "",
    val phoneNumber: String = "",
    val phoneNumberHash: String = "",
    val fullName: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val panNumber: String = "",
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis(),
    val fcmToken: String? = null,

    val hasCallLogPermission: Boolean = false,
    val hasMessagesPermission: Boolean = false,
    val hasStoragePermission: Boolean = false,
    val hasDeviceInfoPermission: Boolean = false,
    val privacyConsentGiven: Boolean = false,
    val privacyConsentTimestamp: Long = 0L,

    val basicDetailsCompleted: Boolean = false,
    val basicDetailsTimestamp: Long = 0L,

    val creditScore: Int = 0,
    val creditScoreProvider: String = "",
    val creditScoreLastUpdated: Long = 0L,
    val creditScoreStatus: String = "",

    val isAccountComplete: Boolean = false,
    val hasReceivedSignupReward: Boolean = false,
    val signupRewardTimestamp: Long = 0L
)
