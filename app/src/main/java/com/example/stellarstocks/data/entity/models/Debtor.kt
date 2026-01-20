package com.example.stellarstocks.data.entity.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debtor_master")
data class DebtorMaster(
    @PrimaryKey val accountCode: String,
    val address1: String,
    val address2: String,
    val address3: String,
    val balance: Double = 0.0,
    val salesYearToDate: Double = 0.0,
    val costYearToDate: Double = 0.0
)