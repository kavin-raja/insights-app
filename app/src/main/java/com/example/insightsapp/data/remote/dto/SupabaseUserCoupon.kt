@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)
package com.example.insightsapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.example.insightsapp.data.database.UserCoupon

@Serializable
data class SupabaseUserCoupon(
    @SerialName("user_coupon_id") val userCouponId: String? = null,  // ✅ Add this field
    @SerialName("user_id") val userId: String? = null,
    @SerialName("coupon_id") val couponId: String,  // ✅ Keep as String - Supabase handles UUID conversion
    @SerialName("claimed_at") val claimedAt: String? = null
)

//fun SupabaseUserCoupon.toUserCoupon(): UserCoupon {
//    return UserCoupon(
//        userId = this.userId,
//        couponId = this.couponId,
//        claimedAt = System.currentTimeMillis()
//    )
//}

//fun UserCoupon.toSupabaseUserCoupon(): SupabaseUserCoupon {
//    return SupabaseUserCoupon(
//        userId = this.userId,
//        couponId = this.couponId,
//        claimedAt = null // Let database handle timestamp
//    )
//}
