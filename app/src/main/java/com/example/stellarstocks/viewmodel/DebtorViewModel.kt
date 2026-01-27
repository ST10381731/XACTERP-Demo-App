package com.example.stellarstocks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stellarstocks.data.db.models.DebtorMaster
import com.example.stellarstocks.data.db.models.DebtorTransaction
import com.example.stellarstocks.data.db.repository.StellarStocksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

enum class SortOption {
    MOST_RECENT,   // Date Descending
    HIGHEST_VALUE, // Value Descending
    LOWEST_VALUE   // Value Ascending
}
class DebtorViewModel(private val repository: StellarStocksRepository) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    private val _currentSort = MutableStateFlow(SortOption.MOST_RECENT)
    val currentSort = _currentSort.asStateFlow()

    val filteredDebtors: StateFlow<List<DebtorMaster>> = combine(
        repository.getAllDebtors(),
        _searchQuery
    ) { debtors, query ->
        if (query.isBlank()) debtors else debtors.filter { it.accountCode.contains(query, ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedDebtor = MutableStateFlow<DebtorMaster?>(null)
    val selectedDebtor = _selectedDebtor.asStateFlow()

    private val _selectedTransactions = MutableStateFlow<List<DebtorTransaction>>(emptyList())

    val visibleTransactions: StateFlow<List<DebtorTransaction>> = combine(
        _selectedTransactions,
        _currentSort
    ) { transactions, sortOption ->
        when (sortOption) {
            SortOption.MOST_RECENT -> transactions.sortedByDescending { it.date }
            SortOption.HIGHEST_VALUE -> transactions.sortedByDescending { it.grossTransactionValue }
            SortOption.LOWEST_VALUE -> transactions.sortedBy { it.grossTransactionValue }
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


    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun selectDebtorForDetails(code: String) {
        viewModelScope.launch {
            // Get Master Details
            _selectedDebtor.value = repository.getDebtor(code)

            // Get Transaction History
            repository.getDebtorTransactions(code).collect { transactions ->
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
        val currentList = filteredDebtors.value
        if (currentList.isEmpty()) { _accountCode.value = "ACC001"; return }
        try {
            val maxId = currentList.mapNotNull { it.accountCode.removePrefix("ACC").toIntOrNull() }.maxOrNull() ?: 0
            _accountCode.value = "ACC" + String.format("%03d", maxId + 1)
        } catch (e: Exception) { _accountCode.value = "ACC001" }
    }

    fun searchDebtor() {
        viewModelScope.launch {
            val code = _accountCode.value.trim()
            val debtor = repository.getDebtor(code)
            if (debtor != null) {
                _name.value = debtor.name
                _address1.value = debtor.address1
                _address2.value = debtor.address2
                _toastMessage.value = "Debtor Found"
            } else { _toastMessage.value = "Debtor not found" }
        }
    }

    fun saveDebtor() {
        viewModelScope.launch {
            if (_name.value.isBlank()) { _toastMessage.value = "Name is required"; return@launch }
            val debtor = DebtorMaster(
                accountCode = _accountCode.value, name = _name.value,
                address1 = _address1.value, address2 = _address2.value, balance = 0.0
            )
            if (_isEditMode.value) {
                val existing = repository.getDebtor(_accountCode.value)
                if (existing != null) repository.insertDebtor(debtor.copy(balance = existing.balance, salesYearToDate = existing.salesYearToDate, costYearToDate = existing.costYearToDate))
            } else { repository.insertDebtor(debtor) }
            _toastMessage.value = "Saved Successfully"
            if (!_isEditMode.value) { clearForm(); generateNewCode() }
        }
    }

    fun deleteDebtor() {
        viewModelScope.launch {
            val existing = repository.getDebtor(_accountCode.value)
            if (existing != null) { repository.deleteDebtor(existing); _toastMessage.value = "Deleted"; clearForm() }
        }
    }
    fun clearToast() { _toastMessage.value = null }
    private fun clearForm() { _name.value = ""; _address1.value = ""; _address2.value = ""; _accountCode.value = "" }
}