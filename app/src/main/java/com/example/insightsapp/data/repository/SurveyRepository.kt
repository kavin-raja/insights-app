package com.example.insightsapp.data.repository

import com.example.insightsapp.data.database.Survey
import com.example.insightsapp.data.remote.RemoteSurveyDataSource
import kotlinx.coroutines.flow.Flow

class SurveyRepository(
    private val remoteDataSource: RemoteSurveyDataSource
) {

    fun getActiveSurveys(): Flow<List<Survey>> {
        return remoteDataSource.getActiveSurveys()
    }

    suspend fun getAllSurveysCount(): Int {
        return remoteDataSource.getAllSurveysCount()
    }

    suspend fun insertAllSurveys(surveys: List<Survey>) {
        remoteDataSource.insertAllSurveys(surveys)
    }
}
