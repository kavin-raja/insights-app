package com.example.insightsapp.data.auth

import com.example.insightsapp.data.database.AppDatabase
import com.example.insightsapp.data.database.Transaction
import com.example.insightsapp.data.database.User
import kotlinx.coroutines.flow.first

data class AuthenticationResult(
    val isReturningUser: Boolean,
    val user: User?,
    val shouldSkipOnboarding: Boolean
)

class AuthenticationService(
    private val database: AppDatabase
) {

    suspend fun checkUserStatus(phoneNumber: String): AuthenticationResult {
        return try {
            val anyUser = database.userDao().getUserByPhoneNumber(phoneNumber)
            println("🔍 AuthenticationService: Checking user for $phoneNumber")
            println("📋 Raw user  $anyUser")

            if (anyUser != null) {
                val hasBasicInfo = anyUser.fullName.isNotBlank()
                val hasSignupReward = anyUser.hasReceivedSignupReward
                val hasTransactions = database.transactionDao().getTransactionsByPhoneNumber(phoneNumber).first().isEmpty()
                val basicDetailsCompleted = anyUser.basicDetailsCompleted

                println("📊 User analysis:")
                println("   - Has basic info: $hasBasicInfo")
                println("   - Basic details completed: $basicDetailsCompleted")
                println("   - Has signup reward: $hasSignupReward")
                println("   - Account marked complete: ${anyUser.isAccountComplete}")
                println("   - Has transactions: $hasTransactions")

                // ✅ STRICT: Only returning user if they have ALL completed onboarding
                val isReturning = hasBasicInfo && basicDetailsCompleted && (hasSignupReward || hasTransactions)

                if (isReturning) {
                    database.userDao().updateLastLogin(phoneNumber, System.currentTimeMillis())
                    println("✅ Returning user detected: ${anyUser.fullName}")

                    AuthenticationResult(
                        isReturningUser = true,
                        user = anyUser,
                        shouldSkipOnboarding = true
                    )
                } else {
                    println("⚠️ User incomplete - needs onboarding")
                    println("   Missing: ${if (!hasBasicInfo) "basic info, " else ""}${if (!basicDetailsCompleted) "basic details completion, " else ""}${if (!hasSignupReward && !hasTransactions) "rewards" else ""}")
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

            // ✅ Mark account as complete ONLY after basic details are done
            database.userDao().markAccountComplete(
                phoneNumber = phoneNumber,
                complete = true,
                hasReward = true,
                timestamp = currentTime
            )

            // Add signup reward transaction if not exists
            val existingReward = database.transactionDao().getTransactionsByType(phoneNumber, "Signup Reward")
            if (existingReward.isEmpty()) {
                val signupTransaction = Transaction(
                    phoneNumber = phoneNumber,
                    type = "CREDIT",
                    amount = 500.0,
                    description = "Signup Reward",
                    timestamp = currentTime,
                    status = "SUCCESS"
                )

                database.transactionDao().insertTransaction(signupTransaction)
                println("✅ Signup reward added for: $phoneNumber")
            }

        } catch (e: Exception) {
            println("❌ Error completing onboarding: ${e.message}")
            e.printStackTrace()
        }
    }
}
