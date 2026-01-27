package com.example.stellarstocks.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.stellarstocks.data.db.models.StockMaster
import com.example.stellarstocks.data.db.models.StockTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    // Stock Master File
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: StockMaster)

    @Query("SELECT * FROM stock_master")
    fun getAllStock(): Flow<List<StockMaster>>

    @Query("SELECT * FROM stock_master WHERE stockCode = :code")
    suspend fun getStock(code: String): StockMaster?

    @Query("UPDATE stock_master SET stockOnHand = stockOnHand - :qty, qtySold = qtySold + :qty WHERE stockCode = :code")
    suspend fun updateStockLevel(code: String, qty: Int)

    // Stock Transaction
    @Insert
    suspend fun insertTransaction(transaction: StockTransaction)

    @Query("SELECT * FROM stock_transaction WHERE stockCode = :code")
    fun getStockTransactions(code: String): Flow<List<StockTransaction>>

}


