package com.example.stellarstocks.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.stellarstocks.data.db.models.StockMaster
import com.example.stellarstocks.data.db.models.StockTransaction
import com.example.stellarstocks.data.db.models.TransactionInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    // Stock Master File
    @Query("SELECT stockCode FROM stock_master ORDER BY stockCode DESC LIMIT 1")
    suspend fun getHighestStockCode(): String?
    // Get the highest stockCode

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: StockMaster)
    // Insert a new Stock

    @Update
    suspend fun updateStock(stock: StockMaster)
    // Update a Stock

    @Query("UPDATE stock_master SET isActive = 0 WHERE stockCode = :code")
    suspend fun deleteStock(code: String)
    // Delete a Stock

    @Query("SELECT * FROM stock_master WHERE isActive = 1")
    fun getAllStock(): Flow<List<StockMaster>>
    // Get all Stocks

    @Query("SELECT * FROM stock_master WHERE stockCode = :code")
    suspend fun getStock(code: String): StockMaster?
    // Get a Stock by stockCode

    @Query("UPDATE stock_master SET stockOnHand = stockOnHand + :qty WHERE stockCode = :code")
    suspend fun updateStockQty(code: String, qty: Int)
    // Update stockOnHand

    // Stock Transaction
    @Insert
    suspend fun insertTransaction(transaction: StockTransaction)
    // Insert a new Stock Transaction

    @Query("SELECT * FROM stock_transaction WHERE stockCode = :code")
    fun getStockTransactions(code: String): Flow<List<StockTransaction>>
    // Get Stock Transactions by stockCode

    @Query("SELECT MAX(documentNum) FROM stock_transaction WHERE transactionType IN ('Adjustment', 'Purchase')")
    suspend fun getMaxAdjustmentDocNum(): Int?
    // Get the highest adjustment document number

    @Transaction
    @Query("""
        SELECT t.id AS transactionId, t.date, h.accountCode, t.documentNum, t.transactionType, t.qty, 
               (ABS(t.qty) * CASE WHEN t.transactionType = 'Invoice' THEN t.unitSell ELSE t.unitCost END) as value
        FROM stock_transaction t
        LEFT JOIN invoice_items i ON t.documentNum = i.invoiceNum AND t.stockCode = i.stockCode
        LEFT JOIN invoice_header h ON i.invoiceNum = h.invoiceNum
        WHERE t.stockCode = :stockCode
        GROUP BY t.id  
        ORDER BY t.date DESC
    """)
    fun getTransactionInfoForStock(stockCode: String): Flow<List<TransactionInfo>>
    // Get transaction info for a stock using a join query

    @Query("""
        UPDATE stock_master 
        SET stockOnHand = stockOnHand + :qty, 
            qtyPurchased = qtyPurchased + :qty, 
            totalPurchasesExclVat = totalPurchasesExclVat + :purchaseValue
        WHERE stockCode = :code
    """)
    suspend fun recordStockPurchase(code: String, qty: Int, purchaseValue: Double)
    // Record a stock purchase for perform adjustment query

    @Transaction
    suspend fun performAdjustment(transaction: StockTransaction) {
        // Perform a stock adjustment
        if (transaction.transactionType == "Purchase") {
            val purchaseValue = transaction.qty * transaction.unitCost
            recordStockPurchase(transaction.stockCode, transaction.qty, purchaseValue)
        } else {
            updateStockQty(transaction.stockCode, transaction.qty)
        }
        insertTransaction(transaction)
    }

    @Query("""
        UPDATE stock_master 
        SET stockOnHand = stockOnHand - :qtySold, 
            qtySold = qtySold + :qtySold, 
            totalSalesExclVat = totalSalesExclVat + :saleAmount 
        WHERE stockCode = :code
    """)
    suspend fun recordStockSale(code: String, qtySold: Int, saleAmount: Double)
    // Update the stock master total sales and qty sold per invoice item
}