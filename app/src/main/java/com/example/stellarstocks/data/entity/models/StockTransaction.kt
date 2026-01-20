package com.example.stellarstocks.data.entity.models

import androidx.room.Entity
import androidx.room.ForeignKey
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
    ]
)
data class StockTransaction(
    @PrimaryKey val id: Int,
    val stockCode: String,
    val date: Date,
    val transactionType: String,
    val documentNum: Int,
    val qty: Double = 0.0,
    val unitCost: Double = 0.0,
    val unitSold: Double = 0.0
)
