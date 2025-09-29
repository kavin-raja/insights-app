@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package com.example.insightsapp.data.remote

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.InternalSerializationApi

interface WalletRepository {
    suspend fun currentPoints(userId: String): Double  // ✅ Changed to Double
    fun pointsFlow(userId: String): Flow<Double>       // ✅ Changed to Double
    companion object { fun instance(): WalletRepository = WalletRepositoryImpl }
}

@Serializable
data class TxRow(
    @SerialName("type") val type: String,
    @SerialName("amount") val amount: Double  // ✅ Double to match database
)

private object WalletRepositoryImpl : WalletRepository {

    override suspend fun currentPoints(userId: String): Double {
        return try {
            println("💰 WalletRepository: Fetching balance for user $userId")

            val httpClient = SupabaseHttpClient.getInstance()
            val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/transactions"

            val rows: List<TxRow> = httpClient.client.get(url) {
                headers {
                    append("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                    append("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                    append("Content-Profile", "public")
                }
                parameter("select", "type,amount")
                parameter("user_id", "eq.$userId")
            }.body()

            val balance = rows.sumOf {
                if (it.type.equals("CREDIT", true)) it.amount else -it.amount
            }

            println("💰 WalletRepository: Balance = $balance from ${rows.size} transactions")
            balance

        } catch (e: Exception) {
            println("❌ WalletRepository error: ${e.message}")
            e.printStackTrace()
            0.0  // ✅ Return Double instead of Int
        }
    }

    override fun pointsFlow(userId: String): Flow<Double> = flow {
        println("🔄 WalletRepository: Starting real-time balance flow for $userId")
        while (true) {
            try {
                val balance = currentPoints(userId)
                emit(balance)
                kotlinx.coroutines.delay(3_000) // ✅ Poll every 3 seconds
            } catch (e: Exception) {
                println("❌ Flow error: ${e.message}")
                emit(0.0) // ✅ Emit Double on error
                kotlinx.coroutines.delay(5_000) // Wait longer on error
            }
        }
    }
}
