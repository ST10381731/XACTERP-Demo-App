package com.example.stellarstocks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stellarstocks.data.db.repository.StellarStocksRepository

class DebtorViewModelFactory(private val repository: StellarStocksRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DebtorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DebtorViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
