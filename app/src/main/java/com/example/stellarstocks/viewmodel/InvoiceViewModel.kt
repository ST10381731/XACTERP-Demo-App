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

    fun setDebtor(debtor: DebtorMaster) { // Set debtor for invoice
        _selectedDebtor.value = debtor
    }

    fun addToInvoice(stock: StockMaster, qty: Int, discountPercent: Double) { // Add item to invoice, qty, and discount percent
        if (qty <= 0 || discountPercent<0 || discountPercent>100) return // If qty is 0 or less, do not add to Invoice

        val grossTotal = stock.sellingPrice * qty // Calculate totalSellAmt
        val discountAmt = grossTotal * (discountPercent / 100) // Calculate discount amount
        val finalTotal = grossTotal - discountAmt // Calculate total before VAT

        val currentList = _invoiceItems.value.toMutableList() // returns list of items added to invoice

        currentList.removeAll { it.stock.stockCode == stock.stockCode } // Remove any existing items with the same stockCode

        currentList.add( // Add new item to list
            InvoiceItem(
                stock = stock,
                qty = qty,
                discountPercent = discountPercent,
                discountAmount = discountAmt,
                lineTotal = finalTotal
            )
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
    private fun calculateTotals() { // Calculate final totals for invoice
        val exVat = _invoiceItems.value.sumOf { it.lineTotal }
        val vatCalc = exVat * 0.15 // 15% VAT
        val total = exVat + vatCalc

        _totalExVat.value = exVat
        _vat.value = vatCalc
        _grandTotal.value = total
    }

    fun confirmInvoice() { // Confirm invoice and processing to other tables
        val debtor = _selectedDebtor.value // Debtor selected for invoice
        val items = _invoiceItems.value // List of items in the Invoice

        if (debtor == null) {
            _toastMessage.value = "Please select a Debtor"
            return
        }
        if (items.isEmpty()) {
            _toastMessage.value = "Invoice is empty"
            return
        }

        viewModelScope.launch {
            val invoiceNum = _invoiceNum.value // Generate invoice number

            val calculatedTotalCost = items.sumOf { it.qty * it.stock.cost } // Calculate total line cost

            val header = InvoiceHeader( // Create invoice header
                invoiceNum = invoiceNum,
                accountCode = debtor.accountCode,
                date = Date(),
                totalSellAmtExVat = _totalExVat.value,
                vat = _vat.value,
                totalCost = calculatedTotalCost
            )

            val detailItems = items.mapIndexed { index, invoiceItem ->
                InvoiceDetail( // Create invoice details
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

            repository.processInvoice(header, detailItems) // Process invoice

            _toastMessage.value = "Invoice Processed!"
            clearInvoice()
        }
    }

    private fun clearInvoice() { // Clear invoice
        _invoiceItems.value = emptyList()
        _selectedDebtor.value = null
        _invoiceNum.value = (System.currentTimeMillis() % 1000000).toInt()
        calculateTotals()
    }

    fun updateInvoiceItem(stock: StockMaster, newQty: Int, newDiscount: Double) { // Update invoice item
        if (newQty <= 0) {
            val itemToRemove = _invoiceItems.value.find { it.stock.stockCode == stock.stockCode }
            if (itemToRemove != null) removeFromInvoice(itemToRemove)// If user sets qty to 0, remove it
            _toastMessage.value = "Quantity cannot be 0"
            return
        }

        if (newDiscount !in 0.0..100.0){
            _toastMessage.value = "Discount must be between 0 and 100"
            return
        }

        val grossTotal = stock.sellingPrice * newQty// Calculate total before VAT
        val discountAmt = grossTotal * (newDiscount / 100) // Calculate discount amount
        val finalTotal = grossTotal - discountAmt // Calculate total after VAT

        val currentList = _invoiceItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.stock.stockCode == stock.stockCode }

        if (index != -1) {
            // Replace the item with new values
            currentList[index] = InvoiceItem(
                stock = stock,
                qty = newQty,
                discountPercent = newDiscount,
                discountAmount = discountAmt,
                lineTotal = finalTotal
            )
            _invoiceItems.value = currentList
            calculateTotals()
        }
    }
    fun clearToast() { _toastMessage.value = null }
}