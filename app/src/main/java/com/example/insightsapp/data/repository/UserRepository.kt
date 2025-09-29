package com.example.insightsapp.data.repository

import com.example.insightsapp.data.database.User
import com.example.insightsapp.data.remote.RemoteUserDataSource

class UserRepository(
    private val remoteDataSource: RemoteUserDataSource
) {

    suspend fun getUserByPhoneNumber(phoneNumber: String): User? {
        return remoteDataSource.getUserByPhoneNumber(phoneNumber)
    }

    suspend fun getUserByUserId(userId: String): User? {
        return remoteDataSource.getUserByUserId(userId)
    }

    suspend fun insertUser(user: User): User {
        return remoteDataSource.insertUser(user)
    }

    suspend fun updateUser(user: User) {
        remoteDataSource.updateUser(user)
    }
    suspend fun updateBasicDetails(
        userId: String,
        fullName: String,
        dateOfBirth: String,
        gender: String,
        panNumber: String,
        creditScore: Int,
        completed: Boolean,
        timestamp: Long
    ) {
        remoteDataSource.updateBasicDetails(
            userId, fullName, dateOfBirth, gender, panNumber, creditScore, completed, timestamp
        )
    }


    suspend fun updateVerificationStatus(userId: String, verified: Boolean) {
        remoteDataSource.updateVerificationStatus(userId, verified)
    }

    suspend fun updateLastLogin(userId: String, timestamp: Long) {
        remoteDataSource.updateLastLogin(userId, timestamp)
    }

    suspend fun markAccountComplete(userId: String, complete: Boolean, hasReward: Boolean, timestamp: Long) {
        remoteDataSource.markAccountComplete(userId, complete, hasReward, timestamp)
    }
    suspend fun updateUserPermissions(
        userId: String,
        callLog: Boolean,
        messages: Boolean,
        storage: Boolean,
        deviceInfo: Boolean,
        consentGiven: Boolean,
        timestamp: Long
    ) {
        remoteDataSource.updateUserPermissions(
            userId, callLog, messages, storage, deviceInfo, consentGiven, timestamp
        )
    }

    suspend fun updateCreditScore(
        userId: String,
        score: Int,
        provider: String,
        timestamp: Long,
        status: String
    ) {
        remoteDataSource.updateCreditScore(userId, score, provider, timestamp, status)
    }

}
