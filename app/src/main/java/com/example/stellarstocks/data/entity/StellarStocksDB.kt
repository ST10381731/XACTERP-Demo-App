package com.example.stellarstocks.data.entity

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.stellarstocks.data.entity.models.DebtorTransaction
import com.example.stellarstocks.data.entity.models.InvoiceDetail
import com.example.stellarstocks.data.entity.models.InvoiceHeader
import com.example.stellarstocks.data.entity.models.StockMaster
import com.example.stellarstocks.data.entity.models.StockTransaction
import com.example.stellarstocks.data.entity.models.DebtorMaster
import com.example.stellarstocks.data.entity.dao.DebtorDao
import com.example.stellarstocks.data.entity.dao.InvoiceDetailDao
import com.example.stellarstocks.data.entity.dao.InvoiceHeaderDao
import com.example.stellarstocks.data.entity.dao.StockDao
import com.example.stellarstocks.data.entity.dao.StockTransactionDao
import com.example.stellarstocks.data.entity.dao.DebtorTransactionDao

@Database(entities = [
    StockMaster::class,
    DebtorMaster::class,
    InvoiceHeader::class,
    InvoiceDetail::class,
    DebtorTransaction::class,
    StockTransaction::class
], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class StellarStocksDB : RoomDatabase() {

abstract fun stockDao(): StockDao
abstract fun debtorDao(): DebtorDao
abstract fun invoiceHeaderDao(): InvoiceHeaderDao
abstract fun invoiceDetailDao(): InvoiceDetailDao
abstract fun debtorTransactionDao(): DebtorTransactionDao
abstract fun stockTransactionDao(): StockTransactionDao


    companion object {
        @Volatile
        private var INSTANCE: StellarStocksDB? = null

        fun getDatabase(context: Context): StellarStocksDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StellarStocksDB::class.java,
                    "stellar_stocks_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}