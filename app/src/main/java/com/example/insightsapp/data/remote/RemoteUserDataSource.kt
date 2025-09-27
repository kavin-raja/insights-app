package com.example.insightsapp.data.remote

import com.example.insightsapp.data.database.User
import com.example.insightsapp.data.remote.dto.toSupabaseUser
import com.example.insightsapp.data.remote.dto.toUser
import java.security.MessageDigest

class RemoteUserDataSource(
    private val httpClient: SupabaseHttpClient = SupabaseHttpClient.getInstance(),
    private val userIdGenerator: UserIdGenerator = UserIdGenerator.getInstance()
) {

    suspend fun getUserByPhoneNumber(phoneNumber: String): User? {
        return try {
            println("🔍 Fetching user for phone: $phoneNumber")

            // ✅ Use the new method that handles phone number encoding
            val supabaseUser = httpClient.selectUserByPhoneNumber(phoneNumber)
            val user = supabaseUser?.toUser()

            println("👤 Found user: ${user?.fullName} (ID: ${user?.userId})")
            user
        } catch (e: Exception) {
            println("❌ Error fetching user: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun getUserByUserId(userId: String): User? {
        return try {
            val result = httpClient.selectUsers(
                filter = "user_id=eq.\"$userId\"",
                limit = 1
            )

            result.firstOrNull()?.toUser()
        } catch (e: Exception) {
            println("❌ Error fetching user by ID: ${e.message}")
            null
        }
    }

    suspend fun insertUser(user: User): User {
        return try {
            // ✅ Use human-readable ID
            val userId = userIdGenerator.generateUserId()
            val phoneHash = hashPhoneNumber(user.phoneNumber)

            val newUser = user.copy(
                userId = userId,
                phoneNumberHash = phoneHash,
                createdAt = System.currentTimeMillis(),
                lastLoginAt = System.currentTimeMillis()
            )

            val supabaseUser = newUser.toSupabaseUser()
            val success = httpClient.insertUser(supabaseUser)

            if (success) {
                println("✅ User created: ${user.phoneNumber} with ID: $userId")
                newUser
            } else {
                throw Exception("Failed to insert user")
            }
        } catch (e: Exception) {
            println("❌ Error creating user: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    suspend fun updateUser(user: User) {
        try {
            val supabaseUser = user.toSupabaseUser()

            // Convert to Map for update
            val updates = mapOf(
                "full_name" to supabaseUser.full_name,
                "date_of_birth" to supabaseUser.date_of_birth,
                "gender" to supabaseUser.gender,
                "pan_number" to supabaseUser.pan_number,
                "is_verified" to supabaseUser.is_verified,
                "last_login_at" to supabaseUser.last_login_at,
                "has_call_log_permission" to supabaseUser.has_call_log_permission,
                "has_messages_permission" to supabaseUser.has_messages_permission,
                "has_storage_permission" to supabaseUser.has_storage_permission,
                "has_device_info_permission" to supabaseUser.has_device_info_permission,
                "privacy_consent_given" to supabaseUser.privacy_consent_given,
                "privacy_consent_timestamp" to supabaseUser.privacy_consent_timestamp,
                "basic_details_completed" to supabaseUser.basic_details_completed,
                "basic_details_timestamp" to supabaseUser.basic_details_timestamp,
                "credit_score" to supabaseUser.credit_score,
                "credit_score_provider" to supabaseUser.credit_score_provider,
                "credit_score_last_updated" to supabaseUser.credit_score_last_updated,
                "credit_score_status" to supabaseUser.credit_score_status,
                "is_account_complete" to supabaseUser.is_account_complete,
                "has_received_signup_reward" to supabaseUser.has_received_signup_reward,
                "signup_reward_timestamp" to supabaseUser.signup_reward_timestamp
            )

            // ✅ Use proper phone number filter
            val success = httpClient.updateUser(updates, "phone_number=eq.\"${user.phoneNumber}\"")

            if (success) {
                println("✅ User updated: ${user.userId}")
            } else {
                throw Exception("Failed to update user")
            }
        } catch (e: Exception) {
            println("❌ Error updating user: ${e.message}")
            e.printStackTrace()
            throw e
        }
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
        try {
            println("🔄 Updating basic details for user: $userId")
            println("🔄 Name: '$fullName', DOB: '$dateOfBirth', Gender: '$gender', PAN: '$panNumber'")

            val updates = mapOf(
                "full_name" to fullName,
                "date_of_birth" to dateOfBirth,
                "gender" to gender,
                "pan_number" to panNumber,
                "credit_score" to creditScore,
                "basic_details_completed" to completed,
                "basic_details_timestamp" to timestamp
            )

            // ✅ Try multiple update methods
            var success = false

            // Method 1: Update by user_id with quotes
            val filter1 = "user_id=eq.\"$userId\""
            success = httpClient.updateUser(updates, filter1)

            if (!success) {
                println("⚠️ Method 1 failed, trying Method 2...")
                // Method 2: Update by user_id without quotes
                val filter2 = "user_id=eq.$userId"
                success = httpClient.updateUser(updates, filter2)
            }

            if (success) {
                println("✅ Basic details updated for user: $userId")
            } else {
                throw Exception("Failed to update basic details with all methods")
            }
        } catch (e: Exception) {
            println("❌ Error updating basic details: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }


    suspend fun updateVerificationStatus(userId: String, verified: Boolean) {
        try {
            val updates = mapOf("is_verified" to verified)
            val success = httpClient.updateUser(updates, "user_id=eq.\"$userId\"")

            if (success) {
                println("✅ Verification status updated: $userId -> $verified")
            } else {
                throw Exception("Failed to update verification")
            }
        } catch (e: Exception) {
            println("❌ Error updating verification: ${e.message}")
            throw e
        }
    }

    suspend fun updateLastLogin(userId: String, timestamp: Long) {
        try {
            val updates = mapOf("last_login_at" to timestamp)
            val success = httpClient.updateUser(updates, "user_id=eq.\"$userId\"")

            if (success) {
                println("✅ Last login updated: $userId")
            } else {
                throw Exception("Failed to update last login")
            }
        } catch (e: Exception) {
            println("❌ Error updating last login: ${e.message}")
            throw e
        }
    }

    suspend fun markAccountComplete(userId: String, complete: Boolean, hasReward: Boolean, timestamp: Long) {
        try {
            val updates = mapOf(
                "is_account_complete" to complete,
                "has_received_signup_reward" to hasReward,
                "signup_reward_timestamp" to timestamp
            )

            val success = httpClient.updateUser(updates, "user_id=eq.\"$userId\"")

            if (success) {
                println("✅ Account completion updated: $userId")
            } else {
                throw Exception("Failed to mark account complete")
            }
        } catch (e: Exception) {
            println("❌ Error marking account complete: ${e.message}")
            throw e
        }
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
        try {
            val updates = mapOf(
                "has_call_log_permission" to callLog,
                "has_messages_permission" to messages,
                "has_storage_permission" to storage,
                "has_device_info_permission" to deviceInfo,
                "privacy_consent_given" to consentGiven,
                "privacy_consent_timestamp" to timestamp
            )

            val success = httpClient.updateUser(updates, "user_id=eq.\"$userId\"")

            if (success) {
                println("✅ User permissions updated: $userId")
            } else {
                throw Exception("Failed to update permissions")
            }
        } catch (e: Exception) {
            println("❌ Error updating permissions: ${e.message}")
            throw e
        }
    }

    suspend fun updateCreditScore(
        userId: String,
        score: Int,
        provider: String,
        timestamp: Long,
        status: String
    ) {
        try {
            val updates = mapOf(
                "credit_score" to score,
                "credit_score_provider" to provider,
                "credit_score_last_updated" to timestamp,
                "credit_score_status" to status
            )

            val success = httpClient.updateUser(updates, "user_id=eq.\"$userId\"")

            if (success) {
                println("✅ Credit score updated: $userId -> $score")
            } else {
                throw Exception("Failed to update credit score")
            }
        } catch (e: Exception) {
            println("❌ Error updating credit score: ${e.message}")
            throw e
        }
    }

    private fun hashPhoneNumber(phoneNumber: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(phoneNumber.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
