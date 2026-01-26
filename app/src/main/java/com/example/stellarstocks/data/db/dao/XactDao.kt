package com.example.stellarstocks.data.db.dao

import androidx.room.*
import com.example.stellarstocks.data.db.models.DebtorMaster
import com.example.stellarstocks.data.db.models.DebtorTransaction
import com.example.stellarstocks.data.db.models.InvoiceDetail
import com.example.stellarstocks.data.db.models.InvoiceHeader
import com.example.stellarstocks.data.db.models.StockMaster
import com.example.stellarstocks.data.db.models.StockTransaction

@Dao
interface XactDao {
    // for Seeding
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtors(debtors: List<DebtorMaster>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: List<StockMaster>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceHeaders(headers: List<InvoiceHeader>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceDetails(details: List<InvoiceDetail>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtorTransactions(trans: List<DebtorTransaction>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockTransactions(trans: List<StockTransaction>)
}