package com.example.stellarstocks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stellarstocks.data.db.models.DebtorMaster
import com.example.stellarstocks.data.db.models.InvoiceDetail
import com.example.stellarstocks.data.db.models.InvoiceHeader
import com.example.stellarstocks.data.db.models.StockMaster
import com.example.stellarstocks.data.db.repository.StellarStocksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

data class InvoiceItem( //Class to represent an item in the invoice
    val stock: StockMaster,
    val qty: Int,
    val discountPercent: Double,
    val discountAmount: Double,
    val lineTotal: Double
)

class InvoiceViewModel(private val repository: StellarStocksRepository) : ViewModel() { //Invoice view model
    private val _selectedDebtor = MutableStateFlow<DebtorMaster?>(null) // Debtor selected for invoice
    val selectedDebtor = _selectedDebtor.asStateFlow()

    private val _invoiceItems = MutableStateFlow<List<InvoiceItem>>(emptyList()) // List of items in the Invoice
    val invoiceItems = _invoiceItems.asStateFlow()

    private val _totalExVat = MutableStateFlow(0.0) // Total amount before VAT
    val totalExVat = _totalExVat.asStateFlow()

    private val _vat = MutableStateFlow(0.0) // VAT amount
    val vat = _vat.asStateFlow()

    private val _grandTotal = MutableStateFlow(0.0) // Gross total amount
    val grandTotal = _grandTotal.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    private val _invoiceNum = MutableStateFlow((System.currentTimeMillis() % 1000000).toInt()) // Invoice number
    val invoiceNum = _invoiceNum.asStateFlow()

    private val _isInvoiceProcessed = MutableStateFlow(false) // Invoice processed flag
    val isInvoiceProcessed = _isInvoiceProcessed.asStateFlow()

    fun setDebtor(debtor: DebtorMaster) { // Set debtor for invoice
        _selectedDebtor.value = debtor
    }

    fun addToInvoice(stock: StockMaster, qty: Int, discountPercent: Double) {
        if (qty <= 0) { // Check if quantity is valid
            _toastMessage.value = "Quantity must be greater than 0"
            return
        }
        if (discountPercent !in 0.0..100.0) { // discount cannot be greater than 100% or less than 0%
            _toastMessage.value = "Discount must be between 0% and 100%"
            return
        }


        if (qty > stock.stockOnHand) {
            _toastMessage.value = "Insufficient stock! Available: ${stock.stockOnHand}"
            return
        }


        val grossTotal = stock.sellingPrice * qty // Calculate total before discount
        val discountAmt = grossTotal * (discountPercent / 100) // Calculate discount amount
        val finalTotal = grossTotal - discountAmt // calculate the final total

        val currentList = _invoiceItems.value.toMutableList() // Get current list of items

        currentList.removeAll { it.stock.stockCode == stock.stockCode } // Remove any existing items with the same stock code

        currentList.add(
            InvoiceItem(stock, qty, discountPercent, discountAmt, finalTotal)
        )
        _invoiceItems.value = currentList
        calculateTotals()
    }

    fun removeFromInvoice(item: InvoiceItem) { // Remove an item from invoice
        val currentList = _invoiceItems.value.toMutableList()
        currentList.remove(item)
        _invoiceItems.value = currentList
        calculateTotals()
    }
    private fun calculateTotals() { // Calculate final total for invoice
        val exVat = _invoiceItems.value.sumOf { it.lineTotal }
        val vatCalc = exVat * 0.15 // 15% VAT
        val total = exVat + vatCalc

        _totalExVat.value = exVat
        _vat.value = vatCalc
        _grandTotal.value = total
    }

    fun updateInvoiceItem(stock: StockMaster, newQty: Int, newDiscount: Double) {
        if (newQty <= 0) { // Check if quantity is greater than 0
            val itemToRemove = _invoiceItems.value.find { it.stock.stockCode == stock.stockCode }
            if (itemToRemove != null) removeFromInvoice(itemToRemove)
            _toastMessage.value = "Quantity must be greater than 0"
            return
        }
        if (newDiscount !in 0.0..100.0) { // check if discount is less than 100 and more than 0
            _toastMessage.value = "Discount must be between 0 and 100"
            return
        }

        if (newQty > stock.stockOnHand) {
            _toastMessage.value = "Insufficient stock! Available: ${stock.stockOnHand}"
            return
        }

        val grossTotal = stock.sellingPrice * newQty // calculate gross total before discount
        val discountAmt = grossTotal * (newDiscount / 100)
        val finalTotal = grossTotal - discountAmt // calculate final total

        val currentList = _invoiceItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.stock.stockCode == stock.stockCode } // find index of item to update

        if (index != -1) { // if index is found then update the necessary fields
            currentList[index] = InvoiceItem(stock, newQty, newDiscount, discountAmt, finalTotal)
            _invoiceItems.value = currentList
            calculateTotals()
        }
    }
    fun confirmInvoice() { // Confirm invoice and process it
        val debtor = _selectedDebtor.value
        val items = _invoiceItems.value

        if (debtor == null) {
            _toastMessage.value = "Please select a Debtor"
            return
        }
        if (items.isEmpty()) {
            _toastMessage.value = "Invoice is empty"
            return
        }

        val insufficientItems = items.filter { it.qty > it.stock.stockOnHand }
        if (insufficientItems.isNotEmpty()) {
            _toastMessage.value = "Cannot process: Insufficient stock for ${insufficientItems.size} item(s)."
            return
        }

        viewModelScope.launch {
            val invoiceNum = _invoiceNum.value // get invoice number
            val calculatedTotalCost = items.sumOf { it.qty * it.stock.cost } //calculate total cost

            val header = InvoiceHeader( // create the invoice header
                invoiceNum = invoiceNum,
                accountCode = debtor.accountCode,
                date = Date(),
                totalSellAmtExVat = _totalExVat.value,
                vat = _vat.value,
                totalCost = calculatedTotalCost
            )

            val detailItems = items.mapIndexed { index, invoiceItem -> //create the details for the invoice
                InvoiceDetail(
                    invoiceNum = invoiceNum,
                    itemNum = index + 1,
                    stockCode = invoiceItem.stock.stockCode,
                    qtySold = invoiceItem.qty,
                    unitCost = invoiceItem.stock.cost,
                    unitSell = invoiceItem.stock.sellingPrice,
                    discount = invoiceItem.discountAmount,
                    total = invoiceItem.lineTotal
                )
            }

            repository.processInvoice(header, detailItems) // process the invoice

            _toastMessage.value = "Invoice Processed Successfully!"
            _isInvoiceProcessed.value = true // set invoice processed flag to true
        }
    }


    fun startNewInvoice() { // Start a new invoice
        _invoiceItems.value = emptyList()
        _selectedDebtor.value = null
        _invoiceNum.value = (System.currentTimeMillis() % 1000000).toInt()
        _isInvoiceProcessed.value = false
        calculateTotals()
    }

    fun clearToast() { _toastMessage.value = null }
}