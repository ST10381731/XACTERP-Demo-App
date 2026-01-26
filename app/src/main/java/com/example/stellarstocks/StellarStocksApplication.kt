package com.example.stellarstocks

import android.app.Application
import com.example.stellarstocks.data.db.StellarStocksDatabase
import com.example.stellarstocks.data.db.repository.StellarStocksRepository
import com.example.stellarstocks.data.db.repository.StellarStocksRepositoryImpl
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StellarStocksApplication : Application() {

    val database by lazy { StellarStocksDatabase.getDatabase(this) }

    val repository: StellarStocksRepository by lazy {
        StellarStocksRepositoryImpl(
            database.debtorDao(),
            database.stockDao(),
            database.invoiceHeaderDao(),
            database.invoiceDetailDao()
        )
    }
}