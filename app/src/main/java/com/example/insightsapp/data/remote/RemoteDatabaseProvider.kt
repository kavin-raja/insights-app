package com.example.insightsapp.data.remote

import android.content.Context
import com.example.insightsapp.data.repository.SurveyRepository
import com.example.insightsapp.data.auth.AuthenticationService
import com.example.insightsapp.data.repository.TransactionRepository
import com.example.insightsapp.data.repository.UserRepository
import com.example.insightsapp.data.session.UserSessionManager

class RemoteDatabaseProvider(context: Context) {

    // Data Sources
    private val userDataSource = RemoteUserDataSource()
    private val transactionDataSource = RemoteTransactionDataSource()
    private val surveyDataSource = RemoteSurveyDataSource()

    // Repositories (public interface)
    val userRepository = UserRepository(userDataSource)
    val transactionRepository = TransactionRepository(transactionDataSource)
    val surveyRepository = SurveyRepository(surveyDataSource)
    val userSessionManager = UserSessionManager(context)

    val authenticationService = AuthenticationService(this)

    companion object {
        @Volatile
        private var INSTANCE: RemoteDatabaseProvider? = null

        fun getInstance(context: Context): RemoteDatabaseProvider {
            return INSTANCE ?: synchronized(this) {
                val instance = RemoteDatabaseProvider(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
