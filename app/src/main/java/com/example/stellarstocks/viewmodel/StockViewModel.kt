package com.example.stellarstocks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stellarstocks.data.db.models.StockMaster
import com.example.stellarstocks.data.db.models.StockTransaction
import com.example.stellarstocks.data.db.repository.StellarStocksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.abs

enum class StockSortOption {
    MOST_RECENT,
    HIGHEST_VALUE,
    LOWEST_VALUE
}

class StockViewModel(private val repository: StellarStocksRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    private val _stock = MutableStateFlow<List<StockMaster>>(emptyList())
    val stock: StateFlow<List<StockMaster>> = _stock

    val filteredStock: StateFlow<List<StockMaster>> = combine(
        repository.getAllStock(),
        _searchQuery
    ) { stockList, query ->
        if (query.isBlank()) {
            stockList
        } else {
            stockList.filter { it.stockCode.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedStock = MutableStateFlow<StockMaster?>(null)
    val selectedStock = _selectedStock.asStateFlow()

    private val _selectedTransactions = MutableStateFlow<List<StockTransaction>>(emptyList())
    val selectedTransactions = _selectedTransactions.asStateFlow()
    private val _currentSort = MutableStateFlow(StockSortOption.MOST_RECENT)
    val currentSort = _currentSort.asStateFlow()

    val visibleTransactions: StateFlow<List<StockTransaction>> = combine(
        _selectedTransactions,
        _currentSort
    ) { transactions, sortOption ->
        when (sortOption) {
            StockSortOption.MOST_RECENT -> transactions.sortedByDescending { it.date }
            StockSortOption.HIGHEST_VALUE -> transactions.sortedByDescending { calculateTransactionValue(it) }
            StockSortOption.LOWEST_VALUE -> transactions.sortedBy { calculateTransactionValue(it) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun calculateTransactionValue(trans: StockTransaction): Double {
        val price = if (trans.transactionType == "Purchase") trans.unitCost else trans.unitSell
        // Use absolute quantity so -5 items sold counts as a "value" of 5 * price
        return price * abs(trans.qty)
    }

    private val _adjustmentSearchCode = MutableStateFlow("")
    val adjustmentSearchCode = _adjustmentSearchCode.asStateFlow()

    private val _foundAdjustmentStock = MutableStateFlow<StockMaster?>(null)
    val foundAdjustmentStock = _foundAdjustmentStock.asStateFlow()

    private val _adjustmentQty = MutableStateFlow(0)
    val adjustmentQty = _adjustmentQty.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

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

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun updateSort(option: StockSortOption) {
        _currentSort.value = option
    }

    fun selectStockForDetails(code: String) {
        viewModelScope.launch {
            _selectedStock.value = repository.getStock(code)
            repository.getStockTransactions(code).collect { transactions ->
                _selectedTransactions.value = transactions
            }
        }
    }

    fun onAdjustmentSearchChange(query: String) {
        _adjustmentSearchCode.value = query
    }

    fun searchForAdjustment() {
        viewModelScope.launch {
            val code = _adjustmentSearchCode.value.trim()
            if (code.isNotEmpty()) {
                val result = repository.getStock(code)
                if (result != null) {
                    _foundAdjustmentStock.value = result
                    _adjustmentQty.value = 0
                    _toastMessage.value = "Stock Found"
                } else {
                    _foundAdjustmentStock.value = null
                    _toastMessage.value = "Stock Code not found"
                }
            }
        }
    }

    fun onAdjustmentQtyChange(newQty: Int) {
        _adjustmentQty.value = newQty.coerceIn(-100, 100)
    }

    fun confirmAdjustment() {
        val stock = _foundAdjustmentStock.value ?: return
        val qty = _adjustmentQty.value

        if (qty == 0) {
            _toastMessage.value = "Quantity cannot be zero"
            return
        }

        viewModelScope.launch {
            val transaction = StockTransaction(
                stockCode = stock.stockCode,
                date = Date(),
                transactionType = "Adjustment",
                documentNum = (System.currentTimeMillis() % 100000).toInt(),
                qty = qty,
                unitCost = stock.cost,
                unitSell = stock.sellingPrice
            )

            repository.adjustStock(transaction)

            _toastMessage.value = "Adjustment Processed"
            _foundAdjustmentStock.value = null
            _adjustmentSearchCode.value = ""
            _adjustmentQty.value = 0

        }
    }

    fun clearToast() { _toastMessage.value = null }
}
