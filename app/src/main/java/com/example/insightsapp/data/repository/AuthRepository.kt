package com.example.insightsapp.data.repository

import android.app.Activity
import com.example.insightsapp.data.auth.FirebaseAuthService
import com.example.insightsapp.data.database.User
import com.example.insightsapp.data.database.UserDao
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao,
    private val firebaseAuthService: FirebaseAuthService
) {
    fun getUserByPhoneNumber(phoneNumber: String): Flow<User?> {
        return userDao.getUserByPhoneNumberFlow(phoneNumber)
    }

    suspend fun createOrUpdateUser(phoneNumber: String): User {
        val existingUser = userDao.getUserByPhoneNumber(phoneNumber)

        return if (existingUser != null) {
            val updatedUser = existingUser.copy(lastLoginAt = System.currentTimeMillis())
            userDao.updateUser(updatedUser)
            updatedUser
        } else {
            val newUser = User(phoneNumber = phoneNumber)
            userDao.insertUser(newUser)
            newUser
        }
    }

    suspend fun verifyUser(phoneNumber: String) {
        userDao.updateVerificationStatus(phoneNumber, true)
        userDao.updateLastLogin(phoneNumber, System.currentTimeMillis())
    }

    suspend fun sendOtp(phoneNumber: String, activity: Activity): Result<String> {
        return firebaseAuthService.sendOtp(phoneNumber, activity)
    }

    suspend fun verifyOtp(otp: String): Result<FirebaseUser> {
        return firebaseAuthService.verifyOtp(otp)
    }

    suspend fun resendOtp(phoneNumber: String, activity: Activity): Result<String> {
        return firebaseAuthService.resendOtp(phoneNumber, activity)
    }

    suspend fun saveUserToDatabase(phoneNumber: String, firebaseUser: FirebaseUser) {
        val user = User(
            phoneNumber = phoneNumber,
            isVerified = true,
            createdAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis()
        )
        userDao.insertUser(user)
    }

    fun getCurrentUser() = firebaseAuthService.getCurrentUser()

    fun signOut() = firebaseAuthService.signOut()

    suspend fun updateUserPermissions(
        phoneNumber: String,
        callLog: Boolean,
        messages: Boolean,
        storage: Boolean,
        deviceInfo: Boolean,
        consentGiven: Boolean
    ) {
        userDao.updateUserPermissions(
            phoneNumber = phoneNumber,
            callLog = callLog,
            messages = messages,
            storage = storage,
            deviceInfo = deviceInfo,
            consentGiven = consentGiven,
            timestamp = System.currentTimeMillis()
        )
    }

    suspend fun updateBasicDetails(
        phoneNumber: String,
        fullName: String,
        dateOfBirth: String,
        gender: String,
        panNumber: String
    ) {
        userDao.updateBasicDetails(
            phoneNumber = phoneNumber,
            fullName = fullName,
            dateOfBirth = dateOfBirth,
            gender = gender,
            panNumber = panNumber,
            completed = true,
            timestamp = System.currentTimeMillis()
        )
    }

    suspend fun updateCreditScore(
        phoneNumber: String,
        score: Int,
        provider: String,
        status: String
    ) {
        userDao.updateCreditScore(
            phoneNumber = phoneNumber,
            score = score,
            provider = provider,
            timestamp = System.currentTimeMillis(),
            status = status
        )
    }
}
