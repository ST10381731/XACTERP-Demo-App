package com.example.stellarstocks.viewmodel

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stellarstocks.data.db.models.DebtorMaster
import com.example.stellarstocks.data.db.models.DebtorTransactionInfo
import com.example.stellarstocks.data.db.models.StockMaster
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
import kotlin.collections.emptyList

enum class SortOption {
    FULL_LIST,
    RECENT_ITEM_SOLD,
    HIGHEST_VALUE,
    LOWEST_VALUE
}
class DebtorViewModel(private val repository: StellarStocksRepository) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    private val _currentSort = MutableStateFlow(SortOption.FULL_LIST)
    val currentSort = _currentSort.asStateFlow()

    val filteredDebtors: StateFlow<List<DebtorMaster>> = combine(
        repository.getAllDebtors(),
        _searchQuery
    ) { debtors, query ->
        if (query.isBlank()) {
            debtors
        } else {
            debtors.filter {
                it.accountCode.contains(query, ignoreCase = true) ||
                        it.name.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedDebtor = MutableStateFlow<DebtorMaster?>(null)
    val selectedDebtor = _selectedDebtor.asStateFlow()

    private val _selectedTransactions = MutableStateFlow<List<DebtorTransactionInfo>>(emptyList())

    val visibleTransactions: StateFlow<List<DebtorTransactionInfo>> = combine(
        _selectedTransactions,
        _currentSort
    ) { transactions, sortOption ->
        when (sortOption) {
            SortOption.FULL_LIST -> transactions.sortedByDescending { it.date }
            SortOption.RECENT_ITEM_SOLD -> transactions.filter { it.items != null }.sortedByDescending { it.date }
            SortOption.HIGHEST_VALUE -> transactions.sortedByDescending { it.value }
            SortOption.LOWEST_VALUE -> transactions.sortedBy { it.value }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode = _isEditMode.asStateFlow()

    private val _accountCode = MutableStateFlow("")
    val accountCode = _accountCode.asStateFlow()

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _address1 = MutableStateFlow("")
    val address1 = _address1.asStateFlow()

    private val _address2 = MutableStateFlow("")
    val address2 = _address2.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    private val _navigationChannel = Channel<Boolean>()
    val navigationChannel = _navigationChannel.receiveAsFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun selectDebtorForDetails(code: String) {
        viewModelScope.launch {
            // Get Debtor Master Details
            _selectedDebtor.value = repository.getDebtor(code)

            // Get Debtor Transaction History
            repository.getDebtorTransactionInfo(code).collect { transactions ->
                _selectedTransactions.value = transactions
            }
        }
    }

    fun updateSort(option: SortOption) {
        _currentSort.value = option
    }

    fun toggleMode() {
        _isEditMode.value = !_isEditMode.value
        clearForm()
        if (!_isEditMode.value) generateNewCode()
    }
    fun onSearchCodeChange(newValue: String) { _accountCode.value = newValue }
    fun onNameChange(newValue: String) { _name.value = newValue }
    fun onAddress1Change(newValue: String) { _address1.value = newValue }
    fun onAddress2Change(newValue: String) { _address2.value = newValue }

    fun generateNewCode() {
        viewModelScope.launch {
            val lastCode = repository.getLastDebtorCode()

            if (lastCode == null) {
                _accountCode.value = "ACC001"
            } else {
                try {
                    val number = lastCode.removePrefix("ACC").toIntOrNull() ?: 0
                    _accountCode.value = "ACC" + String.format("%03d", number + 1)
                } catch (_: Exception) {
                    _accountCode.value = "ACC001"
                }
            }
        }
    }

    fun saveDebtor() {
        viewModelScope.launch {
            if (_name.value.isBlank() || _name.value.isDigitsOnly() || _address1.value.isBlank()) { _toastMessage.value = "Name is required"; return@launch }

            val debtor = DebtorMaster(
                accountCode = _accountCode.value, name = _name.value,
                address1 = _address1.value, address2 = _address2.value, balance = 0.0
            )

            if (_isEditMode.value) {
                val existing = repository.getDebtor(_accountCode.value)
                if (existing != null) {
                    repository.insertDebtor(debtor.copy(
                        balance = existing.balance,
                        salesYearToDate = existing.salesYearToDate,
                        costYearToDate = existing.costYearToDate
                    ))
                    _toastMessage.value = "Debtor Updated"
                    _navigationChannel.send(true)
                }
            } else {
                repository.insertDebtor(debtor)
                _toastMessage.value = "Debtor Created"

                _navigationChannel.send(true)

                clearForm()
            }
        }
    }

    fun deleteDebtor() {
        viewModelScope.launch {
            repository.deleteDebtor(_accountCode.value)

            _toastMessage.value = "Debtor Deleted"
            clearForm()
            _navigationChannel.send(true)
        }
    }

    fun clearToast() { _toastMessage.value = null }
    private fun clearForm() { _name.value = ""; _address1.value = ""; _address2.value = ""; _accountCode.value = "" }
}