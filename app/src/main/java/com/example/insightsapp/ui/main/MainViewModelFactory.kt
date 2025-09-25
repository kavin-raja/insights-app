package com.example.insightsapp.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.insightsapp.data.database.AppDatabase

class MainViewModelFactory(
    private val database: AppDatabase,
    private val phoneNumber: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(database, phoneNumber) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
