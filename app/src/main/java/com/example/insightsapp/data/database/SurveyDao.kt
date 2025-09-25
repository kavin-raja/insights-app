package com.example.insightsapp.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SurveyDao {
    @Insert
    suspend fun insertSurvey(survey: Survey)

    @Insert
    suspend fun insertAllSurveys(surveys: List<Survey>)

    @Query("SELECT * FROM surveys WHERE isActive = 1 ORDER BY reward DESC")
    fun getActiveSurveys(): Flow<List<Survey>>

    @Query("SELECT * FROM surveys WHERE id = :surveyId")
    suspend fun getSurveyById(surveyId: String): Survey?

    @Query("DELETE FROM surveys")
    suspend fun deleteAllSurveys()

    @Query("SELECT COUNT(*) FROM surveys WHERE isActive = 1")
    suspend fun getAllSurveysCount(): Int

}
