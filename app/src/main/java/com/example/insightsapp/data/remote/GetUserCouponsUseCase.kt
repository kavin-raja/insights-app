package com.example.insightsapp.data.remote

import com.example.insightsapp.data.repository.CouponRepository
import com.example.insightsapp.data.model.UserCouponsResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserCouponsUseCase @Inject constructor(
    private val couponRepository: CouponRepository
) {
    suspend operator fun invoke(userId: String, page: Int = 0, size: Int = 10): Flow<Result<UserCouponsResponse>> {
        return couponRepository.getUserCoupons(userId, page, size)
    }
}
