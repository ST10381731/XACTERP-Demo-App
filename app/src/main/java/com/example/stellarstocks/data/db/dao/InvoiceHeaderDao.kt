package com.example.stellarstocks.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.stellarstocks.data.db.models.InvoiceHeader

@Dao
interface InvoiceHeaderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceHeader(header: InvoiceHeader): Long // returns the id of the inserted row

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceHeaders(invoiceHeaders: List<InvoiceHeader>) // inserts a list of invoice headers
}