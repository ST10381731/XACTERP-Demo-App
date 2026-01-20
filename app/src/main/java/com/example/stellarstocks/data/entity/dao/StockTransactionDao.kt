package com.example.stellarstocks.data.entity.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stellarstocks.data.entity.models.StockTransaction
import kotlinx.coroutines.flow.Flow
import java.util.Date


@Dao
interface StockTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockTransaction(stockTransaction: StockTransaction)

    @Query ("SELECT * FROM stock_transaction WHERE stockCode= :stockCode")
    fun getDebtorTransactionByStockCode(stockCode: String): Flow<List<StockTransaction>>

    @Query ( "SELECT * FROM stock_transaction WHERE date = :date")
    fun getStockTransactionByDate(date: Date): Flow<List<StockTransaction>>

}