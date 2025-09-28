package com.example.insightsapp.data.remote

import com.example.insightsapp.data.remote.dto.SupabaseUser
import com.example.insightsapp.data.remote.dto.SupabaseTransaction
import com.example.insightsapp.data.remote.dto.SupabaseSurvey
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.net.URLEncoder

class SupabaseHttpClient {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    private val baseUrl = SupabaseConfig.SUPABASE_URL + "/rest/v1"
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

            val response: HttpResponse = client.get(url) {
                addHeaders()
            }

            val responseText = response.bodyAsText()
            println("📥 Response: ${response.status}")
            println("📦 Response Body: $responseText")

            if (response.status.isSuccess()) {
                val users: List<SupabaseUser> = json.decodeFromString(responseText)
                println("👥 Found ${users.size} users")
                users.forEach { user ->
                    println("   - User: ${user.phone_number} (ID: ${user.user_id})")
                }
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

            // ✅ Method 1: Try with URL encoding
            val encodedPhone = java.net.URLEncoder.encode(phoneNumber, "UTF-8")
            val url1 = "$baseUrl/users?select=*&phone_number=eq.$encodedPhone&limit=1"

            println("🌐 Method 1 URL: $url1")

            var response: HttpResponse = client.get(url1) {
                addHeaders()
            }

            var responseText = response.bodyAsText()
            println("📥 Method 1 Response: ${response.status}")
            println("📦 Method 1 Body: $responseText")

            if (response.status.isSuccess()) {
                val users: List<SupabaseUser> = json.decodeFromString(responseText)
                if (users.isNotEmpty()) {
                    val user = users.first()
                    println("✅ Found user with Method 1: ${user.phone_number} (ID: ${user.user_id})")
                    return user
                }
            }

            // ✅ Method 2: Try without encoding but with quotes
            val url2 = "$baseUrl/users?select=*&phone_number=eq.\"$phoneNumber\"&limit=1"
            println("🌐 Method 2 URL: $url2")

            response = client.get(url2) {
                addHeaders()
            }

            responseText = response.bodyAsText()
            println("📥 Method 2 Response: ${response.status}")
            println("📦 Method 2 Body: $responseText")

            if (response.status.isSuccess()) {
                val users: List<SupabaseUser> = json.decodeFromString(responseText)
                if (users.isNotEmpty()) {
                    val user = users.first()
                    println("✅ Found user with Method 2: ${user.phone_number} (ID: ${user.user_id})")
                    return user
                }
            }

            // ✅ Method 3: Get all users and filter manually (fallback)
            println("🌐 Method 3: Manual filtering")
            val allUsers = selectUsers()
            val matchedUser = allUsers.find { it.phone_number == phoneNumber }

            if (matchedUser != null) {
                println("✅ Found user with Method 3: ${matchedUser.phone_number} (ID: ${matchedUser.user_id})")
                return matchedUser
            }

            println("❌ No user found with any method for phone: $phoneNumber")
            return null

        } catch (e: Exception) {
            println("❌ Error selecting user by phone: ${e.message}")
            e.printStackTrace()
            null
        }
    }


    suspend fun selectTransactions(filter: String? = null, limit: Int? = null, order: String? = null): List<SupabaseTransaction> {
        return try {
            val url = buildUrl("transactions", filter, limit, order)
            val response: HttpResponse = client.get(url) {
                addHeaders()
            }

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
            val response: HttpResponse = client.get(url) {
                addHeaders()
            }

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
            println("📤 User ID: ${user.user_id}")
            println("📤 Full Name: ${user.full_name}")

            val response: HttpResponse = client.post("$baseUrl/users") {
                addHeaders()
                contentType(ContentType.Application.Json)
                setBody(user)
            }

            val responseText = response.bodyAsText()
            println("📥 Insert Response: ${response.status}")
            println("📦 Insert Body: $responseText")

            if (response.status.isSuccess()) {
                println("✅ User inserted successfully")
                true
            } else {
                println("❌ Insert failed: $responseText")
                false
            }
        } catch (e: Exception) {
            println("❌ Error inserting user: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun insertTransaction(transaction: SupabaseTransaction): Boolean {
        return try {
            println("📤 Inserting transaction: ${transaction.description} for ${transaction.user_id}")

            val response: HttpResponse = client.post("$baseUrl/transactions") {
                addHeaders()
                contentType(ContentType.Application.Json)
                setBody(transaction)
            }

            val responseText = response.bodyAsText()
            println("📥 Transaction Response: ${response.status}")

            if (response.status.isSuccess()) {
                println("✅ Transaction inserted successfully")
                true
            } else {
                println("❌ Transaction insert failed: $responseText")
                false
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
            println("🔄 Updating user with filter: $filter")
            println("🔄 Updates: $updates")

            // ✅ Extract user_id from filter for primary key updates
            val userId = if (filter.contains("user_id=eq.")) {
                filter.substringAfter("user_id=eq.").removeSurrounding("\"")
            } else null

            val url = if (userId != null) {
                // ✅ Use primary key syntax for user_id updates
                "$baseUrl/users?user_id=eq.$userId"
            } else {
                "$baseUrl/users?$filter"
            }

            println("🌐 Update URL: $url")

            // ✅ Create JSON manually to avoid LinkedHashMap serialization issues
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

            println("🔄 JSON Body: $jsonBody")

            val response: HttpResponse = client.patch(url) {
                addHeaders()
                contentType(ContentType.Application.Json)
                setBody(jsonBody)
            }

            val responseText = response.bodyAsText()
            println("📥 Update Response: ${response.status}")
            println("📦 Update Body: $responseText")

            when (response.status) {
                HttpStatusCode.OK -> {
                    println("✅ User updated successfully (200 OK)")
                    true
                }
                HttpStatusCode.NoContent -> {
                    // ✅ Check if it's actually no content or no rows affected
                    if (userId != null) {
                        // Verify the update by checking if user exists
                        val verification = selectUsers(filter = "user_id=eq.$userId", limit = 1)
                        if (verification.isNotEmpty()) {
                            println("✅ User updated successfully (204 with verification)")
                            true
                        } else {
                            println("❌ Update failed: User not found with ID: $userId")
                            false
                        }
                    } else {
                        println("✅ User updated successfully (204 No Content)")
                        true
                    }
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

    private fun buildUrl(table: String, filter: String?, limit: Int?, order: String?): String {
        return buildString {
            append("$baseUrl/$table")
            append("?select=*")
            filter?.let { append("&$it") }
            limit?.let { append("&limit=$it") }
            order?.let { append("&order=$it") }
        }
    }

    private fun HttpRequestBuilder.addHeaders() {
        headers {
            append("apikey", apiKey)
            append("Authorization", "Bearer $apiKey")
            append("Content-Profile", "public")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SupabaseHttpClient? = null

        fun getInstance(): SupabaseHttpClient {
            return INSTANCE ?: synchronized(this) {
                val instance = SupabaseHttpClient()
                INSTANCE = instance
                instance
            }
        }
    }
}
