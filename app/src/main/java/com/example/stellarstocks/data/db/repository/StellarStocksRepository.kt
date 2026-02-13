package com.example.stellarstocks.data.db.repository

import com.example.stellarstocks.data.db.models.*
import kotlinx.coroutines.flow.Flow
// Interface for StellarStocksRepository acts as a bridge between the database and the viewmodel
interface StellarStocksRepository {
    /* Debtors */
    suspend fun insertDebtor(debtor: DebtorMaster) // inserts a debtor
    suspend fun updateDebtor(debtor: DebtorMaster) // updates a debtor
    suspend fun deleteDebtor(code: String) // deletes a debtor
    fun getAllDebtors(): Flow<List<DebtorMaster>> // gets a list of all debtors for enquiry and graph
    suspend fun getDebtor(code: String): DebtorMaster? // gets a debtor by code for debtor details
    suspend fun getLastDebtorCode(): String? // gets the last debtor code added for auto creation of new account code

    /* Debtor Transaction */
    fun getAllDebtorTransactions(): Flow<List<DebtorTransaction>>
    // gets a list of all debtor transactions for graph
    fun getDebtorTransactionInfo(accountCode: String): Flow<List<DebtorTransactionInfo>>
    // gets a list of debtor transaction info according to accountCode for debtor details filter

    /* Stock */
    suspend fun insertStock(stock: StockMaster) // inserts a new stock
    suspend fun updateStock(stock: StockMaster) // updates a stock
    suspend fun deleteStock(code: String) // deletes a stock
    fun getAllStock(): Flow<List<StockMaster>> // gets a list of all stocks for enquiry and graph
    suspend fun getStock(code: String): StockMaster? // gets a stock by stockCode
    suspend fun getLastStockCode(): String? // gets the last stock code added for auto creation of new stock code


    /* Stock Transaction */
    suspend fun adjustStock(transaction: StockTransaction) // adjusts a stock transaction
    suspend fun getNextAdjustmentNum(): Int
    fun getTransactionInfoForStock(stockCode: String): Flow<List<TransactionInfo>>
    // gets a list of transaction info by stockCode for stock details filter

    /* Invoicing */
    suspend fun getNextInvoiceNum(): Int // gets the next invoice number
    suspend fun processInvoice(header: InvoiceHeader, items: List<InvoiceDetail>) // processes an invoice

}