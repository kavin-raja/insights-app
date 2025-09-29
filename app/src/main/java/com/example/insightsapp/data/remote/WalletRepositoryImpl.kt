@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package com.example.insightsapp.data.remote

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface WalletRepository {
    suspend fun currentPoints(userId: String): Int
    fun pointsFlow(userId: String): Flow<Int>
    companion object { fun instance(): WalletRepository = WalletRepositoryImpl }
}

@Serializable
private data class TxRow(
    @SerialName("type") val type: String,
    @SerialName("amount") val amount: Int
)

private object WalletRepositoryImpl : WalletRepository {

    private val http = SupabaseHttpClient.getInstance()

    override suspend fun currentPoints(userId: String): Int {
        val url = "${http.baseUrl}/transactions"
        val rows: List<TxRow> = http.client.get(url) {
            with(http) { supabaseHeaders() }
            parameter("select", "type,amount")
            parameter("user_id", "eq.$userId")
        }.body()

        return rows.sumOf { if (it.type.equals("CREDIT", true)) it.amount else -it.amount }
    }

    override fun pointsFlow(userId: String): Flow<Int> = flow {
        while (true) {
            emit(currentPoints(userId))
            kotlinx.coroutines.delay(2_000)
        }
    }
}
