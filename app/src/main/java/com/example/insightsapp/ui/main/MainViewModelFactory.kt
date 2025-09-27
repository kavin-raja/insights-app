package com.example.insightsapp.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.insightsapp.data.remote.RemoteDatabaseProvider

class MainViewModelFactory(
    private val databaseProvider: RemoteDatabaseProvider,
    private val phoneNumber: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(databaseProvider, phoneNumber) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
