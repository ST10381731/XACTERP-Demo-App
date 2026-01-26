package com.example.stellarstocks.data.db.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "debtor_transaction",
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
data class DebtorTransaction(
    @PrimaryKey (autoGenerate = true) val id: Int=0,
    val accountCode: String,
    val date: Date,
    val transactionType: String,
    val documentNo: Int,
    val grossTransactionValue: Double = 0.0,
    val vatValue: Double = 0.0
)