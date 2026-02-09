package com.example.stellarstocks.data.db.repository

import com.example.stellarstocks.data.db.models.*
import kotlinx.coroutines.flow.Flow

interface StellarStocksRepository { // Interface for StellarStocksRepository acts as a bridge between the database and the viewmodel
    // Debtors
    suspend fun insertDebtor(debtor: DebtorMaster) // inserts a debtor
    suspend fun updateDebtor(debtor: DebtorMaster) // updates a debtor
    suspend fun deleteDebtor(code: String) // deletes a debtor
    fun getAllDebtors(): Flow<List<DebtorMaster>> // gets a list of all debtors
    suspend fun getDebtor(code: String): DebtorMaster? // gets a debtor by code
    suspend fun getLastDebtorCode(): String? // gets the last debtor code added
    fun getAllDebtorTransactions(): Flow<List<DebtorTransaction>> // gets a list of all debtor transactions for graph

    //Debtor Transaction
    fun getDebtorTransactionInfo(accountCode: String): Flow<List<DebtorTransactionInfo>> // gets a list of debtor transaction info according to accountCode

    // Stock
    suspend fun insertStock(stock: StockMaster) // inserts a new stock
    suspend fun updateStock(stock: StockMaster) // updates a stock
    fun getAllStock(): Flow<List<StockMaster>> // gets a list of all stocks
    suspend fun getStock(code: String): StockMaster? // gets a stock by stockCode
    suspend fun deleteStock(code: String) // deletes a stock
    suspend fun getLastStockCode(): String? // gets the last stock code added


    // Stock Transaction
    fun getStockTransactions(code: String): Flow<List<StockTransaction>> // gets a list of stock transactions by stockCode
    suspend fun adjustStock(transaction: StockTransaction) // adjusts a stock transaction

    //join table data
    suspend fun getMostRecentDebtorForStock(code: String): String? // gets the most recent debtor for stock transaction filter
    fun getTransactionInfoForStock(stockCode: String): Flow<List<TransactionInfo>> // gets a list of transaction info by stockCode

    // Invoicing
    suspend fun processInvoice(header: InvoiceHeader, items: List<InvoiceDetail>) // processes an invoice
}