package com.example.stellarstocks.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.stellarstocks.data.db.models.DebtorMaster
import com.example.stellarstocks.data.db.models.DebtorTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtorDao {
    // Debtor Master File
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtor(debtor: DebtorMaster)

    @Delete
    suspend fun deleteDebtor(debtor: DebtorMaster)

    @Query("SELECT * FROM debtor_master")
    fun getAllDebtors(): Flow<List<DebtorMaster>>

    @Query("SELECT * FROM debtor_master WHERE accountCode = :code")
    suspend fun getDebtor(code: String): DebtorMaster?

    @Query("UPDATE debtor_master SET balance = balance + :amount, salesYearToDate = salesYearToDate + :amount WHERE accountCode = :code")
    suspend fun updateBalance(code: String, amount: Double)
    // Debtor Transaction
    @Insert
    suspend fun insertTransaction(transaction: DebtorTransaction)

    @Query("SELECT * FROM debtor_transaction WHERE accountCode = :code")
    fun getDebtorTransactions(code: String): Flow<List<DebtorTransaction>>
}