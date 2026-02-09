package com.example.stellarstocks.viewmodel

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stellarstocks.data.db.models.DebtorMaster
import com.example.stellarstocks.data.db.models.DebtorTransaction
import com.example.stellarstocks.data.db.models.DebtorTransactionInfo
import com.example.stellarstocks.data.db.repository.StellarStocksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.collections.emptyList
import kotlin.math.abs

enum class SortOption { // enum class to hold sort options
    FULL_LIST,
    RECENT_ITEM_SOLD,
    HIGHEST_VALUE,
    LOWEST_VALUE
}

enum class DebtorListSortOption {
    CODE_ASC,
    CODE_DESC,
    BALANCE_ASC,
    BALANCE_DESC
}
class DebtorViewModel(private val repository: StellarStocksRepository) : ViewModel() {
    private val _searchQuery = MutableStateFlow("") // variable to hold search query
    val searchQuery = _searchQuery.asStateFlow()

    private val _debtorListSort = MutableStateFlow(DebtorListSortOption.CODE_ASC)
    val debtorListSort = _debtorListSort.asStateFlow()

    private val _currentSort =
        MutableStateFlow(SortOption.FULL_LIST) // variable to hold current sort option
    val currentSort = _currentSort.asStateFlow()

    val filteredDebtors: StateFlow<List<DebtorMaster>> = combine(
        repository.getAllDebtors(),
        _searchQuery,
        _debtorListSort
    ) { debtors: List<DebtorMaster>, query: String, sortOption: DebtorListSortOption ->
        val filtered = if (query.isBlank()) {
            debtors
        } else {
            debtors.filter {
                it.accountCode.contains(query, ignoreCase = true) ||
                        it.name.contains(query, ignoreCase = true)
            }
        }

        when (sortOption) {
            DebtorListSortOption.CODE_ASC -> filtered.sortedBy { it.accountCode }
            DebtorListSortOption.CODE_DESC -> filtered.sortedByDescending { it.accountCode }
            DebtorListSortOption.BALANCE_ASC -> filtered.sortedBy { it.balance }
            DebtorListSortOption.BALANCE_DESC -> filtered.sortedByDescending { it.balance }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedDebtor =
        MutableStateFlow<DebtorMaster?>(null) // variable to hold selected debtor
    val selectedDebtor = _selectedDebtor.asStateFlow()

    private val _selectedTransactions =
        MutableStateFlow<List<DebtorTransactionInfo>>(emptyList()) // variable to hold selected debtor transactions

    val visibleTransactions: StateFlow<List<DebtorTransactionInfo>> =
        combine( // variable to hold debtor transactions based on sort option
            _selectedTransactions,
            _currentSort
        ) { transactions, sortOption ->
            when (sortOption) {
                SortOption.FULL_LIST -> transactions.sortedByDescending { it.date }
                SortOption.RECENT_ITEM_SOLD -> transactions.filter { it.items != null }
                    .sortedByDescending { it.date }

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

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _addrLine1 = MutableStateFlow("")
    val addrLine1 = _addrLine1.asStateFlow()

    private val _addrLine2 = MutableStateFlow("")
    val addrLine2 = _addrLine2.asStateFlow()

    private val _suburb = MutableStateFlow("")
    val suburb = _suburb.asStateFlow()

    private val _postalCode = MutableStateFlow("")
    val postalCode = _postalCode.asStateFlow()

    private val _showAddress2 = MutableStateFlow(false)
    val showAddress2 = _showAddress2.asStateFlow()

    private val _addr2Line1 = MutableStateFlow("")
    val addr2Line1 = _addr2Line1.asStateFlow()

    private val _addr2Line2 = MutableStateFlow("")
    val addr2Line2 = _addr2Line2.asStateFlow()

    private val _addr2Suburb = MutableStateFlow("")
    val addr2Suburb = _addr2Suburb.asStateFlow()

    private val _addr2PostalCode = MutableStateFlow("")
    val addr2PostalCode = _addr2PostalCode.asStateFlow()

    private val _balance = MutableStateFlow(0.0)
    val balance = _balance.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    private val _navigationChannel = Channel<Boolean>()
    val navigationChannel = _navigationChannel.receiveAsFlow()

    private val _transactions = MutableStateFlow<List<DebtorTransaction>>(emptyList())
    val transactions = _transactions.asStateFlow()

    private val _transactionInfo = MutableStateFlow<List<DebtorTransactionInfo>>(emptyList())
    val transactionInfo = _transactionInfo.asStateFlow()

    fun onSearchQueryChange(query: String) { // function to update search query
        _searchQuery.value = query
    }

    fun updateDebtorListSort(option: DebtorListSortOption) {
        _debtorListSort.value = option
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

    fun onSearchCodeChange(newValue: String) {
        _accountCode.value = newValue
    }
    fun onNameChange(newValue: String) {
        _name.value = newValue
    }

    // Address 1 Setters
    fun onAddrLine1Change(newValue: String) {
        _addrLine1.value = newValue.filter { it.isDigit() }
    }
    fun onAddrLine2Change(newValue: String) {
        _addrLine2.value = newValue.filter { it.isLetterOrDigit() || it.isWhitespace() }
    }
    fun onSuburbChange(newValue: String) {
        _suburb.value = newValue.filter { it.isLetter() || it.isWhitespace() }
    }
    fun onPostalCodeChange(newValue: String) {
        // Filter digits and limit to 4 characters
        _postalCode.value = newValue.filter { it.isDigit() }.take(4)
    }
    // Address 2 Setters
    fun onShowAddress2Change(v: Boolean) { _showAddress2.value = v }
    fun onAddr2Line1Change(v: String) {
        _addr2Line1.value = v.filter { it.isDigit() }
    }
    fun onAddr2Line2Change(v: String) {
        _addr2Line2.value = v.filter { it.isLetterOrDigit() || it.isWhitespace() }
    }
    fun onAddr2SuburbChange(v: String) {
        _addr2Suburb.value = v.filter { it.isLetter() || it.isWhitespace() }
    }
    fun onAddr2PostalCodeChange(v: String) {
        _addr2PostalCode.value = v.filter { it.isDigit() }.take(4)
    }


    fun generateNewCode() { // function to generate new account code
        viewModelScope.launch {
            val lastCode = repository.getLastDebtorCode() // get last account code

            if (lastCode == null) { // if no account code, set to ACC001
                _accountCode.value = "ACC001"
            } else {
                try {
                    val number = lastCode.removePrefix("ACC").toIntOrNull()
                        ?: 0 // get last number and increment
                    _accountCode.value = "ACC" + String.format("%03d", number + 1)
                } catch (_: Exception) {
                    _accountCode.value = "ACC001"
                }
            }
        }
    }

    fun saveDebtor() { // function to save debtor
        viewModelScope.launch {
            if (_name.value.isBlank()) { // if name is blank throw error
                _toastMessage.value = "Name cannot be blank"
                return@launch
            }
            if (_name.value.isDigitsOnly()) {
                _toastMessage.value =
                    "Name cannot be all digits" // if name is all digits throw error
                return@launch
            }
            if (_addrLine1.value.isBlank()) {
                _toastMessage.value = "Address Line 1 is required"
                return@launch
            }
            if (_addrLine2.value.isBlank()) {
                _toastMessage.value = "Address Line 2 is required"
                return@launch
            }
            if (_suburb.value.isBlank()) {
                _toastMessage.value = "Suburb is required"
                return@launch
            }
            if (_postalCode.value.length != 4) {
                _toastMessage.value = "Postal Code must be 4 digits"
                return@launch
            }

            val combinedAddress1 = listOf(
                _addrLine1.value.trim(),
                _addrLine2.value.trim(),
                _suburb.value.trim(),
                _postalCode.value.trim()
            ).filter { it.isNotEmpty() }.joinToString(", ")

            if (_showAddress2.value) { // if address 2 is visible, check for required fields
                if (_addr2Line1.value.isBlank()) { _toastMessage.value = "Address 2 Line 1 required"; return@launch }
                if (_addr2Line2.value.isBlank()) { _toastMessage.value = "Address 2 Line 2 required"; return@launch }
                if (_addr2Suburb.value.isBlank()) { _toastMessage.value = "Address 2 Suburb required"; return@launch }
                if (_addr2PostalCode.value.length != 4) { _toastMessage.value = "Address 2 Post Code must be 4 digits"; return@launch }
            }

            val combinedAddress2 = if (_showAddress2.value) {
                listOf(
                    _addr2Line1.value.trim(),
                    _addr2Line2.value.trim(),
                    _addr2Suburb.value.trim(),
                    _addr2PostalCode.value.trim()
                ).joinToString(", ")
            } else { "" }

            val debtor = DebtorMaster(
                accountCode = _accountCode.value,
                name = _name.value,
                address1 = combinedAddress1,
                address2 = combinedAddress2,
                balance = 0.0
            )

            if (_isEditMode.value) {
                val existing = repository.getDebtor(_accountCode.value)
                if (existing != null) {
                    repository.updateDebtor(debtor.copy(
                        balance = existing.balance,
                        salesYearToDate = existing.salesYearToDate,
                        costYearToDate = existing.costYearToDate,
                        isActive = existing.isActive
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

    fun loadDebtorDetails(code: String) {
        viewModelScope.launch {
            val debtor = repository.getDebtor(code)
            if (debtor != null) {
                _accountCode.value = debtor.accountCode
                _name.value = debtor.name

                // Split Address 1
                val parts1 = debtor.address1.split(",").map { it.trim() }
                _addrLine1.value = parts1.getOrNull(0) ?: ""
                _addrLine2.value = parts1.getOrNull(1) ?: ""
                _suburb.value = parts1.getOrNull(2) ?: ""
                _postalCode.value = parts1.getOrNull(3) ?: ""

                // Split Address 2
                val parts2 = debtor.address2?.split(",")?.map { it.trim() }
                if (!parts2.isNullOrEmpty() && debtor.address2?.isNotBlank() == true) {
                    _showAddress2.value = true
                    _addr2Line1.value = parts2.getOrNull(0) ?: ""
                    _addr2Line2.value = parts2.getOrNull(1) ?: ""
                    _addr2Suburb.value = parts2.getOrNull(2) ?: ""
                    _addr2PostalCode.value = parts2.getOrNull(3) ?: ""
                } else {
                    _showAddress2.value = false
                    clearAddress2Form()
                }

                _balance.value = debtor.balance
                repository.getDebtorTransactionInfo(code).collect { list -> _transactionInfo.value = list }
            }
        }
    }

    fun deleteDebtor() {
        viewModelScope.launch {
            if (_accountCode.value.isNotBlank()) {
                repository.deleteDebtor(_accountCode.value)
                _toastMessage.value = "Debtor Deleted"
                _navigationChannel.send(true)
                clearForm()
            } else {
                _toastMessage.value = "No debtor selected"
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    private fun clearForm() {
        _name.value = ""
        _addrLine1.value = ""
        _addrLine2.value = ""
        _suburb.value = ""
        _postalCode.value = ""
        _accountCode.value = ""
        _showAddress2.value = false
        clearAddress2Form()
    }

    private fun clearAddress2Form() {
        _addr2Line1.value = ""
        _addr2Line2.value = ""
        _addr2Suburb.value = ""
        _addr2PostalCode.value = ""
    }

    val topDebtors = repository.getAllDebtors().map { list -> //for graphing
        list.sortedByDescending { it.salesYearToDate }.take(5)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlySales: StateFlow<List<Pair<Float, Float>>> = repository.getAllDebtorTransactions() //for graphing
        .map { transactions ->
            if (transactions.isEmpty()) return@map emptyList<Pair<Float, Float>>()

            val calendar = Calendar.getInstance()

            // Group by Month
            transactions
                .groupBy {
                    calendar.time = it.date
                    calendar.get(Calendar.MONTH)
                }
                .map { (month, trans) ->
                    month.toFloat() to trans.sumOf { it.grossTransactionValue }.toFloat()
                }
                .sortedBy { it.first } // chronological order
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}