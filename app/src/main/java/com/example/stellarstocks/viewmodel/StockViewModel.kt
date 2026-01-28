package com.example.stellarstocks.viewmodel

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stellarstocks.data.db.models.DebtorMaster
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

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode = _isEditMode.asStateFlow()

    private val _stockCode = MutableStateFlow("")
    val stockCode = _stockCode.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _cost = MutableStateFlow(0.0)
    val cost = _cost.asStateFlow()

    private val _sellingPrice = MutableStateFlow(0.0)
    val sellingPrice = _sellingPrice.asStateFlow()

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

    fun toggleMode() {
        _isEditMode.value = !_isEditMode.value
        clearForm()
        if (!_isEditMode.value) generateNewCode()
    }

    fun onStockCodeChange(newValue: String) { _stockCode.value = newValue }
    fun onDescriptionChange(newValue: String) { _description.value = newValue }
    fun onCostChange(newValue: Double) { _cost.value = newValue }
    fun onSellingPriceChange(newValue: Double) { _sellingPrice.value = newValue }

    fun generateNewCode() {
        val currentList = filteredStock.value
        if (currentList.isEmpty()) { _stockCode.value = "STK001"; return }
        try {
            val maxId = currentList.mapNotNull { it.stockCode.removePrefix("STK").toIntOrNull() }.maxOrNull() ?: 0
            _stockCode.value = "STK" + String.format("%03d", maxId + 1)
        } catch (_: Exception) { _stockCode.value = "STK001" }
    }

    fun searchStock() {
        viewModelScope.launch {
            val code = _stockCode.value.trim()
            val stock = repository.getStock(code)
            if (stock != null) {
                _description.value = stock.stockDescription
                _cost.value = stock.cost
                _sellingPrice.value = stock.sellingPrice
                _toastMessage.value = "Stock Found"
            } else { _toastMessage.value = "Stock not found" }
        }
    }

    fun saveStock() {
        viewModelScope.launch {
            if (_description.value.isBlank() || _description.value.isDigitsOnly() ||_cost.value == 0.0 || _sellingPrice.value == 0.0) { _toastMessage.value = "Please fill in the required fields (*)"; return@launch }
            val stock = StockMaster(
                stockCode = _stockCode.value, stockDescription = _description.value,
                cost = _cost.value, sellingPrice = _sellingPrice.value, stockOnHand = 0
            , qtyPurchased = 0, qtySold = 0, totalPurchasesExclVat = 0.0, totalSalesExclVat = 0.0)
            if (_isEditMode.value) {
                val existing = repository.getStock(_stockCode.value)
                if (existing != null) repository.insertStock(stock.copy(stockOnHand = existing.stockOnHand, qtyPurchased = existing.qtyPurchased, qtySold = existing.qtySold, totalPurchasesExclVat = existing.totalPurchasesExclVat, totalSalesExclVat = existing.totalSalesExclVat))
            } else { repository.insertStock(stock) }
            _toastMessage.value = "Saved Successfully"
            if (!_isEditMode.value) { clearForm(); generateNewCode() }
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

    fun deleteStock() {
        viewModelScope.launch {
            val existing = repository.getStock(_stockCode.value)
            if (existing != null) { repository.deleteStock(existing); _toastMessage.value = "Deleted"; clearForm() }
        }
    }

    fun clearToast() { _toastMessage.value = null }

    private fun clearForm() { _description.value = ""; _cost.value = 0.0; _sellingPrice.value = 0.0; _stockCode.value = "" }
}
