package com.example.stellarstocks.data.entity.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_master",
)
data class StockMaster(
    @PrimaryKey val stockCode: String,
    val stockDescription: String,
    val cost: Double = 0.0,
    val sellingCost: Double = 0.0,
    val totalPurchasesExclVat: Double = 0.0,
    val totalSalesExclVat: Double = 0.0,
    val qtyPurchased: Double = 0.0,
    val qtySold: Double = 0.0,
    val stockOnHand: Double = 0.0
)
