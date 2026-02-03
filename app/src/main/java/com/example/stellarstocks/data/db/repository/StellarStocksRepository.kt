package com.example.stellarstocks.data.db.repository

import com.example.stellarstocks.data.db.models.*
import kotlinx.coroutines.flow.Flow

interface StellarStocksRepository {
    // Debtors
    suspend fun insertDebtor(debtor: DebtorMaster)
    suspend fun deleteDebtor(code: String)
    fun getAllDebtors(): Flow<List<DebtorMaster>>
    suspend fun getDebtor(code: String): DebtorMaster?
    suspend fun getLastDebtorCode(): String?
    //Debtor Transaction
    fun getDebtorTransactionInfo(accountCode: String): Flow<List<DebtorTransactionInfo>>

    // Stock
    suspend fun insertStock(stock: StockMaster)
    fun getAllStock(): Flow<List<StockMaster>>
    suspend fun getStock(code: String): StockMaster?
    suspend fun deleteStock(code: String)
    suspend fun getLastStockCode(): String?


    // Stock Transaction
    fun getStockTransactions(code: String): Flow<List<StockTransaction>>
    suspend fun adjustStock(transaction: StockTransaction)

    //join table data
    suspend fun getMostRecentDebtorForStock(code: String): String?
    fun getTransactionInfoForStock(stockCode: String): Flow<List<TransactionInfo>>

    // Invoicing
    suspend fun processInvoice(header: InvoiceHeader, items: List<InvoiceDetail>)
}