package com.example.insightsapp.ui.coupons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.insightsapp.data.database.Coupon
import com.example.insightsapp.data.remote.CouponRepository
import com.example.insightsapp.data.remote.UserCouponRow
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
                println("🔍 CouponsViewModel: Starting load for phone: $phoneNumber")
                val user = databaseProvider.userRepository.getUserByPhoneNumber(phoneNumber)
                println("🔍 CouponsViewModel: User lookup result = ${user?.userId}")

                if (user != null) {
                    currentUserId = user.userId

                    // Load coupons with individual error handling
                    val dtos: List<SupabaseCoupon> = try {
                        println("🔍 Loading coupons from repository...")
                        couponRepo.getCoupons()
                    } catch (e: Exception) {
                        println("❌ Failed to load coupons: ${e.message}")
                        e.printStackTrace()
                        emptyList()
                    }

                    // Load user's claimed coupons with error handling
                    val claimedRows: List<UserCouponRow> = try {
                        println("🔍 Loading claimed coupons for user: ${user.userId}")
                        couponRepo.getUserCoupons(user.userId)
                    } catch (e: Exception) {
                        println("❌ Failed to load user coupons: ${e.message}")
                        e.printStackTrace()
                        emptyList()
                    }

                    val claimedIds: Set<String> = claimedRows.map { it.couponId }.toSet()

                    // Load wallet balance with error handling
                    val balancePoints: Double = try {
                        println("🔍 Loading wallet balance for user: ${user.userId}")
                        walletRepo.currentPoints(user.userId)
                    } catch (e: Exception) {
                        println("❌ Failed to load wallet balance: ${e.message}")
                        e.printStackTrace()
                        0.0
                    }

                    println("✅ CouponsViewModel: Loaded ${dtos.size} coupons, ${claimedIds.size} claimed, balance: $balancePoints")
                    setFromDto(dtos, claimedIds, balancePoints)

                } else {
                    println("❌ CouponsViewModel: User not found for phone: $phoneNumber")
                    _coupons.value = emptyList()
                }

            } catch (e: Exception) {
                println("❌ CouponsViewModel: Critical error in loadCoupons()")
                println("❌ Error: ${e.message}")
                e.printStackTrace()
                _coupons.value = emptyList()
            } finally {
                _isLoading.value = false
                println("✅ CouponsViewModel: Loading completed, isLoading = false")
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
            if (url.isNullOrEmpty()) return@mapNotNull null

            Coupon(
                id = dto.couponId,
                title = dto.title,
                points = dto.pricePoints,
                imageUrl = url,
                claimed = dto.couponId in claimedIds,
                canBuy = !claimedIds.contains(dto.couponId) && dto.pricePoints <= balancePoints.toInt()
            )
        }
    }

    fun applyBalance(balancePoints: Double) {
        _coupons.update { list ->
            list.map { coupon ->
                coupon.copy(canBuy = !coupon.claimed && coupon.points <= balancePoints.toInt())
            }
        }
    }

    fun claim(id: String) {
        val selected = _coupons.value.firstOrNull { it.id == id } ?: return
        if (selected.claimed || !selected.canBuy) return

        _coupons.update { list ->
            list.map { if (it.id == id) it.copy(claimed = true, canBuy = false) else it }
        }
    }
}

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
