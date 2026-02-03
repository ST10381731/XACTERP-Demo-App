package com.example.stellarstocks.data.db.models

import java.util.Date

data class DebtorTransactionInfo( //data class for join table data between stock transaction and debtor master
    val date: Date,
    val documentNum: Int,
    val transactionType: String,
    val value: Double,
    val items: String?
)
