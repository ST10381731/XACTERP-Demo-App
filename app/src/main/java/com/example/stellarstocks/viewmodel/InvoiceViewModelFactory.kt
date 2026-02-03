package com.example.stellarstocks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stellarstocks.data.db.repository.StellarStocksRepository

class InvoiceViewModelFactory(private val repository: StellarStocksRepository) : ViewModelProvider.Factory {// factory for view model to pass repository
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InvoiceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InvoiceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}