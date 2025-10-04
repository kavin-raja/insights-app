package com.example.insightsapp.data.api

import com.example.insightsapp.data.model.UserCouponsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CouponApiService @Inject constructor() {

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

    suspend fun getUserCoupons(
        userId: String,
        page: Int = 0,
        size: Int = 10
    ): Result<UserCouponsResponse> {
        return try {
            val url = "${ApiConfig.BASE_URL}/api/v1/users/$userId/coupons?page=$page&size=$size"
            println("🎫 API Request: GET $url")

            val response: HttpResponse = client.get(url)
            println("🎫 API Response Code: ${response.status.value}")

            if (response.status == HttpStatusCode.OK) {

                val couponsResponse: UserCouponsResponse = response.body()
                println("🎫 Parsed ${couponsResponse.content.size} coupons")
                Result.success(couponsResponse)
            } else {
                val error = "API Error: ${response.status.value}"
                println("❌ $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            println("❌ Exception in getUserCoupons: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}