package com.example.insightsapp.data.database

data class Coupon(
    val id: String,
    val title: String,
    val points: Int,
    val imageUrl: String,
    val claimed: Boolean = false,
    val canBuy: Boolean = true
)


