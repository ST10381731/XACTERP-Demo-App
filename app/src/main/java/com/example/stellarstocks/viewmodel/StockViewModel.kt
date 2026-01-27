package com.example.stellarstocks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stellarstocks.data.db.models.StockMaster
import com.example.stellarstocks.data.db.models.StockTransaction
import com.example.stellarstocks.data.db.repository.StellarStocksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StockViewModel(private val repository: StellarStocksRepository) : ViewModel() {

    private val _stock = MutableStateFlow<List<StockMaster>>(emptyList())
    val stock: StateFlow<List<StockMaster>> = _stock

    private val _selectedStock = MutableStateFlow<StockMaster?>(null)
    val selectedStock = _selectedStock.asStateFlow()

    private val _selectedTransactions = MutableStateFlow<List<StockTransaction>>(emptyList())
    val selectedTransactions = _selectedTransactions.asStateFlow()

    init {
        fetchStock()
    }

    private fun fetchStock() {
        viewModelScope.launch {
            repository.getAllStock().collect { stockList ->
                _stock.value = stockList
            }
        }
    }

    /*fun selectStockForDetails(code: String) {
        viewModelScope.launch {
            _selectedStock.value = repository.getStock(code)

            repository.getStockTransactions(code).collect { transactions ->
                _selectedTransactions.value = transactions
            }
        }
    }*/
}