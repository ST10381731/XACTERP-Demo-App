package com.example.stellarstocks.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.stellarstocks.data.db.models.InvoiceDetail

@Dao
interface InvoiceDetailDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceDetails(invoiceDetails: List<InvoiceDetail>) // Insert stock per Invoice
}