package com.example.stellarstocks.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.stellarstocks.data.db.models.StockMaster
import com.example.stellarstocks.data.db.models.StockTransaction
import com.example.stellarstocks.data.db.models.TransactionInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    // Stock Master File
    @Query("SELECT stockCode FROM stock_master ORDER BY stockCode DESC LIMIT 1")
    suspend fun getHighestStockCode(): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: StockMaster) // Insert a new Stock

    @Query("UPDATE stock_master SET isActive = 0 WHERE stockCode = :code")
    suspend fun deleteStock(code: String) // Delete a Stock

    @Query("SELECT * FROM stock_master WHERE isActive = 1")
    fun getAllStock(): Flow<List<StockMaster>> // Get all Stocks

    @Query("SELECT * FROM stock_master WHERE stockCode = :code")
    suspend fun getStock(code: String): StockMaster? // Get a Stock by stockCode

    @Query("UPDATE stock_master SET stockOnHand = stockOnHand + :qty WHERE stockCode = :code")
    suspend fun updateStockQty(code: String, qty: Int) // Update stockOnHand

    // Stock Transaction
    @Insert
    suspend fun insertTransaction(transaction: StockTransaction) // Insert a new Stock Transaction

    @Query("SELECT * FROM stock_transaction WHERE stockCode = :code")
    fun getStockTransactions(code: String): Flow<List<StockTransaction>> // Get Stock Transactions by stockCode

    @Query(
        """
    SELECT ih.accountCode
    FROM stock_transaction st
    JOIN invoice_items id ON st.documentNum = id.invoiceNum
    JOIN invoice_header ih ON id.invoiceNum = ih.invoiceNum
    WHERE st.stockCode = :stockCode
    ORDER BY st.date DESC
    LIMIT 1
"""
    )
    suspend fun getMostRecentDebtorForStock(stockCode: String): String? // Get most recent debtor for a stock

    @Query("""
    SELECT st.date, ih.accountCode, st.documentNum, st.transactionType, st.qty, st.unitSell * st.qty AS value
    FROM stock_transaction st
    LEFT JOIN invoice_items ii ON st.documentNum = ii.invoiceNum
    LEFT JOIN invoice_header ih ON ii.invoiceNum = ih.invoiceNum
    WHERE st.stockCode = :stockCode
    """)
    fun getTransactionInfoForStock(stockCode: String): Flow<List<TransactionInfo>> // Get transaction info for a stock

    @Transaction
    suspend fun performAdjustment(transaction: StockTransaction) { // Perform a quantity adjustment on a stock
        updateStockQty(transaction.stockCode, transaction.qty)
        insertTransaction(transaction)
    }
}