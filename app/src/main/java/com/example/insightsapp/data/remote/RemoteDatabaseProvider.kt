package com.example.insightsapp.data.remote

import android.content.Context
import com.example.insightsapp.data.auth.AuthenticationService
import com.example.insightsapp.data.api.CouponApiService
import com.example.insightsapp.data.repository.CouponRepository
import com.example.insightsapp.data.repository.TransactionRepository
import com.example.insightsapp.data.repository.UserRepository
import com.example.insightsapp.data.session.UserSessionManager

/**
 * Central provider that wires all remote data sources, API services and repositories
 * so that UI layers can obtain a single instance per application process.
 */
class RemoteDatabaseProvider private constructor(context: Context) {

    // ---------- Data sources ----------
    private val userDataSource = RemoteUserDataSource()
    private val transactionDataSource = RemoteTransactionDataSource()

    // ---------- API services ----------
    private val couponApiService = CouponApiService()

    // ---------- Repositories (exposed to the rest of the app) ----------
    val userRepository = UserRepository(userDataSource)
    val transactionRepository = TransactionRepository(transactionDataSource)
    val couponRepository = CouponRepository(couponApiService)
    val walletRepository = WalletRepository.instance()
    val userSessionManager = UserSessionManager(context)

    // Higher-level services
    val authenticationService = AuthenticationService(this)

    companion object {
        @Volatile
        private var INSTANCE: RemoteDatabaseProvider? = null

        fun getInstance(context: Context): RemoteDatabaseProvider {
            return INSTANCE ?: synchronized(this) {
                RemoteDatabaseProvider(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
