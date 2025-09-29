package com.example.insightsapp.ui.coupons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.insightsapp.data.remote.CouponRepository
import com.example.insightsapp.data.remote.WalletRepository
import com.example.insightsapp.data.remote.UserRepository
import com.example.insightsapp.data.remote.dto.SupabaseCoupon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CouponsViewModel(
    private val couponRepo: CouponRepository = CouponRepository.instance(),
    private val walletRepo: WalletRepository = WalletRepository.instance(),
    private val userRepo: UserRepository = UserRepository.instance()
) : ViewModel() {

    private val _coupons = MutableStateFlow<List<Coupon>>(emptyList())
    val coupons = _coupons.asStateFlow()

    init {
        // Initial load and real-time balance updates
        viewModelScope.launch(Dispatchers.IO) {
            val userId = userRepo.currentUserId()

            val dtos: List<SupabaseCoupon> = couponRepo.getCoupons()
            val claimedIds: Set<String> =
                couponRepo.getUserCoupons(userId).map { it.couponId }.toSet()
            val balancePoints: Int = walletRepo.currentPoints(userId)

            setFromDto(dtos, claimedIds, balancePoints)
            walletRepo.pointsFlow(userId).collect { pts ->
                applyBalance(pts)
            }
        }
    }

    fun setFromDto(
        dtos: List<SupabaseCoupon>,
        claimedIds: Set<String> = emptySet(),
        balancePoints: Int = Int.MAX_VALUE
    ) {
        _coupons.value = dtos.mapNotNull { dto ->
            val url = dto.imageUrl?.trim().orEmpty()
            if (url.isEmpty()) return@mapNotNull null

            Coupon(
                id = dto.couponId,
                title = dto.title,
                points = dto.pricePoints,
                imageUrl = url,
                claimed = dto.couponId in claimedIds,
                canBuy = dto.pricePoints <= balancePoints
            )
        }
    }

    fun applyBalance(balancePoints: Int) {
        _coupons.update { list ->
            list.map { it.copy(canBuy = !it.claimed && it.points <= balancePoints) }
        }
    }
    fun claim(id: String) {
        val currentList = _coupons.value
        val selected = currentList.firstOrNull { it.id == id } ?: return
        if (selected.claimed || !selected.canBuy) return

        _coupons.update { list ->
            list.map { if (it.id == id) it.copy(claimed = true, canBuy = false) else it }
        }

        viewModelScope.launch(Dispatchers.IO) {
            val userId = userRepo.currentUserId()
            runCatching {
                couponRepo.redeem(
                    userId = userId,
                    couponId = selected.id,
                    costPts = selected.points
                )
            }.onFailure {
                _coupons.update { list ->
                    list.map { if (it.id == id) it.copy(claimed = false) else it }
                }
            }
        }
    }
}
