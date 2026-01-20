package com.example.stellarstocks.data.entity.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stellarstocks.data.entity.models.InvoiceDetail
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDetailDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceDetail(invoiceDetail: InvoiceDetail)

    @Query( "SELECT * FROM invoice_detail WHERE invoiceNum = :invoiceNum AND stockCode = :stockCode")
    fun getInvoiceDetailByInvoiceNum(invoiceNum: Int, stockCode: String): Flow<List<InvoiceDetail>>

}