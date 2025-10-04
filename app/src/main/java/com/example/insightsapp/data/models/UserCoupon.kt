@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)
package com.example.insightsapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserCoupon(
    @SerialName("userCouponId") val userCouponId: Int,
    @SerialName("title") val title: String? = null,
    @SerialName("brand") val brand: String? = null,
    @SerialName("imageUrl") val imageUrl: String? = null,
    @SerialName("pricePoints") val pricePoints: Int? = null,
    @SerialName("claimedAt") val claimedAt: Long,
    @SerialName("couponCode") val couponCode: String,
    @SerialName("status") val status: String,
    @SerialName("expiresAt") val expiresAt: Long
)

@Serializable
data class UserCouponsResponse(
    @SerialName("content") val content: List<UserCoupon>,
    @SerialName("totalElements") val totalElements: Long,
    @SerialName("totalPages") val totalPages: Int,
    @SerialName("size") val size: Int,
    @SerialName("number") val number: Int,
    @SerialName("first") val first: Boolean,
    @SerialName("last") val last: Boolean
)
