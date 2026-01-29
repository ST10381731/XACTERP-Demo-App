package com.example.stellarstocks.data.db.models

import java.util.Date

data class TransactionInfo(
    val date: Date,
    val accountCode: String?,
    val documentNum: Int,
    val transactionType: String,
    val qty: Int,
    val value: Double
)
