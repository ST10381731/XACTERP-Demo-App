package com.example.stellarstocks.data.db.repository

import com.example.stellarstocks.data.db.models.*
import kotlinx.coroutines.flow.Flow

interface StellarStocksRepository {
    // Debtors
    suspend fun insertDebtor(debtor: DebtorMaster)
    fun getAllDebtors(): Flow<List<DebtorMaster>>
    suspend fun getDebtor(code: String): DebtorMaster?
    fun getDebtorTransactions(code: String): Flow<List<DebtorTransaction>>

    // Stock
    suspend fun insertStock(stock: StockMaster)
    fun getAllStock(): Flow<List<StockMaster>>
    suspend fun getStock(code: String): StockMaster?
    fun getStockTransactions(code: String): Flow<List<StockTransaction>>

    // Invoicing
    suspend fun processInvoice(header: InvoiceHeader, items: List<InvoiceDetail>)
}