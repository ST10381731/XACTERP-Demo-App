package com.example.stellarstocks.data.entity.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stellarstocks.data.entity.models.InvoiceHeader
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceHeaderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceHeader(invoiceHeader: InvoiceHeader)

    @Query( "SELECT * FROM invoice_header WHERE invoiceNum= :invoiceNum AND accountCode = :accountCode")
    fun getInvoiceHeaderByAccountCode(invoiceNum: Int,accountCode: String): Flow<List<InvoiceHeader>>
}