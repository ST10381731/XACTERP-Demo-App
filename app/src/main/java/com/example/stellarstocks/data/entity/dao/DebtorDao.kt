package com.example.stellarstocks.data.entity.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stellarstocks.data.entity.models.DebtorMaster
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtor(debtor: DebtorMaster)

    @Update
    suspend fun updateDebtor(debtor: DebtorMaster)

    @Query("SELECT * FROM debtor_master ORDER BY accountCode ASC")
    fun getAllDebtors(): Flow<List<DebtorMaster>>

    @Query("SELECT * FROM debtor_master WHERE accountCode = :accountCode")
    fun getDebtorByAccountCode(accountCode: String): Flow<DebtorMaster>
}