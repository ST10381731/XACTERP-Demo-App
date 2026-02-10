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

enum class SortOption { // enum class to hold debtor transaction sort options
    FULL_LIST,
    RECENT_ITEM_SOLD,
    HIGHEST_VALUE,
    LOWEST_VALUE
}

enum class DebtorListSortOption { //enum class to hold debtor list sort options
    CODE_ASC,
    CODE_DESC,
    BALANCE_ASC,
    BALANCE_DESC
}
class DebtorViewModel(private val repository: StellarStocksRepository) : ViewModel() {
    private val _searchQuery = MutableStateFlow("") // variable to hold search query
    val searchQuery = _searchQuery.asStateFlow()

    private val _debtorListSort = MutableStateFlow(DebtorListSortOption.CODE_ASC) // variable to hold debtor list sort option
    val debtorListSort = _debtorListSort.asStateFlow()

    private val _currentSort =
        MutableStateFlow(SortOption.FULL_LIST) // variable to hold current sort option
    val currentSort = _currentSort.asStateFlow()

    val filteredDebtors: StateFlow<List<DebtorMaster>> = combine( // variable to hold filtered debtors based on search query and sort option
        repository.getAllDebtors(),
        _searchQuery,
        _debtorListSort
    ) { debtors: List<DebtorMaster>, query: String, sortOption: DebtorListSortOption ->
        val filtered = if (query.isBlank()) { // if search query is blank, return all debtors
            debtors
        } else { // if search query is not blank, filter debtors by account code or name
            debtors.filter {
                it.accountCode.contains(query, ignoreCase = true) ||
                        it.name.contains(query, ignoreCase = true)
            }
        }

        when (sortOption) { // sort debtors based on sort option
            DebtorListSortOption.CODE_ASC -> filtered.sortedBy { it.accountCode }
            DebtorListSortOption.CODE_DESC -> filtered.sortedByDescending { it.accountCode }
            DebtorListSortOption.BALANCE_ASC -> filtered.sortedBy { it.balance }
            DebtorListSortOption.BALANCE_DESC -> filtered.sortedByDescending { it.balance }
        }
    }.stateIn( // state flow to hold filtered debtors
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
            when (sortOption) { // sort transactions based on sort option
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

    private val _name = MutableStateFlow("") // variable to hold debtor name
    val name = _name.asStateFlow()

    private val _addrLine1 = MutableStateFlow("") // variable to hold debtor primary address line 1
    val addrLine1 = _addrLine1.asStateFlow()

    private val _addrLine2 = MutableStateFlow("") // variable to hold debtor primary address line 2
    val addrLine2 = _addrLine2.asStateFlow()

    private val _suburb = MutableStateFlow("") // variable to hold debtor suburb
    val suburb = _suburb.asStateFlow()

    private val _postalCode = MutableStateFlow("") // variable to hold debtor postal code
    val postalCode = _postalCode.asStateFlow()

    private val _showAddress2 = MutableStateFlow(false) // variable to determine state of secondary address
    val showAddress2 = _showAddress2.asStateFlow()

    private val _addr2Line1 = MutableStateFlow("") // variable to hold secondary address line 1
    val addr2Line1 = _addr2Line1.asStateFlow()

    private val _addr2Line2 = MutableStateFlow("") // variable to hold secondary address line 2
    val addr2Line2 = _addr2Line2.asStateFlow()

    private val _addr2Suburb = MutableStateFlow("") // variable to hold secondary address suburb
    val addr2Suburb = _addr2Suburb.asStateFlow()

    private val _addr2PostalCode = MutableStateFlow("") // variable to hold secondary address postal code
    val addr2PostalCode = _addr2PostalCode.asStateFlow()

    private val _balance = MutableStateFlow(0.0) // variable to hold debtor balance
    val balance = _balance.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null) // variable to hold toast messages
    val toastMessage = _toastMessage.asStateFlow()

    private val _navigationChannel = Channel<Boolean>() // variable to hold navigation events
    val navigationChannel = _navigationChannel.receiveAsFlow()

    private val _transactionInfo = MutableStateFlow<List<DebtorTransactionInfo>>(emptyList()) // variable to hold debtor transaction info

    fun onSearchQueryChange(query: String) { // function to update search query
        _searchQuery.value = query
    }

    fun updateDebtorListSort(option: DebtorListSortOption) { // function to update debtor list sort option
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

    fun onSearchCodeChange(newValue: String) { // function to update account code
        _accountCode.value = newValue
    }
    fun onNameChange(newValue: String) { // function to update name
        _name.value = newValue
    }

    // Primary Address Setters
    fun onAddrLine1Change(newValue: String) { // function to update primary address line 1
        _addrLine1.value = newValue.filter { it.isDigit() }
    }
    fun onAddrLine2Change(newValue: String) { // function to update primary address line 2
        _addrLine2.value = newValue.filter { it.isLetterOrDigit() || it.isWhitespace() }
    }
    fun onSuburbChange(newValue: String) { // function to update suburb
        _suburb.value = newValue.filter { it.isLetter() || it.isWhitespace() }
    }
    fun onPostalCodeChange(newValue: String) { // function to update postal code
        // Filter digits and limit to 4 characters
        _postalCode.value = newValue.filter { it.isDigit() }.take(4)
    }
    // Secondary Address Setters
    fun onShowAddress2Change(v: Boolean) { _showAddress2.value = v } // function to toggle secondary address visibility
    fun onAddr2Line1Change(v: String) { // function to update secondary address line 1
        _addr2Line1.value = v.filter { it.isDigit() }
    }
    fun onAddr2Line2Change(v: String) { // function to update secondary address line 2
        _addr2Line2.value = v.filter { it.isLetterOrDigit() || it.isWhitespace() }
    }
    fun onAddr2SuburbChange(v: String) { // function to update secondary address suburb
        _addr2Suburb.value = v.filter { it.isLetter() || it.isWhitespace() }
    }
    fun onAddr2PostalCodeChange(v: String) { // function to update secondary address postal code
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
            if (_name.value.isDigitsOnly()) { // if name is all digits throw error
                _toastMessage.value =
                    "Name cannot be all digits"
                return@launch
            }
            if (_addrLine1.value.isBlank()) { // if primary address line 1 is blank throw error
                _toastMessage.value = "Address Line 1 is required"
                return@launch
            }
            if (_addrLine2.value.isBlank()) { // if primary address line 2 is blank throw error
                _toastMessage.value = "Address Line 2 is required"
                return@launch
            }
            if (_suburb.value.isBlank()) { // if suburb is blank throw error
                _toastMessage.value = "Suburb is required"
                return@launch
            }
            if (_postalCode.value.length != 4) { // if postal code is not 4 digits throw error
                _toastMessage.value = "Postal Code must be 4 digits"
                return@launch
            }

            val combinedAddress1 = listOf( // combine primary address lines into one string
                _addrLine1.value.trim(),
                _addrLine2.value.trim(),
                _suburb.value.trim(),
                _postalCode.value.trim()
            ).filter { it.isNotEmpty() }.joinToString(", ")

            if (_showAddress2.value) { // if address 2 is visible, check for required fields
                if (_addr2Line1.value.isBlank()) { // if address 2 line 1 is blank throw error
                    _toastMessage.value = "Address 2 Line 1 required"
                    return@launch
                }
                if (_addr2Line2.value.isBlank()) { // if address 2 line 2 is blank throw error
                    _toastMessage.value = "Address 2 Line 2 required"
                    return@launch
                }
                if (_addr2Suburb.value.isBlank()) { // if address 2 suburb is blank throw error
                    _toastMessage.value = "Address 2 Suburb required"
                    return@launch
                }
                if (_addr2PostalCode.value.length != 4) { // if address 2 postal code is not 4 digits throw error
                    _toastMessage.value = "Address 2 Post Code must be 4 digits"
                    return@launch
                }
            }

            val combinedAddress2 = if (_showAddress2.value) { // combine address 2 lines into one string
                listOf(
                    _addr2Line1.value.trim(),
                    _addr2Line2.value.trim(),
                    _addr2Suburb.value.trim(),
                    _addr2PostalCode.value.trim()
                ).joinToString(", ")
            } else { "" } // if address 2 is not visible, set to empty string


            val debtor = DebtorMaster( // create debtor object
                accountCode = _accountCode.value,
                name = _name.value,
                address1 = combinedAddress1,
                address2 = combinedAddress2,
                balance = 0.0
            )

            if (_isEditMode.value) { // if in edit mode, update debtor
                val existing = repository.getDebtor(_accountCode.value) // get existing debtor
                if (existing != null) { // if debtor exists, update it
                    repository.updateDebtor(debtor.copy( // create new debtor object with updated values
                        balance = existing.balance,
                        salesYearToDate = existing.salesYearToDate,
                        costYearToDate = existing.costYearToDate,
                        isActive = existing.isActive
                    ))
                    _toastMessage.value = "Debtor Updated"
                    clearForm()
                    _navigationChannel.send(true)

                }
            } else { // if not in edit mode, insert new debtor
                repository.insertDebtor(debtor)
                _toastMessage.value = "Debtor Created"
                clearForm()
                _navigationChannel.send(true)

            }
        }
    }

    fun loadDebtorDetails(code: String) { // function to load debtor details
        viewModelScope.launch {
            val debtor = repository.getDebtor(code) // get debtor by code
            if (debtor != null) { // if debtor exists, update form fields
                _accountCode.value = debtor.accountCode
                _name.value = debtor.name

                // Split Address 1
                val parts1 = debtor.address1.split(",").map { it.trim() } // split address into parts
                _addrLine1.value = parts1.getOrNull(0) ?: ""
                _addrLine2.value = parts1.getOrNull(1) ?: ""
                _suburb.value = parts1.getOrNull(2) ?: ""
                _postalCode.value = parts1.getOrNull(3) ?: ""

                // Split Address 2
                val parts2 = debtor.address2?.split(",")?.map { it.trim() } //split address into parts if it exists
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

    fun deleteDebtor() { // function to delete debtor
        viewModelScope.launch {
            if (_accountCode.value.isNotBlank()) { // if account code is not blank, delete debtor
                repository.deleteDebtor(_accountCode.value)
                _toastMessage.value = "Debtor Deleted"
                _navigationChannel.send(true)
                clearForm()
            } else {
                _toastMessage.value = "No debtor selected"
            }
        }
    }

    fun clearToast() { // function to clear toast message
        _toastMessage.value = null
    }

    private fun clearForm() { // function to clear form fields
        _name.value = ""
        _addrLine1.value = ""
        _addrLine2.value = ""
        _suburb.value = ""
        _postalCode.value = ""
        _accountCode.value = ""
        _showAddress2.value = false
        clearAddress2Form()
    }

    private fun clearAddress2Form() { // function to clear address 2 form fields
        _addr2Line1.value = ""
        _addr2Line2.value = ""
        _addr2Suburb.value = ""
        _addr2PostalCode.value = ""
    }

    val topDebtors = repository.getAllDebtors().map { list -> //for graphing get all debtors with top 5 sales year to date
        list.sortedByDescending { it.salesYearToDate }.take(5)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlySales: StateFlow<List<Pair<Float, Float>>> = repository.getAllDebtorTransactions() //for graphing get all debtor transactions
        .map { transactions ->
            if (transactions.isEmpty()) return@map emptyList<Pair<Float, Float>>()

            val calendar = Calendar.getInstance() // calendar to get month from date


            transactions
                .groupBy {// Group by Month
                    calendar.time = it.date
                    calendar.get(Calendar.MONTH) // Get Month from Date
                }
                .map { (month, trans) -> // create a pair of month and total value
                    month.toFloat() to trans.sumOf { it.grossTransactionValue }.toFloat()
                }
                .sortedBy { it.first } // chronological order
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}