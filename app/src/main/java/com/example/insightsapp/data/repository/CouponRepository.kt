package com.example.insightsapp.data.repository

import com.example.insightsapp.data.api.CouponApiService
import com.example.insightsapp.data.model.UserCouponsResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CouponRepository @Inject constructor(
    private val couponApiService: CouponApiService
) {

    suspend fun getUserCoupons(userId: String, page: Int = 0, size: Int = 10): Flow<Result<UserCouponsResponse>> = flow {
        try {
            val result = couponApiService.getUserCoupons(userId, page, size)
            emit(result)
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
