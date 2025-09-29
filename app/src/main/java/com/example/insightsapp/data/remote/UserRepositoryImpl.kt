package com.example.insightsapp.data.remote

import com.google.firebase.auth.FirebaseAuth

interface UserRepository {
    suspend fun currentUserId(): String
    companion object { fun instance(): UserRepository = UserRepositoryImpl }
}

private object UserRepositoryImpl : UserRepository {
    override suspend fun currentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: UserIdGenerator.getInstance().generateUserId()
    }
}
