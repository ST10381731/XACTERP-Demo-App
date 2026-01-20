package com.example.stellarstocks.data.entity.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "debtor_transaction",
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
data class DebtorTransaction(
    @PrimaryKey val id: Int,
    val accountCode: String,
    val date: Date,
    val transactionType: String,
    val documentNo: Int,
    val grossTransactionValue: Double = 0.0,
    val vatValue: Double = 0.0
)