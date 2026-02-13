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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.abs

enum class StockSortOption { // Sort options for stock transaction table
    FULL_LIST,
    RECENT_DEBTOR_ONLY,
    HIGHEST_QUANTITY,
    LOWEST_QUANTITY
}

enum class StockListSortOption { // sort options for stock enquiry
    CODE_ASC,
    CODE_DESC,
    QTY_DESC,
    QTY_ASC,
    COST_DESC,
    COST_ASC
}

class StockViewModel(private val repository: StellarStocksRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("") // user input for searching
    val searchQuery = _searchQuery.asStateFlow()

    private val _stockListSort = MutableStateFlow(StockListSortOption.CODE_ASC) // Sort option for stock enquiry
    val stockListSort = _stockListSort.asStateFlow()

    private val _stock = MutableStateFlow<List<StockMaster>>(emptyList()) // List of stock
    val stock: StateFlow<List<StockMaster>> = _stock

    //The search updates instantly when input changes
    val filteredStock: StateFlow<List<StockMaster>> = combine(
        // Combine stock, search query, and sort option to filter and sort stock
        repository.getAllStock(),
        _searchQuery,
        _stockListSort
    ) { stockList: List<StockMaster>, query: String, sortOption: StockListSortOption ->
        //combine all three flows into one

        val filtered = if (query.isBlank()) { // If search query is blank, return all stock
            stockList
        } else {
            stockList.filter { // if search query is not blank return stock that matches query
                it.stockCode.contains(query, ignoreCase = true) ||
                        it.stockDescription.contains(query, ignoreCase = true)
            }
        }

        when (sortOption) { // sort stock based on sort option
            StockListSortOption.CODE_ASC -> filtered.sortedBy { it.stockCode }
            StockListSortOption.CODE_DESC -> filtered.sortedByDescending { it.stockCode }
            StockListSortOption.QTY_DESC -> filtered.sortedByDescending { it.stockOnHand }
            StockListSortOption.QTY_ASC -> filtered.sortedBy { it.stockOnHand }
            StockListSortOption.COST_DESC -> filtered.sortedByDescending { it.cost }
            StockListSortOption.COST_ASC -> filtered.sortedBy { it.cost }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedStock = MutableStateFlow<StockMaster?>(null) // Selected stock for details
    val selectedStock = _selectedStock.asStateFlow()

    private val _selectedTransactions = MutableStateFlow<List<TransactionInfo>>(emptyList())
    // List of transactions for selected stock

    private val _currentSort = MutableStateFlow(StockSortOption.FULL_LIST)
    // current sort option for transactions
    val currentSort = _currentSort.asStateFlow()

    val visibleTransactions: StateFlow<List<TransactionInfo>> = combine(
        // Combine transactions and sort option to filter and sort transactions
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
        //continuously collect data from the flow and display new values to the UI
        initialValue = emptyList()
    )

    private val _adjustmentSearchCode = MutableStateFlow("") // Search query for stock adjustment
    val adjustmentSearchCode = _adjustmentSearchCode.asStateFlow()

    private val _foundAdjustmentStock = MutableStateFlow<StockMaster?>(null) // selected stock for adjustment
    val foundAdjustmentStock = _foundAdjustmentStock.asStateFlow()

    private val _adjustmentQty = MutableStateFlow(0) // adjustment quantity
    val adjustmentQty = _adjustmentQty.asStateFlow()

    private val _adjustmentType = MutableStateFlow("Adjustment") // Transaction type for adjustment
    val adjustmentType = _adjustmentType.asStateFlow()


    private val _isEditMode = MutableStateFlow(false) // toggle edit or creation mode for stock
    val isEditMode = _isEditMode.asStateFlow()

    private val _stockCode = MutableStateFlow("") // Stock code
    val stockCode = _stockCode.asStateFlow()

    private val _description = MutableStateFlow("") // stock description
    val description = _description.asStateFlow()

    private val _cost = MutableStateFlow(0.0) // stock cost
    val cost = _cost.asStateFlow()

    private val _sellingPrice = MutableStateFlow(0.0) // stock selling price
    val sellingPrice = _sellingPrice.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null) // Toast message
    val toastMessage = _toastMessage.asStateFlow()

    private val _navigationChannel = Channel<Boolean>() // Channel for navigation
    val navigationChannel = _navigationChannel.receiveAsFlow()

    private fun fetchStock() { // Fetch stock from repository
        viewModelScope.launch {
            repository.getAllStock().collect { stockList -> // Collect stock list from repository
                _stock.value = stockList
            }
        }
    }

    init {
        fetchStock() // Fetch stock from repository
    }

    fun onSearchQueryChange(query: String) { // Update search query
        _searchQuery.value = query
    }

    fun resetSearch() { // Reset search query
        _searchQuery.value = ""
    }

    fun updateStockListSort(option: StockListSortOption) { // function to update stock list sort
        _stockListSort.value = option
    }

    fun updateSort(option: StockSortOption) { // Update sort option
        _currentSort.value = option
    }

    fun selectStockForDetails(code: String) { // Select stock for details
        viewModelScope.launch {
            _selectedStock.value = repository.getStock(code) // get stock from repository that matches code

            repository.getTransactionInfoForStock(code).collect { transactions -> // collect transactions from repository that match code
                _selectedTransactions.value = transactions
            }
        }
    }

    fun toggleMode() { // Toggle edit or creation mode
        _isEditMode.value = !_isEditMode.value
        clearForm()
        if (!_isEditMode.value) generateNewCode() // Generate new code if in creation mode
    }

    fun onStockCodeChange(newValue: String) { _stockCode.value = newValue } // Update stock code
    fun onDescriptionChange(newValue: String) { _description.value = newValue.take(50) } // Update stock description
    fun onCostChange(newValue: Double) { _cost.value = newValue } // Update stock cost
    fun onSellingPriceChange(newValue: Double) { _sellingPrice.value = newValue } // Update stock selling price

    fun generateNewCode() { // Generate new stock code
        viewModelScope.launch {
            val lastCode = repository.getLastStockCode() // Get last stock code from repository

            if (lastCode == null) { // If no stock code, set to STK001
                _stockCode.value = "STK001"
            } else {
                try { // Try to parse last code and increment
                    val number = lastCode.removePrefix("STK").toIntOrNull() ?: 0
                    _stockCode.value = "STK" + String.format("%03d", number + 1)
                } catch (_: Exception) {
                    _stockCode.value = "STK001"
                }
            }
        }
    }

    fun saveStock() { // Save stock to repository
        viewModelScope.launch {
            if (_description.value.isBlank()) { // Description cannot be blank
                _toastMessage.value = "Description cannot be blank"
                return@launch
            }

            if (description.value.isDigitsOnly()){ // Description cannot be all numbers
                _toastMessage.value = "Description cannot be all numbers"
                return@launch
            }

            if (_cost.value ==0.0 || _sellingPrice.value == 0.0){ // Cost and sell price cannot be zero
                _toastMessage.value = "Cost and Sell price cannot be zero"
                return@launch
            }

            if (_cost.value <0.0 || _sellingPrice.value < 0.0){ // Cost and sell price cannot be negative
                _toastMessage.value = "Cost and Sell price cannot be negative"
                return@launch
            }

            if (_cost.value > _sellingPrice.value){ // Cost cannot be greater than sell price
                _toastMessage.value = "Selling price must be higher than cost"
                return@launch
            }

            val stock = StockMaster( // Create stock object
                stockCode = _stockCode.value,
                stockDescription = _description.value.trim(), // prevents text ending in whitespace
                cost = _cost.value,
                sellingPrice = _sellingPrice.value,
                stockOnHand = 0,
                qtyPurchased = 0,
                qtySold = 0,
                totalPurchasesExclVat = 0.0,
                totalSalesExclVat = 0.0
            )

            if (_isEditMode.value) { // If in edit mode, update existing stock
                val existing = repository.getStock(_stockCode.value) // Get existing stock from repository
                if (existing != null) { // If stock exists, update it
                    repository.updateStock(stock.copy(
                        stockOnHand = existing.stockOnHand,
                        qtyPurchased = existing.qtyPurchased,
                        qtySold = existing.qtySold,
                        totalPurchasesExclVat = existing.totalPurchasesExclVat,
                        totalSalesExclVat = existing.totalSalesExclVat,
                        isActive = existing.isActive
                    ))
                    _toastMessage.value = "Stock Updated"
                    clearForm()
                    _navigationChannel.send(true) // Redirect back to stock enquiry page
                }
            } else {
                repository.insertStock(stock) // If not in edit mode, insert new stock
                _toastMessage.value = "Stock Created"
                clearForm()
                _navigationChannel.send(true) // Redirect back to stock enquiry page
            }
        }
    }

    fun setAdjustmentType(type: String) { // set adjustment type
        _adjustmentType.value = type
    }

    fun onAdjustmentSearchChange(query: String) { // update search query in stock adjustment
        _adjustmentSearchCode.value = query
    }

    fun onStockSelectedForAdjustment(stock: StockMaster) { // set selected stock for adjustment
        _foundAdjustmentStock.value = stock
        _adjustmentSearchCode.value = stock.stockCode
        _adjustmentQty.value = 0
    }

    fun onAdjustmentQtyChange(newQty: Int) { // update adjustment quantity
        _adjustmentQty.value = newQty
    }

    fun confirmAdjustment() { //confirm adjustment and update stock
        val stock = _foundAdjustmentStock.value ?: return // If no stock, do nothing
        val qty = _adjustmentQty.value
        val type = _adjustmentType.value

        if (qty == 0) { // Quantity cannot be zero
            _toastMessage.value = "Quantity cannot be zero"
            return
        }

        if (type == "Purchase" && qty < 0) { // Purchases cannot be negative
            _toastMessage.value = "Purchase quantity must be positive"
            return
        }

        if (type == "Adjustment" && (stock.stockOnHand + qty) < 0) {
            // cannot drop stock on hand to negative
            _toastMessage.value = "Insufficient stock! Current: ${stock.stockOnHand}"
            return
        }

        viewModelScope.launch { // If valid, adjust stock
            val nextDocNum = repository.getNextAdjustmentNum()
            val transaction = StockTransaction(
                stockCode = stock.stockCode,
                date = Date(),
                transactionType = type,
                documentNum = nextDocNum,
                qty = qty,
                unitCost = stock.cost,
                unitSell = stock.sellingPrice
            )

            repository.adjustStock(transaction) // Adjust stock in repository

            _toastMessage.value = "$type Processed"

            _foundAdjustmentStock.value = null // Clear form
            _adjustmentSearchCode.value = ""
            _adjustmentQty.value = 0

            _adjustmentType.value = "Adjustment"
        }
    }

    fun deleteStock() { // delete stock
        viewModelScope.launch {
            if (_stockCode.value.isNotBlank()) { // If stock code is not blank, delete stock
                repository.deleteStock(_stockCode.value)

                _toastMessage.value = "Stock Deleted"
                clearForm()
                _navigationChannel.send(true)
            } else {
                _toastMessage.value = "No stock selected"}
        }
    }

    fun clearToast() { _toastMessage.value = null } // function to clear toast

    private fun clearForm() { //function to clear stock creation form
    _description.value = ""
    _cost.value = 0.0
    _sellingPrice.value = 0.0
    _stockCode.value = "" }

    val popularStock= repository.getAllStock().map { list -> // get all stock and sort by qty sold for landing page table
        list.sortedByDescending { it.qtySold }.take(5)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

}