@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)
package com.example.insightsapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseUserCoupon(
    @SerialName("user_coupon_id") val userCouponId: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("coupon_id") val couponId: String,
    @SerialName("claimed_at") val claimedAt: String? = null
)


