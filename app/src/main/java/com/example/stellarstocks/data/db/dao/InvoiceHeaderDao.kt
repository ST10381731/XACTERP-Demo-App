package com.example.stellarstocks.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stellarstocks.data.db.models.InvoiceHeader

@Dao
interface InvoiceHeaderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceHeader(invoiceHeader: InvoiceHeader): Long
    // inserts a list of invoice headers

    @Query("SELECT MAX(invoiceNum) FROM invoice_header")
    suspend fun getLastInvoiceNum(): Int?
    // get the highest invoice number
}