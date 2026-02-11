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

data class InvoiceItem( // class for invoice item
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

    private val _toastMessage = MutableStateFlow<String?>(null) // variable for toast message
    val toastMessage = _toastMessage.asStateFlow()

    private val _invoiceNum = MutableStateFlow((System.currentTimeMillis() % 1000000).toInt()) // Invoice number
    val invoiceNum = _invoiceNum.asStateFlow()

    private val _isInvoiceProcessed = MutableStateFlow(false) // Invoice processed flag
    val isInvoiceProcessed = _isInvoiceProcessed.asStateFlow()

    fun setDebtor(debtor: DebtorMaster) { // function to set debtor for invoice
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
        val currentList = ArrayList(_invoiceItems.value)


        val totalQtyInInvoice = currentList
            .filter { it.stock.stockCode == stock.stockCode }
            .sumOf { it.qty }


        if ((totalQtyInInvoice + qty) > stock.stockOnHand) {
            _toastMessage.value = "Insufficient stock! Total in invoice: $totalQtyInInvoice. Remaining: ${stock.stockOnHand - totalQtyInInvoice}"
            return
        }


        val existingItemIndex = currentList.indexOfFirst {
            it.stock.stockCode == stock.stockCode && it.discountPercent == discountPercent
        }

        val grossTotal = stock.sellingPrice * qty
        val discountAmt = grossTotal * (discountPercent / 100)
        val finalTotal = grossTotal - discountAmt

        if (existingItemIndex != -1) {

            val existingItem = currentList[existingItemIndex]
            val newQty = existingItem.qty + qty // merge quantities


            val newGross = stock.sellingPrice * newQty // Calculate new gross total
            val newDiscAmt = newGross * (discountPercent / 100) // Calculate new discount amount
            val newFinal = newGross - newDiscAmt // Calculate new final total

            currentList[existingItemIndex] = InvoiceItem(stock, newQty, discountPercent, newDiscAmt, newFinal)
        } else {
            currentList.add(InvoiceItem(stock, qty, discountPercent, discountAmt, finalTotal))
        }

        _invoiceItems.value = currentList
        calculateTotals()
    }

    fun removeFromInvoice(item: InvoiceItem) { // Remove an item from invoice
        val currentList = _invoiceItems.value.toMutableList() // Get current list of items
        currentList.remove(item) // Remove item from list
        _invoiceItems.value = currentList
        calculateTotals()
    }
    private fun calculateTotals() { // Calculate final total for invoice
        val exVat = _invoiceItems.value.sumOf { it.lineTotal }
        val vatCalc = exVat * 0.15 // 15% VAT
        val total = exVat + vatCalc // line totals +vat

        _totalExVat.value = exVat
        _vat.value = vatCalc
        _grandTotal.value = total
    }

    fun updateInvoiceItem(originalItem: InvoiceItem, newQty: Int, newDiscount: Double) { // Update an item in the invoice
        if (newQty <= 0) {
            removeFromInvoice(originalItem)
            _toastMessage.value = "Item removed (Qty 0)"
            return
        }
        if (newDiscount !in 0.0..100.0) {
            _toastMessage.value = "Discount must be between 0 and 100"
            return
        }

        val currentList = _invoiceItems.value.toMutableList()

        val otherLinesQty = currentList // Get quantity of other lines with the same stock code
            .filter { it.stock.stockCode == originalItem.stock.stockCode && it != originalItem }
            .sumOf { it.qty }


        if ((otherLinesQty + newQty) > originalItem.stock.stockOnHand) { // Check if new quantity exceeds available stock
            _toastMessage.value = "Insufficient stock! Other lines have $otherLinesQty. Available for this line: ${originalItem.stock.stockOnHand - otherLinesQty}"
            return
        }

        val index = currentList.indexOf(originalItem) // Find index of original item
        if (index != -1) {
            val grossTotal = originalItem.stock.sellingPrice * newQty // Calculate new gross total
            val discountAmt = grossTotal * (newDiscount / 100) // Calculate new discount amount
            val finalTotal = grossTotal - discountAmt // Calculate new final total

            currentList[index] = InvoiceItem(originalItem.stock, newQty, newDiscount, discountAmt, finalTotal) // Update item in list
            _invoiceItems.value = currentList
            calculateTotals()
        }
    }
    fun confirmInvoice() { // Confirm invoice and process it
        val debtor = _selectedDebtor.value
        val items = _invoiceItems.value

        if (debtor == null) { // check if debtor is null
            _toastMessage.value = "Please select a Debtor"
            return
        }
        if (items.isEmpty()) { // check if item list is empty
            _toastMessage.value = "Invoice is empty"
            return
        }

        val stockGroups = items.groupBy { it.stock.stockCode }
        for ((_, groupItems) in stockGroups) {
            val totalRequired = groupItems.sumOf { it.qty }
            val stockOnHand = groupItems.first().stock.stockOnHand
            if (totalRequired > stockOnHand) {
                _toastMessage.value = "Insufficient stock for ${groupItems.first().stock.stockDescription}. Req: $totalRequired, Has: $stockOnHand"
                return
            }
        }

        viewModelScope.launch {
            val invoiceNum = _invoiceNum.value
            val calculatedTotalCost = items.sumOf { it.qty * it.stock.cost }

            val header = InvoiceHeader(
                invoiceNum = invoiceNum,
                accountCode = debtor.accountCode,
                date = Date(),
                totalSellAmtExVat = _totalExVat.value,
                vat = _vat.value,
                totalCost = calculatedTotalCost
            )

            val detailItems = items.mapIndexed { index, invoiceItem ->
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

            repository.processInvoice(header, detailItems)

            _toastMessage.value = "Invoice Processed Successfully!"
            _isInvoiceProcessed.value = true
        }
    }


    fun startNewInvoice() { // Start a new invoice
        _invoiceItems.value = emptyList()
        _selectedDebtor.value = null
        _invoiceNum.value = (System.currentTimeMillis() % 1000000).toInt()
        _isInvoiceProcessed.value = false
        calculateTotals()
    }

    fun clearToast() { _toastMessage.value = null } // function to clear toast
}