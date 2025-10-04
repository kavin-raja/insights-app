package com.example.insightsapp.ui.coupons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.insightsapp.data.database.Coupon
import com.example.insightsapp.data.remote.CouponRepository
import com.example.insightsapp.data.remote.WalletRepository
import com.example.insightsapp.data.remote.RemoteDatabaseProvider
import com.example.insightsapp.data.remote.dto.SupabaseCoupon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CouponsViewModel(
    private val databaseProvider: RemoteDatabaseProvider,
    private val phoneNumber: String
) : ViewModel() {

    private val _coupons = MutableStateFlow<List<Coupon>>(emptyList())
    val coupons = _coupons.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val couponRepo = CouponRepository.instance()
    private val walletRepo = WalletRepository.instance()
    private var currentUserId: String? = null

    init {
        loadCoupons()
    }

    private fun loadCoupons() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true

            try {
                println("🎫 CouponsViewModel: Loading coupons for $phoneNumber")

                // ✅ Get user the same way as WalletViewModel
                val user = databaseProvider.userRepository.getUserByPhoneNumber(phoneNumber)

                if (user != null) {
                    currentUserId = user.userId
                    println("👤 CouponsViewModel: Found user ${user.userId} (same as WalletViewModel)")

                    // ✅ Fetch coupons and user's claimed coupons
                    val dtos: List<SupabaseCoupon> = couponRepo.getCoupons()
                    val claimedIds: Set<String> = couponRepo.getUserCoupons(user.userId)
                        .map { it.couponId }.toSet()

                    // ✅ Get current balance using the CORRECT user ID
                    val balancePoints: Double = walletRepo.currentPoints(user.userId)
                    println("💰 CouponsViewModel: Balance = $balancePoints for user ${user.userId}")

                    // ✅ Set initial data
                    setFromDto(dtos, claimedIds, balancePoints)

                    // ✅ IMPORTANT: Set loading to false BEFORE starting infinite flow
                    _isLoading.value = false

                    // ✅ Start real-time balance monitoring in a separate launch
                    //startBalanceMonitoring(user.userId)

                } else {
                    println("❌ CouponsViewModel: User not found for $phoneNumber")
                    _isLoading.value = false
                }

            } catch (e: Exception) {
                println("❌ CouponsViewModel: Error: ${e.message}")
                e.printStackTrace()
                _isLoading.value = false
            }
            // ✅ Remove finally block since we handle _isLoading in each branch
        }
    }

    private fun startBalanceMonitoring(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // ✅ This runs in a separate coroutine, won't block loading
                walletRepo.pointsFlow(userId).collect { pts ->
                    println("💰 CouponsViewModel: Balance updated to $pts")
                    applyBalance(pts)
                }
            } catch (e: Exception) {
                println("❌ Balance monitoring error: ${e.message}")
            }
        }
    }

    fun setFromDto(
        dtos: List<SupabaseCoupon>,
        claimedIds: Set<String> = emptySet(),
        balancePoints: Double = 0.0
    ) {
        _coupons.value = dtos.mapNotNull { dto ->
            val url = dto.imageUrl?.trim()
            if (url.isNullOrEmpty()) {
                println("⚠️ Skipping coupon ${dto.title} - no image URL")
                return@mapNotNull null
            }

            val coupon = Coupon(
                id = dto.couponId,
                title = dto.title,
                points = dto.pricePoints,
                imageUrl = url,
                claimed = dto.couponId in claimedIds,
                canBuy = !claimedIds.contains(dto.couponId) && dto.pricePoints <= balancePoints.toInt()
            )

            println("✅ Coupon: ${coupon.title} - ${coupon.points} pts - Can buy: ${coupon.canBuy}")
            coupon
        }

        println("🎫 Total coupons: ${_coupons.value.size}")
    }

    fun applyBalance(balancePoints: Double) {
        _coupons.update { list ->
            list.map { coupon ->
                val canBuy = !coupon.claimed && coupon.points <= balancePoints.toInt()
                coupon.copy(canBuy = canBuy)
            }
        }
    }

    fun claim(id: String) {
        val currentList = _coupons.value
        val selected = currentList.firstOrNull { it.id == id } ?: return

        if (selected.claimed || !selected.canBuy) {
            println("⚠️ Cannot claim coupon: claimed=${selected.claimed}, canBuy=${selected.canBuy}")
            return
        }

        println("💳 Claiming coupon: ${selected.title} for ${selected.points} points")

        // ✅ Optimistically update UI
        _coupons.update { list ->
            list.map { if (it.id == id) it.copy(claimed = true, canBuy = false) else it }
        }

        // ✅ Don't call the broken redeemCoupon - let MainScreen handle it
    }
}

// ✅ ViewModelFactory
class CouponsViewModelFactory(
    private val databaseProvider: RemoteDatabaseProvider,
    private val phoneNumber: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CouponsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CouponsViewModel(databaseProvider, phoneNumber) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
