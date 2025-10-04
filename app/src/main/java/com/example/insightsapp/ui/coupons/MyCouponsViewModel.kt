package com.example.insightsapp.ui.coupons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.insightsapp.data.model.UserCoupon
import com.example.insightsapp.data.remote.GetUserCouponsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MyCouponsUiState(
    val isLoading: Boolean = false,
    val coupons: List<UserCoupon> = emptyList(),
    val error: String? = null,
    val isRefreshing: Boolean = false
)

class MyCouponsViewModel(
    private val getUserCouponsUseCase: GetUserCouponsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyCouponsUiState())
    val uiState: StateFlow<MyCouponsUiState> = _uiState.asStateFlow()

    fun loadCoupons(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            getUserCouponsUseCase(userId).collect { result ->
                result.fold(
                    onSuccess = { response ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            coupons = response.content,
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Unknown error occurred"
                        )
                    }
                )
            }
        }
    }

    fun refreshCoupons(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)

            getUserCouponsUseCase(userId).collect { result ->
                result.fold(
                    onSuccess = { response ->
                        _uiState.value = _uiState.value.copy(
                            isRefreshing = false,
                            coupons = response.content,
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isRefreshing = false,
                            error = exception.message ?: "Unknown error occurred"
                        )
                    }
                )
            }
        }
    }
}
