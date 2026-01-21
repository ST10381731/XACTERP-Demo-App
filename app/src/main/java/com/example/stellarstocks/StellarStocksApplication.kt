package com.example.stellarstocks

import android.app.Application
import com.example.stellarstocks.data.db.StellarStocksDatabase
import com.example.stellarstocks.data.db.repository.StellarStocksRepository
import com.example.stellarstocks.data.db.repository.StellarStocksRepositoryImpl

class StellarStocksApplication : Application() {

    // Lazy initialization: Created only when needed
    val database by lazy { StellarStocksDatabase.getDatabase(this) }

    val repository: StellarStocksRepository by lazy {
        StellarStocksRepositoryImpl(database)
    }
}