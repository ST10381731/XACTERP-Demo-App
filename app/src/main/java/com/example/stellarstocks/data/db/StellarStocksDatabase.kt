package com.example.stellarstocks.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.stellarstocks.data.db.models.DebtorTransaction
import com.example.stellarstocks.data.db.models.InvoiceDetail
import com.example.stellarstocks.data.db.models.InvoiceHeader
import com.example.stellarstocks.data.db.models.StockMaster
import com.example.stellarstocks.data.db.models.StockTransaction
import com.example.stellarstocks.data.db.models.DebtorMaster
import com.example.stellarstocks.data.db.dao.DebtorDao
import com.example.stellarstocks.data.db.dao.InvoiceDao
import com.example.stellarstocks.data.db.dao.StockDao

@Database(entities = [
    StockMaster::class,
    DebtorMaster::class,
    InvoiceHeader::class,
    InvoiceDetail::class,
    DebtorTransaction::class,
    StockTransaction::class
], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class StellarStocksDatabase : RoomDatabase() {

abstract fun stockDao(): StockDao
abstract fun debtorDao(): DebtorDao
abstract fun invoiceDao(): InvoiceDao


    companion object {
        @Volatile
        private var INSTANCE: StellarStocksDatabase? = null

        fun getDatabase(context: Context): StellarStocksDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StellarStocksDatabase::class.java,
                    "stellar_stocks_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}