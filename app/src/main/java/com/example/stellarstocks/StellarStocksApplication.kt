package com.example.stellarstocks

import android.app.Application
import com.example.stellarstocks.data.db.StellarStocksDatabase
import com.example.stellarstocks.data.db.repository.StellarStocksRepository
import com.example.stellarstocks.data.db.repository.StellarStocksRepositoryImpl
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class StellarStocksApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { StellarStocksDatabase.getDatabase(this, applicationScope) } // lazy initialisation of database


    val repository: StellarStocksRepository by lazy { // lazy initialisation of repository
        StellarStocksRepositoryImpl( // StellarStocksRepositoryImpl is a class that implements StellarStocksRepository
            database,
            database.debtorDao(),
            database.stockDao(),
            database.invoiceHeaderDao(),
            database.invoiceDetailDao()
        )
    }

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch(Dispatchers.IO) {

            database.openHelper.writableDatabase
        }
    }
}