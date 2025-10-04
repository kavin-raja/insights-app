package com.example.insightsapp.data.repository

import com.example.insightsapp.data.api.SurveyApiService
import com.example.insightsapp.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SurveyRepository(
    public val apiService: SurveyApiService = SurveyApiService.getInstance()
) {

    suspend fun getSurveys(): Flow<List<com.example.insightsapp.data.database.Survey>> = flow {
        try {
            val apiSurveys = apiService.getSurveys()
            val surveys = apiSurveys.map { it.toSurvey() }
            emit(surveys)
        } catch (e: Exception) {
            println("❌ Error in getSurveys: ${e.message}")
            e.printStackTrace()
            emit(emptyList()) // Always emit something to prevent flow abortion
        }
    }


    suspend fun getSurveyWithQuestions(surveyId: String): SurveyWithQuestions? {
        return apiService.getSurveyWithQuestions(surveyId)
    }

    suspend fun startSurvey(userId: String, surveyId: String): SurveyResponseDto? {
        val request = StartSurveyRequest(
            userId = userId,
            surveyId = surveyId,
            consentGiven = true,
            agreementAccepted = true
        )
        println("StartSurvey API payload: $request")
        return apiService.startSurvey(request)
    }

    suspend fun submitAnswers(responseId: String, answers: Map<String, String>): Boolean {
        val answerDtos = answers.map { (questionId, optionId) ->
            SurveyAnswerDto(
                questionId = questionId,
                optionId = optionId
            )
        }

        val submitted = apiService.submitAnswers(responseId, answerDtos)

        if (submitted) {
            apiService.completeSurvey(responseId)
        }

        return submitted
    }

    companion object {
        @Volatile
        private var INSTANCE: SurveyRepository? = null

        fun getInstance(): SurveyRepository {
            return INSTANCE ?: synchronized(this) {
                SurveyRepository().also { INSTANCE = it }
            }
        }
    }
}
