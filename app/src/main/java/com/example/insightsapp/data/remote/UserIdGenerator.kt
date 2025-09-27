package com.example.insightsapp.data.remote

class UserIdGenerator {

    suspend fun generateUserId(): String {
        val httpClient = SupabaseHttpClient.getInstance()

        // Get current user count
        val existingUsers = httpClient.selectUsers()
        val userCount = existingUsers.size + 1

        // Generate USER0000000001, USER0000000002, etc.
        val userId = "USER${userCount.toString().padStart(10, '0')}"

        println("🆔 Generated User ID: $userId (Total users: ${existingUsers.size})")
        return userId
    }

    companion object {
        @Volatile
        private var INSTANCE: UserIdGenerator? = null

        fun getInstance(): UserIdGenerator {
            return INSTANCE ?: synchronized(this) {
                val instance = UserIdGenerator()
                INSTANCE = instance
                instance
            }
        }
    }
}
