@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package com.example.insightsapp.data.remote

import com.example.insightsapp.data.remote.dto.SupabaseCoupon
import com.example.insightsapp.data.remote.dto.SupabaseUserCoupon
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.InternalSerializationApi

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
        return try {
            println("🎫 Fetching coupons...")

            val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/coupons"
            val coupons: List<SupabaseCoupon> = http.client.get(url) {
                headers {
                    append("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                    append("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                    append("Content-Profile", "public")
                }
                parameter("select", "coupon_id,title,brand,price_points,image_url")
                parameter("order", "price_points.asc")
            }.body()

            println("🎫 Found ${coupons.size} coupons")
            coupons.forEach { coupon ->
                println("   - ${coupon.title}: ${coupon.pricePoints} pts, IMG: ${coupon.imageUrl}")
            }

            coupons
        } catch (e: Exception) {
            println("❌ Error fetching coupons: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getUserCoupons(userId: String): List<UserCouponRow> {
        return try {
            println("🎫 Fetching user coupons for: $userId")

            val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/user_coupons"
            val userCoupons: List<SupabaseUserCoupon> = http.client.get(url) {
                headers {
                    append("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                    append("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                    append("Content-Profile", "public")
                }
                parameter("select", "coupon_id")
                parameter("user_id", "eq.$userId")
            }.body()

            // ✅ Convert to UserCouponRow
            val result = userCoupons.map { UserCouponRow(couponId = it.couponId) }
            println("🎫 User has ${result.size} claimed coupons")
            result

        } catch (e: Exception) {
            println("❌ Error fetching user coupons: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }


    override suspend fun redeem(userId: String, couponId: String, costPts: Int) {
        try {
            println("💳 Redeeming coupon: $couponId for user: $userId, cost: $costPts")
            http.redeemCoupon(userId, couponId, costPts)
            println("✅ Coupon redeemed successfully")
        } catch (e: Exception) {
            println("❌ Error redeeming coupon: ${e.message}")
            throw e
        }
    }
}
