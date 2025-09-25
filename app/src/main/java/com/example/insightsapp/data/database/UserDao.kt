package com.example.insightsapp.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber")
    suspend fun getUserByPhoneNumber(phoneNumber: String): User?

    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber")
    fun getUserByPhoneNumberFlow(phoneNumber: String): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET isVerified = :isVerified WHERE phoneNumber = :phoneNumber")
    suspend fun updateVerificationStatus(phoneNumber: String, isVerified: Boolean)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("""
        UPDATE users SET 
        hasCallLogPermission = :callLog,
        hasMessagesPermission = :messages, 
        hasStoragePermission = :storage,
        hasDeviceInfoPermission = :deviceInfo,
        privacyConsentGiven = :consentGiven,
        privacyConsentTimestamp = :timestamp 
        WHERE phoneNumber = :phoneNumber
    """)
    suspend fun updateUserPermissions(
        phoneNumber: String,
        callLog: Boolean,
        messages: Boolean,
        storage: Boolean,
        deviceInfo: Boolean,
        consentGiven: Boolean,
        timestamp: Long
    )

    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber AND privacyConsentGiven = 1")
    suspend fun getUserWithConsent(phoneNumber: String): User?

    @Query("""
    UPDATE users SET 
    fullName = :fullName,
    dateOfBirth = :dateOfBirth,
    gender = :gender,
    panNumber = :panNumber,
    basicDetailsCompleted = :completed,
    basicDetailsTimestamp = :timestamp 
    WHERE phoneNumber = :phoneNumber
""")
    suspend fun updateBasicDetails(
        phoneNumber: String,
        fullName: String,
        dateOfBirth: String,
        gender: String,
        panNumber: String,
        completed: Boolean,
        timestamp: Long
    )

    @Query("""
    UPDATE users SET 
    creditScore = :score,
    creditScoreProvider = :provider,
    creditScoreLastUpdated = :timestamp,
    creditScoreStatus = :status
    WHERE phoneNumber = :phoneNumber
""")
    suspend fun updateCreditScore(
        phoneNumber: String,
        score: Int,
        provider: String,
        timestamp: Long,
        status: String
    )

    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber AND basicDetailsCompleted = 1")
    suspend fun getUserWithBasicDetails(phoneNumber: String): User?

    @Query("UPDATE users SET walletBalance = :balance, totalEarned = :balance WHERE phoneNumber = :phoneNumber")
    suspend fun updateWalletBalance(phoneNumber: String, balance: Double)

    @Query("SELECT walletBalance FROM users WHERE phoneNumber = :phoneNumber")
    suspend fun getWalletBalance(phoneNumber: String): Double?

    @Query("UPDATE users SET walletBalance = walletBalance + :amount, totalEarned = totalEarned + :amount WHERE phoneNumber = :phoneNumber")
    suspend fun addToWallet(phoneNumber: String, amount: Double)

    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber AND isAccountComplete = 1")
    suspend fun getCompleteUserAccount(phoneNumber: String): User?

    @Query("UPDATE users SET isAccountComplete = :complete, hasReceivedSignupReward = :hasReward, signupRewardTimestamp = :timestamp WHERE phoneNumber = :phoneNumber")
    suspend fun markAccountComplete(phoneNumber: String, complete: Boolean, hasReward: Boolean, timestamp: Long)

    @Query("UPDATE users SET lastLoginAt = :timestamp WHERE phoneNumber = :phoneNumber")
    suspend fun updateLastLogin(phoneNumber: String, timestamp: Long)

    @Query("SELECT COUNT(*) FROM users WHERE phoneNumber = :phoneNumber AND isAccountComplete = 1")
    suspend fun isReturningUser(phoneNumber: String): Int

    @Query("""
    UPDATE users 
    SET fullName = :fullName, 
        dateOfBirth = :dateOfBirth, 
        gender = :gender, 
        panNumber = :panNumber,
        creditScore = :creditScore,
        basicDetailsCompleted = :completed, 
        basicDetailsTimestamp = :timestamp 
    WHERE phoneNumber = :phoneNumber
""")
    suspend fun updateUserBasicDetails(
        phoneNumber: String,
        fullName: String,
        dateOfBirth: String,
        gender: String,
        panNumber: String,
        creditScore: Int,
        completed: Boolean,
        timestamp: Long
    )

}
