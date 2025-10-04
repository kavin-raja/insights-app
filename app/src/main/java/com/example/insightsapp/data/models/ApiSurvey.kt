@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)
package com.example.insightsapp.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiSurvey(
    @SerialName("survey_id") val surveyId: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String? = null,
    @SerialName("brand") val brand: String,
    @SerialName("purpose") val purpose: String,
    @SerialName("data_points") val dataPoints: List<String> = emptyList(),
    @SerialName("reward_points") val rewardPoints: Int = 0,
    @SerialName("estimated_time_minutes") val estimatedTimeMinutes: Int = 5,
    @SerialName("status") val status: String = "ACTIVE"
) {
    // Convert to your existing Survey model
    fun toSurvey(): com.example.insightsapp.data.database.Survey {
        return com.example.insightsapp.data.database.Survey(
            surveyId = surveyId,
            title = title,
            description = description ?: "",
            brandName = brand,
            reward = rewardPoints.toDouble(),
            durationMinutes = estimatedTimeMinutes,
            isActive = status == "ACTIVE",
            purpose = purpose,
            dataPoints = dataPoints,
            rewardPoints = rewardPoints
        )
    }
}

@Serializable
data class SurveyQuestion(
    @SerialName("question_id") val questionId: String,
    @SerialName("survey_id") val surveyId: String,
    @SerialName("question_text") val questionText: String,
    @SerialName("question_type") val questionType: String = "SINGLE_CHOICE",
    @SerialName("question_order") val questionOrder: Int,
    @SerialName("is_required") val isRequired: Boolean = true,
    @SerialName("options") val options: List<QuestionOption> = emptyList()
)

@Serializable
data class QuestionOption(
    @SerialName("option_id") val optionId: String,
    @SerialName("question_id") val questionId: String,
    @SerialName("option_text") val optionText: String,
    @SerialName("option_order") val optionOrder: Int
)

@Serializable
data class SurveyWithQuestions(
    val survey: ApiSurvey,
    val questions: List<SurveyQuestion>
)
