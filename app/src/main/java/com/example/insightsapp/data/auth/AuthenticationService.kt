package com.example.insightsapp.data.auth

import com.example.insightsapp.data.database.Transaction
import com.example.insightsapp.data.database.User
import com.example.insightsapp.data.remote.RemoteDatabaseProvider
import com.example.insightsapp.data.remote.SupabaseHttpClient
import kotlinx.coroutines.flow.first

data class AuthenticationResult(
    val isReturningUser: Boolean,
    val user: User?,
    val shouldSkipOnboarding: Boolean
)

class AuthenticationService(
    private val databaseProvider: RemoteDatabaseProvider
) {

    // ✅ NEW: Create user and store ID
    suspend fun createUser(phoneNumber: String): User {
        return try {
            println("🔐 AuthenticationService: Creating user for $phoneNumber")

            val user = User(
                userId = "", // Will be generated
                phoneNumber = phoneNumber,
                phoneNumberHash = "",
                fullName = "",
                dateOfBirth = "",
                gender = "",
                panNumber = "",
                isVerified = true,
                createdAt = System.currentTimeMillis(),
                lastLoginAt = System.currentTimeMillis()
            )

            val createdUser = databaseProvider.userRepository.insertUser(user)

            // ✅ Store user ID for future updates
            databaseProvider.userSessionManager.saveCurrentUserId(createdUser.userId)
            databaseProvider.userSessionManager.saveUserSession(createdUser.userId, true)

            println("✅ User created and session saved: ${createdUser.userId}")
            createdUser
        } catch (e: Exception) {
            println("❌ Error creating user: ${e.message}")
            throw e
        }
    }

    // ✅ NEW: Update basic details using stored user ID
    suspend fun updateBasicDetails(
        fullName: String,
        dateOfBirth: String,
        gender: String,
        panNumber: String,
        creditScore: Int
    ) {
        try {
            val userId = databaseProvider.userSessionManager.getCurrentUserId()

            if (userId != null) {
                println("🔄 Using stored user ID: $userId")
                databaseProvider.userRepository.updateBasicDetails(
                    userId = userId,
                    fullName = fullName,
                    dateOfBirth = dateOfBirth,
                    gender = gender,
                    panNumber = panNumber,
                    creditScore = creditScore,
                    completed = true,
                    timestamp = System.currentTimeMillis()
                )
                println("✅ Basic details updated successfully")
            } else {
                throw Exception("No user ID found in session")
            }
        } catch (e: Exception) {
            println("❌ Error updating basic details: ${e.message}")
            throw e
        }
    }

    // ✅ NEW: Update user permissions using stored user ID
    suspend fun updateUserPermissions(
        callLog: Boolean,
        messages: Boolean,
        storage: Boolean,
        deviceInfo: Boolean,
        consentGiven: Boolean
    ) {
        try {
            val userId = databaseProvider.userSessionManager.getCurrentUserId()

            if (userId != null) {
                databaseProvider.userRepository.updateUserPermissions(
                    userId = userId,
                    callLog = callLog,
                    messages = messages,
                    storage = storage,
                    deviceInfo = deviceInfo,
                    consentGiven = consentGiven,
                    timestamp = System.currentTimeMillis()
                )
                println("✅ Permissions updated successfully")
            } else {
                throw Exception("No user ID found in session")
            }
        } catch (e: Exception) {
            println("❌ Error updating permissions: ${e.message}")
            throw e
        }
    }

    suspend fun checkUserStatus(phoneNumber: String): AuthenticationResult {
        return try {
            println("🔍 AuthenticationService: Checking user for $phoneNumber")

            val anyUser = databaseProvider.userRepository.getUserByPhoneNumber(phoneNumber)
            println("📋 Raw user: $anyUser")

            if (anyUser != null) {
                // ✅ Save user session AND user ID
                databaseProvider.userSessionManager.saveUserSession(anyUser.phoneNumber, true)
                databaseProvider.userSessionManager.saveCurrentUserId(anyUser.userId)

                val hasBasicInfo = anyUser.fullName.isNotBlank()
                val hasSignupReward = anyUser.hasReceivedSignupReward
                val basicDetailsCompleted = anyUser.basicDetailsCompleted
                val isAccountComplete = anyUser.isAccountComplete

                // ✅ Simple transaction check without Flow - use HTTP client directly
                val hasTransactions = try {
                    val httpClient = SupabaseHttpClient.getInstance()
                    val supabaseTransactions = httpClient.selectTransactions(
                        filter = "user_id=eq.${anyUser.userId}",
                        limit = 1
                    )
                    val transactionCount = supabaseTransactions.size
                    println("💰 Found $transactionCount transactions for user ${anyUser.userId}")
                    transactionCount > 0
                } catch (e: Exception) {
                    println("⚠️ Transaction check failed, assuming no transactions: ${e.message}")
                    false
                }

                println("📊 User analysis:")
                println("   - Has basic info: $hasBasicInfo")
                println("   - Basic details completed: $basicDetailsCompleted")
                println("   - Has signup reward: $hasSignupReward")
                println("   - Account marked complete: $isAccountComplete")
                println("   - Has transactions: $hasTransactions")

                // ✅ Updated logic for returning user detection
                val isReturning = hasBasicInfo && basicDetailsCompleted && isAccountComplete && (hasSignupReward || hasTransactions)

                if (isReturning) {
                    databaseProvider.userRepository.updateLastLogin(anyUser.userId, System.currentTimeMillis())
                    println("✅ Returning user detected: ${anyUser.fullName}")

                    AuthenticationResult(
                        isReturningUser = true,
                        user = anyUser,
                        shouldSkipOnboarding = true
                    )
                } else {
                    println("⚠️ User incomplete - needs onboarding")
                    println("   - Reason: basicInfo=$hasBasicInfo, completed=$basicDetailsCompleted, accountComplete=$isAccountComplete, hasReward=$hasSignupReward")
                    AuthenticationResult(
                        isReturningUser = false,
                        user = anyUser,
                        shouldSkipOnboarding = false
                    )
                }
            } else {
                println("👤 No user found - new user")
                AuthenticationResult(
                    isReturningUser = false,
                    user = null,
                    shouldSkipOnboarding = false
                )
            }
        } catch (e: Exception) {
            println("❌ Error checking user status: ${e.message}")
            e.printStackTrace()
            AuthenticationResult(
                isReturningUser = false,
                user = null,
                shouldSkipOnboarding = false
            )
        }
    }



    suspend fun completeUserOnboarding(phoneNumber: String) {
        try {
            val currentTime = System.currentTimeMillis()

            // ✅ Try to get user ID from session first
            var userId = databaseProvider.userSessionManager.getCurrentUserId()

            // Fallback to phone number lookup if no session
            if (userId == null) {
                val user = databaseProvider.userRepository.getUserByPhoneNumber(phoneNumber)
                userId = user?.userId
            }

            if (userId != null) {
                databaseProvider.userRepository.markAccountComplete(
                    userId = userId,
                    complete = true,
                    hasReward = true,
                    timestamp = currentTime
                )

                // Add signup reward transaction if not exists
                val existingReward = databaseProvider.transactionRepository.getTransactionsByType(
                    userId, "Signup Reward"
                )
                if (existingReward.isEmpty()) {
                    val signupTransaction = Transaction(
                        userId = userId,
                        type = "CREDIT",
                        amount = 500.0,
                        description = "Signup Reward",
                        timestamp = currentTime,
                        status = "SUCCESS"
                    )

                    databaseProvider.transactionRepository.insertTransaction(signupTransaction)
                    println("✅ Signup reward added for: $phoneNumber")
                }

                println("✅ Onboarding completed for user: $userId")
            } else {
                throw Exception("User not found for phone number: $phoneNumber")
            }

        } catch (e: Exception) {
            println("❌ Error completing onboarding: ${e.message}")
            e.printStackTrace()
        }
    }
}
