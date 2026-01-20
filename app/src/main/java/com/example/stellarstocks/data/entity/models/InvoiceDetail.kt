package com.example.stellarstocks.data.entity.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoice_detail",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceHeader::class,
            parentColumns = arrayOf("invoiceNum"),
            childColumns = arrayOf("invoiceNum"),
            onUpdate = ForeignKey.Companion.CASCADE,
            onDelete = ForeignKey.Companion.CASCADE
        )
    ]
)
data class InvoiceDetail(
    @PrimaryKey(autoGenerate = true) val invoiceNum: Int=0,
    val itemNum: Int,
    val stockCode: String,
    val qtySold: Double=0.0,
    val unitCost: Double=0.0,
    val unitSell: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0
)