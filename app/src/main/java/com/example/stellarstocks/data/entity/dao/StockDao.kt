package com.example.stellarstocks.data.entity.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.stellarstocks.data.entity.models.StockMaster
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: StockMaster)
    @Update
    suspend fun updateStock(stock: StockMaster)

    @Query("SELECT * FROM stock_master ORDER BY stockCode ASC")
    fun getAllStock(): Flow<List<StockMaster>>

    @Query ("SELECT * FROM stock_master WHERE stockCode = :stockCode")
    fun getStockByStockCode(stockCode: String): Flow<StockMaster>

}


