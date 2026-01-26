package com.example.stellarstocks.data.db.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "invoice_header",
    foreignKeys = [
        ForeignKey(
            entity = DebtorMaster::class,
            parentColumns = arrayOf("accountCode"),
            childColumns = arrayOf("accountCode"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],indices = [Index(value = ["accountCode"])]
)
data class InvoiceHeader(
    @PrimaryKey(autoGenerate = true) val invoiceNum: Int=0,
    val accountCode: String,
    val date: Date,
    val totalSellAmtExVat: Double,
    val vat: Double = 0.0,
    val totalCost: Double = 0.0
)