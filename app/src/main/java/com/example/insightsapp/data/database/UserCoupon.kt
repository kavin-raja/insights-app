package com.example.insightsapp.data.database

data class UserCoupon(
    val id: Int = 0,
    val userId: String,
    val couponId: String,
    val claimedAt: Long = System.currentTimeMillis()
)