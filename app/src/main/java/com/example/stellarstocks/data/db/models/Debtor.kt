package com.example.stellarstocks.data.db.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "debtor_master")
data class DebtorMaster(
    @PrimaryKey val accountCode: String,
    val name: String,
    val address1: String,
    val address2: String,
    val balance: Double = 0.0,
    val salesYearToDate: Double = 0.0,
    val costYearToDate: Double = 0.0,
    val isActive: Boolean = true,

    val salesLastYear: Double = 0.0,
    val costLastYear: Double = 0.0,
    val financialYear: Int = Calendar.getInstance().get(Calendar.YEAR)
)