package com.example.stellarstocks.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.stellarstocks.data.db.models.StockMaster
import com.example.stellarstocks.data.db.models.StockTransaction
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

    @Transaction
    suspend fun performAdjustment(transaction: StockTransaction) { // Perform a quantity adjustment on a stock
        updateStockQty(transaction.stockCode, transaction.qty)
        insertTransaction(transaction)
    }
}


