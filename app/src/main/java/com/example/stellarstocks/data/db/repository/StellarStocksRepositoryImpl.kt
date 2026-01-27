package com.example.stellarstocks.data.db.repository

import com.example.stellarstocks.data.db.dao.DebtorDao
import com.example.stellarstocks.data.db.dao.InvoiceDetailDao
import com.example.stellarstocks.data.db.dao.InvoiceHeaderDao
import com.example.stellarstocks.data.db.dao.StockDao
import com.example.stellarstocks.data.db.models.DebtorMaster
import com.example.stellarstocks.data.db.models.DebtorTransaction
import com.example.stellarstocks.data.db.models.InvoiceDetail
import com.example.stellarstocks.data.db.models.InvoiceHeader
import com.example.stellarstocks.data.db.models.StockMaster
import com.example.stellarstocks.data.db.models.StockTransaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StellarStocksRepositoryImpl @Inject constructor(
    private val debtorDao: DebtorDao,
    private val stockDao: StockDao,
    private val invoiceHeaderDao: InvoiceHeaderDao,
    private val invoiceDetailDao: InvoiceDetailDao
) : StellarStocksRepository {

    override suspend fun insertDebtor(debtor: DebtorMaster) = debtorDao.insertDebtor(debtor)

    override suspend fun deleteDebtor(debtor: DebtorMaster) = debtorDao.deleteDebtor(debtor)

    override fun getAllDebtors(): Flow<List<DebtorMaster>> = debtorDao.getAllDebtors()

    override suspend fun getDebtor(code: String): DebtorMaster? = debtorDao.getDebtor(code)

    override fun getDebtorTransactions(code: String): Flow<List<DebtorTransaction>> = debtorDao.getDebtorTransactions(code)


    override suspend fun insertStock(stock: StockMaster) = stockDao.insertStock(stock)

    override fun getAllStock(): Flow<List<StockMaster>> = stockDao.getAllStock()

    override suspend fun getStock(code: String): StockMaster? = stockDao.getStock(code)

    override fun getStockTransactions(code: String): Flow<List<StockTransaction>> = stockDao.getStockTransactions(code)

    override suspend fun adjustStock(transaction: StockTransaction) {
        stockDao.performAdjustment(transaction)
    }

    override suspend fun processInvoice(header: InvoiceHeader, items: List<InvoiceDetail>) {
        invoiceHeaderDao.insertInvoiceHeaders(listOf(header))
        invoiceDetailDao.insertInvoiceDetails(items)
    }
}