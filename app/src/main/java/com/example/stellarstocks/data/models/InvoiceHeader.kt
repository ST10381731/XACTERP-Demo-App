package com.example.stellarstocks.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.sql.Date

@Entity(
    tableName = "invoice_header",
    foreignKeys = [
        ForeignKey(
            entity = DebtorMaster::class,
            parentColumns = arrayOf("accountCode"),
            childColumns = arrayOf("accountCode"),
            onUpdate = ForeignKey.Companion.CASCADE,
            onDelete = ForeignKey.Companion.CASCADE
        )
    ]
)
data class InvoiceHeader(
    @PrimaryKey val invoiceNum: Int,
    val accountCode: String,
    val date: Date,
    val totalSellAmtExVat: Double,
    val vat: Double = 0.0,
    val totalCost: Double = 0.0
)