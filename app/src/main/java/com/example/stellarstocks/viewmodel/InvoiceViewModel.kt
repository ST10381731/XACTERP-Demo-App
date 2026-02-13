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
import kotlin.math.abs

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

    private val _totalExVat = MutableStateFlow(0.0) // variable to hold sum of all line totals
    val totalExVat = _totalExVat.asStateFlow()

    private val _vat = MutableStateFlow(0.0) // VAT amount
    val vat = _vat.asStateFlow()

    private val _grandTotal = MutableStateFlow(0.0) // Total Ex Vat + VAT
    val grandTotal = _grandTotal.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null) // variable for toast message
    val toastMessage = _toastMessage.asStateFlow()

    private val _invoiceNum = MutableStateFlow(0) // variable to display next invoice ID
    val invoiceNum = _invoiceNum.asStateFlow()

    private val _isInvoiceProcessed = MutableStateFlow(false) // Invoice check variable
    val isInvoiceProcessed = _isInvoiceProcessed.asStateFlow()

    init {
        fetchNextInvoiceId() // Load the next ID immediately on startup
    }

    private fun fetchNextInvoiceId() { // Fetch next invoice ID
        viewModelScope.launch {
            _invoiceNum.value = repository.getNextInvoiceNum()
        }
    }

    fun setDebtor(debtor: DebtorMaster) { // function to set debtor for invoice
        _selectedDebtor.value = debtor
    }

    private fun areDiscountsEqual(d1: Double, d2: Double): Boolean {
        // function to check if discounts are equal to combine duplicate invoice items
        return abs(d1 - d2) < 0.001
    }

    fun addToInvoice(stock: StockMaster, qty: Int, discountPercent: Double) {
        if (qty <= 0) { // Check if quantity is valid
            _toastMessage.value = "Quantity must be greater than 0"
            return
        }
        if (discountPercent !in 0.0..100.0) {
            // discount cannot be greater than 100% or less than 0%
            _toastMessage.value = "Discount must be between 0% and 100%"
            return
        }

        val currentList = ArrayList(_invoiceItems.value)


        val totalQtyInInvoice = currentList // Calculate total quantity of a specific stock currently in the invoice
            .filter { it.stock.stockCode == stock.stockCode }
            .sumOf { it.qty }


        if ((totalQtyInInvoice + qty) > stock.stockOnHand) {
            // Check if new quantity exceeds available stock
            _toastMessage.value = "Insufficient stock! Total in invoice: $totalQtyInInvoice. Remaining: ${stock.stockOnHand - totalQtyInInvoice}"
            return
        }


        val existingItemIndex = currentList.indexOfFirst {
            // Check if item already exists in invoice with same discount
            it.stock.stockCode == stock.stockCode && it.discountPercent == discountPercent
        }

        val grossTotal = stock.sellingPrice * qty // Calculate gross total per line
        val discountAmt = grossTotal * (discountPercent / 100) // Calculate discount amount
        val finalTotal = grossTotal - discountAmt // Calculate final total per line

        if (existingItemIndex != -1) { // If item already exists in invoice

            val existingItem = currentList[existingItemIndex]
            val newQty = existingItem.qty + qty // merge quantities


            val newGross = stock.sellingPrice * newQty // Calculate new gross total per line
            val newDiscAmt = newGross * (discountPercent / 100) // Calculate new discount amount
            val newFinal = newGross - newDiscAmt // Calculate final total per line

            currentList[existingItemIndex] = InvoiceItem(stock, newQty, discountPercent, newDiscAmt, newFinal)
            // update existing item
        } else {
            currentList.add(InvoiceItem(stock, qty, discountPercent, discountAmt, finalTotal))
            // add new item to list
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
        val exVat = _invoiceItems.value.sumOf { it.lineTotal } // Calculate total sales excl vat
        val vatCalc = exVat * 0.15 // 15% VAT calculation
        val total = exVat + vatCalc // all line totals +vat

        _totalExVat.value = exVat // update total sales excl vat for display
        _vat.value = vatCalc // update vat for display
        _grandTotal.value = total // update grand total for display
    }

    fun updateInvoiceItem(originalItem: InvoiceItem, newQty: Int, newDiscount: Double) { // editing an item in invoice
        // Update an item in the invoice
        if (newQty <= 0) { // check if quantity is valid
            removeFromInvoice(originalItem)
            _toastMessage.value = "Item removed (Qty 0)"
            return
        }
        if (newDiscount !in 0.0..100.0) {
            _toastMessage.value = "Discount must be between 0 and 100"
            return
        }

        val currentItems = _invoiceItems.value

        val otherLinesQty = currentItems // Get quantity of other lines with the same stock code
            .filter { it.stock.stockCode == originalItem.stock.stockCode && it != originalItem }
            .sumOf { it.qty }


        if ((otherLinesQty + newQty) > originalItem.stock.stockOnHand) {
            // Check if new quantity exceeds available stock in stock  master
            _toastMessage.value = "Insufficient stock! Other items have $otherLinesQty. Available : ${originalItem.stock.stockOnHand - otherLinesQty}"
            return
        }

        val mergeTarget = currentItems.find { // Check if the edit creates a duplicate line
            it != originalItem && it.stock.stockCode == originalItem.stock.stockCode &&
                    areDiscountsEqual(it.discountPercent, newDiscount)
        }

        if (mergeTarget != null) { // If merge stock is not null
            val listWithoutOriginal = currentItems.filter { it != originalItem } // Remove original item

            _invoiceItems.value = listWithoutOriginal.map { item -> // update list
                if (item == mergeTarget) { // if merge is possible
                    val combinedQty = item.qty + newQty // combine quantities
                    val newGross = item.stock.sellingPrice * combinedQty // Calculate new merged gross total
                    val newDiscAmt = newGross * (newDiscount / 100) // Calculate new merged discount amount
                    val newFinal = newGross - newDiscAmt // Calculate new merged final total
                    item.copy(qty = combinedQty, discountAmount = newDiscAmt, lineTotal = newFinal)
                // return new item with updated values
                } else {
                    item // return original item
                }
            }
        } else { // If merge is not possible
            _invoiceItems.value = currentItems.map { item -> // update item list
                if (item == originalItem) { // if original item
                    val grossTotal = item.stock.sellingPrice * newQty // Calculate original gross total
                    val discountAmt = grossTotal * (newDiscount / 100) // Calculate original discount amount
                    val finalTotal = grossTotal - discountAmt // Calculate original final total
                    item.copy(qty = newQty, discountPercent = newDiscount, discountAmount = discountAmt, lineTotal = finalTotal)
                    // return new item with updated values
                } else {
                    item // return the original item
                }
            }
        }
        calculateTotals()
    }

    fun confirmInvoice() { // Confirm invoice and process changes to db tables
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

        val stockGroups = items.groupBy { it.stock.stockCode } // group items by stock code
        for ((_, groupItems) in stockGroups) { // for each item in group
            val totalRequired = groupItems.sumOf { it.qty } // calculate total stock required for items
            val stockOnHand = groupItems.first().stock.stockOnHand // get stock on hand for first item in group
            if (totalRequired > stockOnHand) { // check if stock is sufficient for group
                _toastMessage.value = "Insufficient stock for ${groupItems.first().stock.stockDescription}. Req: $totalRequired, Has: $stockOnHand"
                return
            }
        }

        viewModelScope.launch {
            val calculatedTotalCost = items.sumOf { (it.qty * it.stock.cost) } // calculate total cost for invoice

            // create Header
            val header = InvoiceHeader(
                invoiceNum = 0,
                accountCode = debtor.accountCode,
                date = Date(),
                totalSellAmtExVat = _totalExVat.value,
                vat = _vat.value,
                totalCost = calculatedTotalCost
            )

            // create details
            val detailItems = items.mapIndexed { index, invoiceItem ->
                InvoiceDetail(
                    invoiceNum = 0,
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
        fetchNextInvoiceId()
        _isInvoiceProcessed.value = false
        calculateTotals()
    }

    fun clearToast() { _toastMessage.value = null } // function to clear toast
}