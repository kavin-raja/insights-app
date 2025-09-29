package com.example.insightsapp.data.remote

import com.example.insightsapp.data.database.Survey
import com.example.insightsapp.data.remote.dto.toSupabaseSurvey
import com.example.insightsapp.data.remote.dto.toSurvey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RemoteSurveyDataSource(
    private val httpClient: SupabaseHttpClient = SupabaseHttpClient.getInstance()
) {

    fun getActiveSurveys(): Flow<List<Survey>> = flow {
        try {
            println("🔍 Fetching active surveys...")

            val result = httpClient.selectSurveys(
                filter = "is_active=eq.true",
                order = "created_at.desc"
            )

            val surveys = result.map { survey -> survey.toSurvey() }
            println("📋 Found ${surveys.size} active surveys")
            emit(surveys)
        } catch (e: Exception) {
            println("❌ Error fetching surveys: ${e.message}")
            e.printStackTrace()
            emit(emptyList())
        }
    }

    suspend fun getAllSurveysCount(): Int {
        return try {
            val result = httpClient.selectSurveys(
                filter = "is_active=eq.true"
            )

            result.size
        } catch (e: Exception) {
            println("❌ Error getting survey count: ${e.message}")
            0
        }
    }

    suspend fun insertAllSurveys(surveys: List<Survey>) {
        try {
            val supabaseSurveys = surveys.map { survey -> survey.toSupabaseSurvey() }

            for (survey in supabaseSurveys) {
                try {
                    val success = httpClient.upsertSurvey(survey)
                    if (!success) {
                        println("⚠️ Warning: Failed to insert survey ${survey.survey_id}")
                    }
                } catch (e: Exception) {
                    println("⚠️ Error inserting survey ${survey.survey_id}: ${e.message}")
                    // Continue with other surveys
                }
            }

            println("✅ Surveys initialized: ${surveys.size} surveys")
        } catch (e: Exception) {
            println("❌ Error inserting surveys: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}
