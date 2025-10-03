package com.example.insightsapp.data.api

import com.example.insightsapp.data.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class SurveyApiService {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                encodeDefaults = true
                isLenient = true
            })
        }
        install(Logging) {
            level = LogLevel.BODY
        }
    }

    suspend fun getSurveys(): List<ApiSurvey> {
        return try {
            println("🔄 Fetching surveys from API")
            val response: List<ApiSurvey> = client.get("${ApiConfig.BASE_URL}/api/v1/surveys") {
                headers {
                    // Add auth headers if needed
                }
            }.body()

            println("✅ Fetched ${response.size} surveys")
            response
        } catch (e: Exception) {
            println("❌ Error fetching surveys: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getSurveyWithQuestions(surveyId: String): SurveyWithQuestions? {
        return try {
            println("🔄 Fetching survey details: $surveyId")

            val survey: ApiSurvey = client.get("${ApiConfig.BASE_URL}/api/v1/surveys/$surveyId").body()
            val questions: List<SurveyQuestion> = client.get("${ApiConfig.BASE_URL}/api/v1/surveys/$surveyId/questions").body()

            println("✅ Fetched survey with ${questions.size} questions")
            SurveyWithQuestions(survey, questions)
        } catch (e: Exception) {
            println("❌ Error fetching survey details: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun startSurvey(request: StartSurveyRequest): SurveyResponseDto? {
        return try {
            println("🔄 Starting survey: ${request.surveyId} for user: ${request.userId}")
            println("Payload: $request")
            println("Payload in JSON: ${Json.encodeToString(request)}")
            val response: SurveyResponseDto = client.post("${ApiConfig.BASE_URL}/api/v1/survey-responses") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            println("✅ Started survey, response ID: ${response.responseId}")
            response
        } catch (e: Exception) {
            println("❌ Error starting survey: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun submitAnswers(responseId: String, answers: List<SurveyAnswerDto>): Boolean {
        return try {
            println("🔄 Submitting ${answers.size} answers for response: $responseId")

            val request = SubmitAnswersRequest(answers)

            client.post("${ApiConfig.BASE_URL}/api/v1/survey-responses/$responseId/answers") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            println("✅ Submitted answers successfully")
            true
        } catch (e: Exception) {
            println("❌ Error submitting answers: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun completeSurvey(responseId: String): Boolean {
        return try {
            println("🔄 Completing survey response: $responseId")

            client.post("${ApiConfig.BASE_URL}/api/v1/survey-responses/$responseId/complete") {
                contentType(ContentType.Application.Json)
            }

            println("✅ Survey completed successfully")
            true
        } catch (e: Exception) {
            println("❌ Error completing survey: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SurveyApiService? = null

        fun getInstance(): SurveyApiService {
            return INSTANCE ?: synchronized(this) {
                SurveyApiService().also { INSTANCE = it }
            }
        }
    }
}
