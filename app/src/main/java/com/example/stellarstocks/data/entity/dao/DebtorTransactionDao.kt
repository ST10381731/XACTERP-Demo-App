package com.example.stellarstocks.data.entity.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stellarstocks.data.entity.models.DebtorTransaction
import kotlinx.coroutines.flow.Flow
import java.util.Date



@Dao
interface DebtorTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtorTransaction(debtorTransaction: DebtorTransaction)

    @Query ("SELECT * FROM debtor_transaction WHERE accountCode = :accountCode")
    fun getDebtorTransactionByAccountCode(accountCode: String): Flow<List<DebtorTransaction>>

    @Query ( "SELECT * FROM debtor_transaction WHERE date = :date")
    fun getDebtorTransactionByDate(date: Date): Flow<List<DebtorTransaction>>


}