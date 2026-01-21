package com.example.stellarstocks.data.db.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "stock_transaction",
    foreignKeys = [
        ForeignKey(
            entity = StockMaster::class,
            parentColumns = arrayOf("stockCode"),
            childColumns = arrayOf("stockCode"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],indices = [Index(value = ["stockCode"])]
)
data class StockTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int=0,
    val stockCode: String,
    val date: Date,
    val transactionType: String,
    val documentNum: String,
    val qty: Int,
    val unitCost: Double = 0.0,
    val unitSell: Double = 0.0
)
