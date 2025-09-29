package com.example.insightsapp.ui.coupons
import androidx.annotation.DrawableRes
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class Coupon(
    val id: String,
    val title: String,
    val points: Int,
    val imageUrl: String,
    val claimed: Boolean = false,
    val canBuy: Boolean = true
)


