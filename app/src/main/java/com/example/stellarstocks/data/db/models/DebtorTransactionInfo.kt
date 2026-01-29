package com.example.stellarstocks.data.db.models

import java.util.Date

data class DebtorTransactionInfo(
    val date: Date,
    val documentNum: Int,
    val transactionType: String,
    val value: Double,
    val items: String?
)
