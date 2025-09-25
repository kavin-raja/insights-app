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
class UserRepository @Inject constructor(
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
        return try {
            firebaseAuthService.sendOtp(phoneNumber, activity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyOtp(otp: String): Result<FirebaseUser> {
        return try {
            firebaseAuthService.verifyOtp(otp)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Added: Method to save user after successful verification
    suspend fun saveUserToDatabase(phoneNumber: String, firebaseUser: FirebaseUser) {
        val user = User(
            phoneNumber = phoneNumber,
            isVerified = true,
            createdAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis()
        )
        userDao.insertUser(user)
    }

    // ✅ Added: Resend OTP method
    suspend fun resendOtp(phoneNumber: String, activity: Activity): Result<String> {
        return try {
            firebaseAuthService.resendOtp(phoneNumber, activity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUser() = firebaseAuthService.getCurrentUser()

    fun signOut() = firebaseAuthService.signOut()
}
