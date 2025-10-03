@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)
package com.example.insightsapp.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StartSurveyRequest(
    @SerialName("user_id") val userId: String,
    @SerialName("survey_id") val surveyId: String,
    @SerialName("consent_given") val consentGiven: Boolean = true,
    @SerialName("agreement_accepted") val agreementAccepted: Boolean = true
)

@Serializable
data class SurveyResponseDto(
    @SerialName("response_id") val responseId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("survey_id") val surveyId: String,
    @SerialName("status") val status: String = "IN_PROGRESS",
    @SerialName("consent_given") val consentGiven: Boolean? = null, // ✅ Allow null
    @SerialName("agreement_accepted") val agreementAccepted: Boolean? = null, // ✅ Allow null
    @SerialName("started_at") val startedAt: Long? = null,
    @SerialName("completed_at") val completedAt: Long? = null
)

@Serializable
data class SurveyAnswerDto(
    @SerialName("question_id") val questionId: String,
    @SerialName("option_id") val optionId: String? = null,
    @SerialName("answer_text") val answerText: String? = null
)

@Serializable
data class SubmitAnswersRequest(
    @SerialName("answers") val answers: List<SurveyAnswerDto>
)
