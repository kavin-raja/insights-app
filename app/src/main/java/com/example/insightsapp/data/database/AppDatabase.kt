package com.example.insightsapp.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [User::class, Transaction::class, Survey::class],
    version = 3, // ✅ Increment from 2 to 3
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun surveyDao(): SurveyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "insights_database"
                )
                    .fallbackToDestructiveMigration() // ✅ For development
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
