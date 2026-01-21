package com.example.stellarstocks.data.db.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoice_items",
    primaryKeys = ["invoiceNum", "itemNum"],
    foreignKeys = [
        ForeignKey(
            entity = InvoiceHeader::class,
            parentColumns = ["invoiceNum"],
            childColumns = ["invoiceNum"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StockMaster::class,
            parentColumns = ["stockCode"],
            childColumns = ["stockCode"]
        )
    ],
    indices = [Index("stockCode"), Index("invoiceNum")]
)

data class InvoiceDetail(
    val invoiceNum: Int,
    val itemNum: Int,
    val stockCode: String,
    val qtySold: Int,
    val unitCost: Double=0.0,
    val unitSell: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0
)