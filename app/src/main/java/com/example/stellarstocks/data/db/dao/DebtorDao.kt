package com.example.stellarstocks.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.stellarstocks.data.db.models.DebtorMaster
import com.example.stellarstocks.data.db.models.DebtorTransaction
import com.example.stellarstocks.data.db.models.DebtorTransactionInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtorDao {
    // Debtor Master File
    @Query("SELECT accountCode FROM debtor_master ORDER BY accountCode DESC LIMIT 1")
    suspend fun getHighestAccountCode(): String? // Get the highest accountCode
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtor(debtor: DebtorMaster) // Insert a new Debtor

    @Update
    suspend fun updateDebtor(debtor: DebtorMaster)

    @Query("UPDATE debtor_master SET isActive = 0 WHERE accountCode = :code")
    suspend fun deleteDebtor(code: String) // Delete a Debtor

    @Query("SELECT * FROM debtor_master WHERE isActive = 1 ORDER BY accountCode ASC")
    fun getAllDebtors(): Flow<List<DebtorMaster>> // Get all Debtors

    @Query("SELECT * FROM debtor_master WHERE accountCode = :code")
    suspend fun getDebtor(code: String): DebtorMaster? // Get a Debtor by accountCode

    @Query("""
        UPDATE debtor_master 
        SET balance = balance + :totalInclVat, 
            salesYearToDate = salesYearToDate + :salesExVat, 
            costYearToDate = costYearToDate + :totalCost 
        WHERE accountCode = :code
    """)
    suspend fun updateDebtorFinancials(code: String, totalInclVat: Double, salesExVat: Double, totalCost: Double) // Update debtor master when invoice confirmed

    // Debtor Transaction
    @Insert
    suspend fun insertTransaction(transaction: DebtorTransaction) // Insert a new Debtor Transaction

    @Query("SELECT * FROM debtor_transaction WHERE accountCode = :code") // Get Debtor Transactions by accountCode
    fun getDebtorTransactions(code: String): Flow<List<DebtorTransaction>>

    @Query("""
    SELECT dt.date, dt.documentNo AS documentNum, dt.transactionType, dt.grossTransactionValue AS value,
           (SELECT GROUP_CONCAT(sm.stockDescription, ', ') FROM invoice_items ii JOIN stock_master sm ON ii.stockCode = sm.stockCode WHERE ii.invoiceNum = dt.documentNo) AS items
    FROM debtor_transaction dt
    WHERE dt.accountCode = :accountCode
    """)
    fun getDebtorTransactionInfo(accountCode: String): Flow<List<DebtorTransactionInfo>> // Get transaction info for a debtor
}