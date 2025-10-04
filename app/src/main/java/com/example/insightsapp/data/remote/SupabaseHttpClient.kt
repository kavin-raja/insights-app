package com.example.insightsapp.data.remote

import com.example.insightsapp.data.remote.dto.SupabaseUser
import com.example.insightsapp.data.remote.dto.SupabaseTransaction
import com.example.insightsapp.data.remote.dto.SupabaseSurvey
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class SupabaseHttpClient {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    val client: HttpClient = HttpClient(Android) {
        install(ContentNegotiation) { json(json) }
        install(Logging) { level = LogLevel.INFO }
    }

    val baseUrl: String = "${SupabaseConfig.SUPABASE_URL}/rest/v1"
    private val apiKey = SupabaseConfig.SUPABASE_ANON_KEY

    init {
        println("🔧 SupabaseHttpClient initialized")
        println("   URL: $baseUrl")
        println("   API Key: ${apiKey.take(20)}...")

        if (apiKey == "YOUR_ACTUAL_SUPABASE_ANON_KEY_HERE" || apiKey.isBlank()) {
            println("❌ INVALID API KEY! Please update SupabaseConfig.kt with your real Supabase anon key")
        }
    }

    suspend fun selectUsers(filter: String? = null, limit: Int? = null, order: String? = null): List<SupabaseUser> {
        return try {
            val url = buildUrl("users", filter, limit, order)
            println("🌐 GET Request: $url")

            val response: HttpResponse = client.get(url) { addHeaders() }

            val responseText = response.bodyAsText()
            println("📥 Response: ${response.status}")
            println("📦 Response Body: $responseText")

            if (response.status.isSuccess()) {
                val users: List<SupabaseUser> = json.decodeFromString(responseText)
                println("👥 Found ${users.size} users")
                users
            } else {
                println("❌ Error response: $responseText")
                emptyList()
            }
        } catch (e: Exception) {
            println("❌ Error selecting users: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun selectUserByPhoneNumber(phoneNumber: String): SupabaseUser? {
        return try {
            println("🔍 Searching for phone: $phoneNumber")

            val encodedPhone = java.net.URLEncoder.encode(phoneNumber, "UTF-8")
            val url1 = "$baseUrl/users?select=*&phone_number=eq.$encodedPhone&limit=1"
            println("🌐 Method 1 URL: $url1")

            var response: HttpResponse = client.get(url1) { addHeaders() }
            var responseText = response.bodyAsText()
            println("📥 Method 1 Response: ${response.status}")
            println("📦 Method 1 Body: $responseText")

            if (response.status.isSuccess()) {
                val users: List<SupabaseUser> = json.decodeFromString(responseText)
                if (users.isNotEmpty()) return users.first()
            }

            val url2 = "$baseUrl/users?select=*&phone_number=eq.\"$phoneNumber\"&limit=1"
            println("🌐 Method 2 URL: $url2")

            response = client.get(url2) { addHeaders() }
            responseText = response.bodyAsText()
            println("📥 Method 2 Response: ${response.status}")
            println("📦 Method 2 Body: $responseText")

            if (response.status.isSuccess()) {
                val users: List<SupabaseUser> = json.decodeFromString(responseText)
                if (users.isNotEmpty()) return users.first()
            }

            val allUsers = selectUsers()
            allUsers.find { it.phone_number == phoneNumber }
        } catch (e: Exception) {
            println("❌ Error selecting user by phone: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun selectTransactions(filter: String? = null, limit: Int? = null, order: String? = null): List<SupabaseTransaction> {
        return try {
            val url = buildUrl("transactions", filter, limit, order)
            val response: HttpResponse = client.get(url) { addHeaders() }
            if (response.status.isSuccess()) {
                val responseText = response.bodyAsText()
                json.decodeFromString(responseText)
            } else {
                println("❌ Error response: ${response.bodyAsText()}")
                emptyList()
            }
        } catch (e: Exception) {
            println("❌ Error selecting transactions: ${e.message}")
            emptyList()
        }
    }

    suspend fun selectSurveys(filter: String? = null, limit: Int? = null, order: String? = null): List<SupabaseSurvey> {
        return try {
            val url = buildUrl("surveys", filter, limit, order)
            val response: HttpResponse = client.get(url) { addHeaders() }
            if (response.status.isSuccess()) {
                val responseText = response.bodyAsText()
                json.decodeFromString(responseText)
            } else {
                println("❌ Error response: ${response.bodyAsText()}")
                emptyList()
            }
        } catch (e: Exception) {
            println("❌ Error selecting surveys: ${e.message}")
            emptyList()
        }
    }

    suspend fun insertUser(user: SupabaseUser): Boolean {
        return try {
            println("📤 Inserting user: ${user.phone_number}")
            val response: HttpResponse = client.post("$baseUrl/users") {
                addHeaders()
                contentType(ContentType.Application.Json)
                setBody(user)
            }
            val responseText = response.bodyAsText()
            if (response.status.isSuccess()) true else {
                println("❌ Insert failed: $responseText"); false
            }
        } catch (e: Exception) {
            println("❌ Error inserting user: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun insertTransaction(transaction: SupabaseTransaction): Boolean {
        return try {
            val response: HttpResponse = client.post("$baseUrl/transactions") {
                addHeaders()
                contentType(ContentType.Application.Json)
                setBody(transaction)
            }
            if (response.status.isSuccess()) true else {
                println("❌ Transaction insert failed: ${response.bodyAsText()}"); false
            }
        } catch (e: Exception) {
            println("❌ Error inserting transaction: ${e.message}")
            false
        }
    }

    suspend fun insertSurvey(survey: SupabaseSurvey): Boolean {
        return try {
            val response: HttpResponse = client.post("$baseUrl/surveys") {
                addHeaders()
                contentType(ContentType.Application.Json)
                setBody(survey)
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            println("❌ Error inserting survey: ${e.message}")
            false
        }
    }

    suspend fun updateUser(updates: Map<String, Any?>, filter: String): Boolean {
        return try {
            val userId = if (filter.contains("user_id=eq.")) {
                filter.substringAfter("user_id=eq.").removeSurrounding("\"")
            } else null

            val url = if (userId != null) "$baseUrl/users?user_id=eq.$userId" else "$baseUrl/users?$filter"

            val jsonBody = buildString {
                append("{")
                updates.entries.forEachIndexed { index, (key, value) ->
                    if (index > 0) append(",")
                    append("\"$key\":")
                    when (value) {
                        is String -> append("\"$value\"")
                        is Boolean -> append(value.toString())
                        is Number -> append(value.toString())
                        null -> append("null")
                        else -> append("\"$value\"")
                    }
                }
                append("}")
            }

            val response: HttpResponse = client.patch(url) {
                addHeaders()
                contentType(ContentType.Application.Json)
                setBody(jsonBody)
            }

            val responseText = response.bodyAsText()
            when (response.status) {
                HttpStatusCode.OK -> true
                HttpStatusCode.NoContent -> {
                    if (userId != null) {
                        val verification = selectUsers(filter = "user_id=eq.$userId", limit = 1)
                        verification.isNotEmpty()
                    } else true
                }
                else -> {
                    println("❌ Update failed: $responseText")
                    false
                }
            }
        } catch (e: Exception) {
            println("❌ Error updating user: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun upsertSurvey(survey: SupabaseSurvey): Boolean {
        return try {
            val response: HttpResponse = client.post("$baseUrl/surveys") {
                addHeaders()
                contentType(ContentType.Application.Json)
                header("Prefer", "resolution=merge-duplicates")
                setBody(survey)
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            println("❌ Error upserting survey: ${e.message}")
            false
        }
    }

    suspend fun redeemCoupon(
        userId: String,
        couponId: String,
        costPts: Int
    ): Int {
        try {
            println("💳 Starting coupon redemption: $couponId for user: $userId, cost: $costPts")

            // ✅ Use proper response type - String instead of Map
            val transactionResponse: String = client.get("$baseUrl/transactions") {
                headers {
                    append("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                    append("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                    append("Content-Profile", "public")
                }
                parameter("select", "type,amount")
                parameter("user_id", "eq.$userId")
            }.bodyAsText()  // ✅ Get as text first

            val transactions: List<TxRow> = json.decodeFromString(transactionResponse)

            val currentBalance = transactions.sumOf { transaction ->
                if (transaction.type.equals("CREDIT", true)) transaction.amount else -transaction.amount
            }.toInt()

            println("💰 Current balance from transactions: $currentBalance, Required: $costPts")

            if (currentBalance < costPts) {
                throw Exception("INSUFFICIENT_BALANCE: Have $currentBalance, need $costPts")
            }

            // ✅ Insert into user_coupons - fix the table name and structure
            val couponResult = client.post("$baseUrl/user_coupons") {
                headers {
                    append("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                    append("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                    append("Content-Profile", "public")
                }
                header("Prefer", "resolution=merge-duplicates")
                contentType(ContentType.Application.Json)
                // ✅ Use JSON string instead of Map to avoid LinkedHashMap error
                setBody("""{"user_id":"$userId","coupon_id":"$couponId"}""")
            }

            println("✅ User coupon result: ${couponResult.status}")

            // ✅ Insert DEBIT transaction using JSON string
            val txnResult = client.post("$baseUrl/transactions") {
                headers {
                    append("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                    append("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                    append("Content-Profile", "public")
                }
                contentType(ContentType.Application.Json)
                // ✅ Use JSON string instead of Map
                setBody("""
                {
                    "user_id":"$userId",
                    "type":"DEBIT",
                    "amount":$costPts,
                    "description":"Coupon: $couponId",
                    "timestamp":${System.currentTimeMillis()},
                    "status":"SUCCESS"
                }
            """.trimIndent())
            }

            println("✅ Transaction result: ${txnResult.status}")

            val newBalance = currentBalance - costPts
            println("✅ Coupon redeemed successfully. New balance: $newBalance")

            return newBalance

        } catch (e: Exception) {
            println("❌ Coupon redemption failed: ${e.message}")
            throw e
        }
    }




    private fun buildUrl(table: String, filter: String?, limit: Int?, order: String?): String {
        return buildString {
            append("$baseUrl/$table")
            append("?select=*")
            filter?.let { append("&$it") }
            limit?.let { append("&limit=$it") }
            order?.let { append("&order=$it") }
        }
    }

    // Augmented: send apikey, bearer, schema headers for both read and write + Accept JSON
    private fun HttpRequestBuilder.addHeaders() {
        headers {
            append("apikey", apiKey)
            append("Authorization", "Bearer $apiKey")
            append("Accept-Profile", "public")   // for reads
            append("Content-Profile", "public")  // for writes
        }
        accept(ContentType.Application.Json)
    }

    // Optional: precise method-aware helper (not used by call sites; kept for future)
    fun HttpRequestBuilder.supabaseHeaders(
        schema: String = "public",
        bearer: String = SupabaseConfig.SUPABASE_ANON_KEY
    ) {
        header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
        header("Authorization", "Bearer $bearer")
        // PostgREST schema negotiation
        header("Accept-Profile", schema)
        header("Content-Profile", schema)
        accept(ContentType.Application.Json)
    }

    companion object {
        @Volatile private var INSTANCE: SupabaseHttpClient? = null
        fun getInstance(): SupabaseHttpClient {
            return INSTANCE ?: synchronized(this) {
                SupabaseHttpClient().also { INSTANCE = it }
            }
        }
    }
}
