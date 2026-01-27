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
    suspend fun insertDebtor(debtor: DebtorMaster) // Insert a new Debtor

    @Delete
    suspend fun deleteDebtor(debtor: DebtorMaster) // Delete a Debtor

    @Query("SELECT * FROM debtor_master")
    fun getAllDebtors(): Flow<List<DebtorMaster>> // Get all Debtors

    @Query("SELECT * FROM debtor_master WHERE accountCode = :code")
    suspend fun getDebtor(code: String): DebtorMaster? // Get a Debtor by accountCode

    @Query("UPDATE debtor_master SET balance = balance + :amount, salesYearToDate = salesYearToDate + :amount WHERE accountCode = :code")
    suspend fun updateBalance(code: String, amount: Double) // Update balance and salesYearToDate

    // Debtor Transaction
    @Insert
    suspend fun insertTransaction(transaction: DebtorTransaction) // Insert a new Debtor Transaction

    @Query("SELECT * FROM debtor_transaction WHERE accountCode = :code") // Get Debtor Transactions by accountCode
    fun getDebtorTransactions(code: String): Flow<List<DebtorTransaction>>
}