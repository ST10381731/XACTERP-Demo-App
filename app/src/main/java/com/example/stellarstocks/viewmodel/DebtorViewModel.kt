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
import kotlin.math.abs

enum class SortOption { // enum class to hold sort options
    FULL_LIST,
    RECENT_ITEM_SOLD,
    HIGHEST_VALUE,
    LOWEST_VALUE
}
class DebtorViewModel(private val repository: StellarStocksRepository) : ViewModel() {
    private val _searchQuery = MutableStateFlow("") // variable to hold search query
    val searchQuery = _searchQuery.asStateFlow()
    private val _currentSort = MutableStateFlow(SortOption.FULL_LIST) // variable to hold current sort option
    val currentSort = _currentSort.asStateFlow()

    val filteredDebtors: StateFlow<List<DebtorMaster>> = combine( // variable to hold filtered debtors
        repository.getAllDebtors(),
        _searchQuery
    ) { debtors, query ->
        if (query.isBlank()) { // if query is blank, return all debtors
            debtors
        } else {
            debtors.filter {
                it.accountCode.contains(query, ignoreCase = true) || // filter by account code
                        it.name.contains(query, ignoreCase = true) // filter by name
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedDebtor = MutableStateFlow<DebtorMaster?>(null) // variable to hold selected debtor
    val selectedDebtor = _selectedDebtor.asStateFlow()

    private val _selectedTransactions = MutableStateFlow<List<DebtorTransactionInfo>>(emptyList()) // variable to hold selected debtor transactions

    val visibleTransactions: StateFlow<List<DebtorTransactionInfo>> = combine( // variable to hold debtor transactions based on sort option
        _selectedTransactions,
        _currentSort
    ) { transactions, sortOption ->
        when (sortOption) {
            SortOption.FULL_LIST -> transactions.sortedByDescending { it.date }
            SortOption.RECENT_ITEM_SOLD -> transactions.filter { it.items != null }.sortedByDescending { it.date }
            SortOption.HIGHEST_VALUE -> transactions.sortedByDescending { abs(it.value) }
            SortOption.LOWEST_VALUE -> transactions.sortedBy { abs(it.value) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isEditMode = MutableStateFlow(false) // variable to hold edit mode
    val isEditMode = _isEditMode.asStateFlow()

    private val _accountCode = MutableStateFlow("") // variable to hold account code
    val accountCode = _accountCode.asStateFlow()

    private val _name = MutableStateFlow("") // variable to hold name
    val name = _name.asStateFlow()

    private val _address1 = MutableStateFlow("") // variable to hold primary address
    val address1 = _address1.asStateFlow()

    private val _address2 = MutableStateFlow("") // variable to hold secondary address
    val address2 = _address2.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    private val _navigationChannel = Channel<Boolean>()
    val navigationChannel = _navigationChannel.receiveAsFlow()

    fun onSearchQueryChange(query: String) { // function to update search query
        _searchQuery.value = query
    }

    fun resetSearch() { // function to reset search query
        _searchQuery.value = ""
    }

    fun selectDebtorForDetails(code: String) { // function to select debtor for details
        viewModelScope.launch {
            // Get Debtor Master Details
            _selectedDebtor.value = repository.getDebtor(code)

            // Get Debtor Transaction History
            repository.getDebtorTransactionInfo(code).collect { transactions ->
                _selectedTransactions.value = transactions
            }
        }
    }

    fun updateSort(option: SortOption) { // function to update sort option
        _currentSort.value = option
    }

    fun toggleMode() { // function to toggle edit mode
        _isEditMode.value = !_isEditMode.value
        clearForm()
        if (!_isEditMode.value) generateNewCode()
    }
    fun onSearchCodeChange(newValue: String) { _accountCode.value = newValue } // function to update account code
    fun onNameChange(newValue: String) { _name.value = newValue } // function to update name
    fun onAddress1Change(newValue: String) { _address1.value = newValue } // function to update primary address
    fun onAddress2Change(newValue: String) { _address2.value = newValue } // function to update secondary address

    fun generateNewCode() { // function to generate new account code
        viewModelScope.launch {
            val lastCode = repository.getLastDebtorCode() // get last account code

            if (lastCode == null) { // if no account code, set to ACC001
                _accountCode.value = "ACC001"
            } else {
                try {
                    val number = lastCode.removePrefix("ACC").toIntOrNull() ?: 0 // get last number and increment
                    _accountCode.value = "ACC" + String.format("%03d", number + 1)
                } catch (_: Exception) {
                    _accountCode.value = "ACC001"
                }
            }
        }
    }

    fun saveDebtor() { // function to save debtor
        viewModelScope.launch {
            if (_name.value.isBlank()||_name.value.isDigitsOnly()) { // if name is blank or all digits, show error
                _toastMessage.value = "Name cannot be blank or all digits"
                return@launch
            }

            if (_address1.value.isBlank() || _address1.value.isDigitsOnly() || _address2.value.isBlank() || _address2.value.isDigitsOnly()){
                _toastMessage.value="Address cannot be blank or all digits"
                return@launch
            }

            val debtor = DebtorMaster( // create debtor object
                accountCode = _accountCode.value,
                name = _name.value,
                address1 = _address1.value,
                address2 = _address2.value,
                balance = 0.0
            )

            if (_isEditMode.value) { // if in edit mode, update debtor
                val existing = repository.getDebtor(_accountCode.value) // get existing debtor
                if (existing != null) { // if debtor exists, update
                    repository.updateDebtor(debtor.copy(
                        balance = existing.balance,
                        salesYearToDate = existing.salesYearToDate,
                        costYearToDate = existing.costYearToDate
                    ))
                    _toastMessage.value = "Debtor Updated"
                    _navigationChannel.send(true) // navigate back to debtor enquiry screen
                    clearForm()
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
            _navigationChannel.send(true)// navigate back to debtor enquiry screen
        }
    }

    fun clearToast() { _toastMessage.value = null }
    private fun clearForm() { _name.value = ""; _address1.value = ""; _address2.value = ""; _accountCode.value = "" }
}