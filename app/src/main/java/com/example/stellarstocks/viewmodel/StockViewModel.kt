package com.example.stellarstocks.viewmodel

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stellarstocks.data.db.models.StockMaster
import com.example.stellarstocks.data.db.models.StockTransaction
import com.example.stellarstocks.data.db.models.TransactionInfo
import com.example.stellarstocks.data.db.repository.StellarStocksRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.abs

enum class StockSortOption { // Sort options for stock
    FULL_LIST,
    RECENT_DEBTOR_ONLY,
    HIGHEST_QUANTITY,
    LOWEST_QUANTITY
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
            stockList.filter {
                it.stockCode.contains(query, ignoreCase = true) ||
                it.stockDescription.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedStock = MutableStateFlow<StockMaster?>(null)
    val selectedStock = _selectedStock.asStateFlow()

    private val _selectedTransactions = MutableStateFlow<List<TransactionInfo>>(emptyList())

    private val _currentSort = MutableStateFlow(StockSortOption.FULL_LIST)
    val currentSort = _currentSort.asStateFlow()

    val visibleTransactions: StateFlow<List<TransactionInfo>> = combine(
        _selectedTransactions,
        _currentSort
    ) { transactions, sortOption ->
        when (sortOption) {
            StockSortOption.FULL_LIST -> transactions.sortedByDescending { it.date }
            StockSortOption.RECENT_DEBTOR_ONLY -> transactions.filter { it.accountCode != null }.sortedByDescending { it.date }
            StockSortOption.HIGHEST_QUANTITY -> transactions.sortedByDescending { abs(it.qty) }
            StockSortOption.LOWEST_QUANTITY -> transactions.sortedBy { abs(it.qty) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _adjustmentSearchCode = MutableStateFlow("")
    val adjustmentSearchCode = _adjustmentSearchCode.asStateFlow()

    private val _foundAdjustmentStock = MutableStateFlow<StockMaster?>(null)
    val foundAdjustmentStock = _foundAdjustmentStock.asStateFlow()

    private val _adjustmentQty = MutableStateFlow(0)
    val adjustmentQty = _adjustmentQty.asStateFlow()

    private val _adjustmentType = MutableStateFlow("Adjustment")
    val adjustmentType = _adjustmentType.asStateFlow()

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

    private val _navigationChannel = Channel<Boolean>()
    val navigationChannel = _navigationChannel.receiveAsFlow()

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

    fun resetSearch() {
        _searchQuery.value = ""
    }

    fun updateSort(option: StockSortOption) {
        _currentSort.value = option
    }

    fun selectStockForDetails(code: String) {
        viewModelScope.launch {
            _selectedStock.value = repository.getStock(code)

            repository.getTransactionInfoForStock(code).collect { transactions ->
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
        viewModelScope.launch {
            val lastCode = repository.getLastStockCode()

            if (lastCode == null) {
                _stockCode.value = "STK001"
            } else {
                try {
                    val number = lastCode.removePrefix("STK").toIntOrNull() ?: 0
                    _stockCode.value = "STK" + String.format("%03d", number + 1)
                } catch (_: Exception) {
                    _stockCode.value = "STK001"
                }
            }
        }
    }

    fun saveStock() {
        viewModelScope.launch {
            if (_description.value.isBlank()) {
                _toastMessage.value = "Description required"
                return@launch
            }

            val stock = StockMaster(
                stockCode = _stockCode.value,
                stockDescription = _description.value,
                cost = _cost.value,
                sellingPrice = _sellingPrice.value,
                stockOnHand = 0,
                qtyPurchased = 0,
                qtySold = 0,
                totalPurchasesExclVat = 0.0,
                totalSalesExclVat = 0.0
            )

            if (_isEditMode.value) {
                val existing = repository.getStock(_stockCode.value)
                if (existing != null) {
                    repository.updateStock(stock.copy(
                        stockOnHand = existing.stockOnHand,
                        qtyPurchased = existing.qtyPurchased,
                        qtySold = existing.qtySold,
                        totalPurchasesExclVat = existing.totalPurchasesExclVat,
                        totalSalesExclVat = existing.totalSalesExclVat,
                        isActive = existing.isActive
                    ))
                    _toastMessage.value = "Stock Updated"
                    _navigationChannel.send(true)
                }
            } else {
                repository.insertStock(stock)
                _toastMessage.value = "Stock Created"
                _navigationChannel.send(true)
            }
        }
    }

    fun setAdjustmentType(type: String) {
        _adjustmentType.value = type
    }

    fun onAdjustmentSearchChange(query: String) {
        _adjustmentSearchCode.value = query
    }

    fun onStockSelectedForAdjustment(stock: StockMaster) {
        _foundAdjustmentStock.value = stock
        _adjustmentSearchCode.value = stock.stockCode
        _adjustmentQty.value = 0
    }

    fun onAdjustmentQtyChange(newQty: Int) {
        _adjustmentQty.value = newQty
    }

    fun confirmAdjustment() {
        val stock = _foundAdjustmentStock.value ?: return
        val qty = _adjustmentQty.value
        val type = _adjustmentType.value

        if (qty == 0) {
            _toastMessage.value = "Quantity cannot be zero"
            return
        }

        if (type == "Purchase" && qty < 0) {// Purchases cannot be negative
            _toastMessage.value = "Purchase quantity must be positive"
            return
        }

        viewModelScope.launch {
            val transaction = StockTransaction(
                stockCode = stock.stockCode,
                date = Date(),
                transactionType = type,
                documentNum = (System.currentTimeMillis() % 100000).toInt(),
                qty = qty,
                unitCost = stock.cost,
                unitSell = stock.sellingPrice
            )

            repository.adjustStock(transaction)

            _toastMessage.value = "$type Processed"
            _foundAdjustmentStock.value = null
            _adjustmentSearchCode.value = ""
            _adjustmentQty.value = 0

            _adjustmentType.value = "Adjustment"
        }
    }

    fun deleteStock() {
        viewModelScope.launch {
            repository.deleteStock(_stockCode.value)

            _toastMessage.value = "Stock Deleted"
            clearForm()
            _navigationChannel.send(true)
        }
    }

    fun clearToast() { _toastMessage.value = null }

    private fun clearForm() { _description.value = ""; _cost.value = 0.0; _sellingPrice.value = 0.0; _stockCode.value = "" }
}