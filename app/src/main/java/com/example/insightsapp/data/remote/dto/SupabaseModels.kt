@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package com.example.insightsapp.data.remote.dto

import com.example.insightsapp.data.database.User
import com.example.insightsapp.data.database.Transaction
import com.example.insightsapp.data.database.Survey
import kotlinx.serialization.Serializable
import kotlinx.serialization.InternalSerializationApi

@Serializable
data class SupabaseUser(
    val user_id: String,
    val phone_number: String,
    val phone_number_hash: String?,
    val full_name: String? = null,        // ✅ Make nullable
    val date_of_birth: String? = null,    // ✅ Make nullable
    val gender: String? = null,           // ✅ Make nullable
    val pan_number: String? = null,       // ✅ Make nullable
    val is_verified: Boolean = false,
    val created_at: Long,
    val last_login_at: Long,
    val has_call_log_permission: Boolean = false,
    val has_messages_permission: Boolean = false,
    val has_storage_permission: Boolean = false,
    val has_device_info_permission: Boolean = false,
    val privacy_consent_given: Boolean = false,
    val privacy_consent_timestamp: Long? = null,
    val basic_details_completed: Boolean = false,
    val basic_details_timestamp: Long? = null,
    val credit_score: Int = 0,
    val credit_score_provider: String? = null,
    val credit_score_last_updated: Long? = null,
    val credit_score_status: String? = null,
    val is_account_complete: Boolean = false,
    val has_received_signup_reward: Boolean = false,
    val signup_reward_timestamp: Long? = null
)

@Serializable
data class SupabaseTransaction(
    val transaction_id: String,
    val user_id: String,
    val type: String,
    val amount: Double,
    val description: String,
    val timestamp: Long,
    val status: String = "SUCCESS"
)

@Serializable
data class SupabaseSurvey(
    val survey_id: String,
    val title: String,
    val description: String?,
    val brand_name: String?,
    val reward: Double,
    val duration_minutes: Int?,
    val is_active: Boolean = true
)

// ✅ Extension functions
fun SupabaseUser.toUser(): User = User(
    userId = user_id,
    phoneNumber = phone_number,
    phoneNumberHash = phone_number_hash ?: "",
    fullName = full_name ?: "",                    // ✅ Handle null
    dateOfBirth = date_of_birth ?: "",             // ✅ Handle null
    gender = gender ?: "",                         // ✅ Handle null
    panNumber = pan_number ?: "",                  // ✅ Handle null
    isVerified = is_verified,
    createdAt = created_at,
    lastLoginAt = last_login_at,
    hasCallLogPermission = has_call_log_permission,
    hasMessagesPermission = has_messages_permission,
    hasStoragePermission = has_storage_permission,
    hasDeviceInfoPermission = has_device_info_permission,
    privacyConsentGiven = privacy_consent_given,
    privacyConsentTimestamp = privacy_consent_timestamp ?: 0L,
    basicDetailsCompleted = basic_details_completed,
    basicDetailsTimestamp = basic_details_timestamp ?: 0L,
    creditScore = credit_score,
    creditScoreProvider = credit_score_provider ?: "",
    creditScoreLastUpdated = credit_score_last_updated ?: 0L,
    creditScoreStatus = credit_score_status ?: "",
    isAccountComplete = is_account_complete,
    hasReceivedSignupReward = has_received_signup_reward,
    signupRewardTimestamp = signup_reward_timestamp ?: 0L
)

fun User.toSupabaseUser(): SupabaseUser = SupabaseUser(
    user_id = userId,
    phone_number = phoneNumber,
    phone_number_hash = phoneNumberHash,
    full_name = if (fullName.isBlank()) null else fullName,       // ✅ Send null if empty
    date_of_birth = if (dateOfBirth.isBlank()) null else dateOfBirth,
    gender = if (gender.isBlank()) null else gender,
    pan_number = if (panNumber.isBlank()) null else panNumber,
    is_verified = isVerified,
    created_at = createdAt,
    last_login_at = lastLoginAt,
    has_call_log_permission = hasCallLogPermission,
    has_messages_permission = hasMessagesPermission,
    has_storage_permission = hasStoragePermission,
    has_device_info_permission = hasDeviceInfoPermission,
    privacy_consent_given = privacyConsentGiven,
    privacy_consent_timestamp = if (privacyConsentTimestamp == 0L) null else privacyConsentTimestamp,
    basic_details_completed = basicDetailsCompleted,
    basic_details_timestamp = if (basicDetailsTimestamp == 0L) null else basicDetailsTimestamp,
    credit_score = creditScore,
    credit_score_provider = if (creditScoreProvider.isBlank()) null else creditScoreProvider,
    credit_score_last_updated = if (creditScoreLastUpdated == 0L) null else creditScoreLastUpdated,
    credit_score_status = if (creditScoreStatus.isBlank()) null else creditScoreStatus,
    is_account_complete = isAccountComplete,
    has_received_signup_reward = hasReceivedSignupReward,
    signup_reward_timestamp = if (signupRewardTimestamp == 0L) null else signupRewardTimestamp
)

fun SupabaseTransaction.toTransaction(): Transaction = Transaction(
    transactionId = transaction_id,
    userId = user_id,
    type = type,
    amount = amount,
    description = description,
    timestamp = timestamp,
    status = status
)

fun Transaction.toSupabaseTransaction(): SupabaseTransaction = SupabaseTransaction(
    transaction_id = transactionId,
    user_id = userId,
    type = type,
    amount = amount,
    description = description,
    timestamp = timestamp,
    status = status
)

fun SupabaseSurvey.toSurvey(): Survey = Survey(
    surveyId = survey_id,
    title = title,
    description = description ?: "",
    brandName = brand_name ?: "",
    reward = reward,
    durationMinutes = duration_minutes ?: 0,
    isActive = is_active
)

fun Survey.toSupabaseSurvey(): SupabaseSurvey = SupabaseSurvey(
    survey_id = surveyId,
    title = title,
    description = if (description.isBlank()) null else description,
    brand_name = if (brandName.isBlank()) null else brandName,
    reward = reward,
    duration_minutes = if (durationMinutes == 0) null else durationMinutes,
    is_active = isActive
)
