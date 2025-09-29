@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package com.example.insightsapp.data.remote

import com.example.insightsapp.data.remote.dto.SupabaseCoupon
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface CouponRepository {
    suspend fun getCoupons(): List<SupabaseCoupon>
    suspend fun getUserCoupons(userId: String): List<UserCouponRow>
    suspend fun redeem(userId: String, couponId: String, costPts: Int)
    companion object { fun instance(): CouponRepository = CouponRepositoryImpl }
}

@Serializable
data class UserCouponRow(
    @SerialName("coupon_id") val couponId: String
)

private object CouponRepositoryImpl : CouponRepository {

    private val http = SupabaseHttpClient.getInstance()

    override suspend fun getCoupons(): List<SupabaseCoupon> {
        val url = "${http.baseUrl}/coupons"
        return http.client.get(url) {
            with(http) { supabaseHeaders() }
            parameter("select", "coupon_id,title,brand,price_points,image_url")
            parameter("order", "price_points.asc")
        }.body()
    }

    override suspend fun getUserCoupons(userId: String): List<UserCouponRow> {
        val url = "${http.baseUrl}/user_coupons"
        return http.client.get(url) {
            with(http) { supabaseHeaders() }
            parameter("select", "coupon_id")
            parameter("user_id", "eq.$userId")
        }.body()
    }

    override suspend fun redeem(userId: String, couponId: String, costPts: Int) {
        http.redeemCoupon(userId, couponId, costPts)  // ← single atomic helper
    }
}
