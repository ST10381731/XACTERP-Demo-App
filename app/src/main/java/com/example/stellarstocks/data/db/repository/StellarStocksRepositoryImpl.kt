package com.example.stellarstocks.data.db.repository

import com.example.stellarstocks.data.db.dao.DebtorDao
import com.example.stellarstocks.data.db.dao.InvoiceDetailDao
import com.example.stellarstocks.data.db.dao.InvoiceHeaderDao
import com.example.stellarstocks.data.db.dao.StockDao
import com.example.stellarstocks.data.db.models.DebtorMaster
import com.example.stellarstocks.data.db.models.DebtorTransaction
import com.example.stellarstocks.data.db.models.DebtorTransactionInfo
import com.example.stellarstocks.data.db.models.InvoiceDetail
import com.example.stellarstocks.data.db.models.InvoiceHeader
import com.example.stellarstocks.data.db.models.StockMaster
import com.example.stellarstocks.data.db.models.StockTransaction
import com.example.stellarstocks.data.db.models.TransactionInfo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StellarStocksRepositoryImpl @Inject constructor(
    private val debtorDao: DebtorDao,
    private val stockDao: StockDao,
    private val invoiceHeaderDao: InvoiceHeaderDao,
    private val invoiceDetailDao: InvoiceDetailDao
) : StellarStocksRepository {

    // Debtors
    override suspend fun insertDebtor(debtor: DebtorMaster) =
        debtorDao.insertDebtor(debtor) // inserts a debtor
    override suspend fun updateDebtor(debtor: DebtorMaster) =
        debtorDao.updateDebtor(debtor)

    override suspend fun deleteDebtor(code: String) { // deletes a debtor
        debtorDao.deleteDebtor(code)
    }

    override fun getAllDebtors(): Flow<List<DebtorMaster>> =
        debtorDao.getAllDebtors() // gets a list of all debtors

    override suspend fun getDebtor(code: String): DebtorMaster? =
        debtorDao.getDebtor(code) // gets a debtor by code

    override suspend fun getLastDebtorCode(): String? { // gets the last debtor code
        return debtorDao.getHighestAccountCode()
    }

    override fun getDebtorTransactionInfo(accountCode: String): Flow<List<DebtorTransactionInfo>> { // gets a list of debtor transaction info according to accountCode
        return debtorDao.getDebtorTransactionInfo(accountCode)
    }

    // Stock
    override suspend fun insertStock(stock: StockMaster) =
        stockDao.insertStock(stock) // inserts a stock

    override suspend fun updateStock(stock: StockMaster) =
        stockDao.updateStock(stock)

    override suspend fun deleteStock(code: String) { // deletes a stock according to stockCode
        stockDao.deleteStock(code)
    }

    override fun getAllStock(): Flow<List<StockMaster>> =
        stockDao.getAllStock() // gets a list of all stocks

    override suspend fun getStock(code: String): StockMaster? =
        stockDao.getStock(code) // gets a stock by stockCode

    override suspend fun getLastStockCode(): String? { // gets the last stock code
        return stockDao.getHighestStockCode()
    }

    override fun getStockTransactions(code: String): Flow<List<StockTransaction>> =
        stockDao.getStockTransactions(code) // gets a list of stock transactions by stockCode


    override suspend fun getMostRecentDebtorForStock(code: String): String? // gets the most recent debtor for stock transaction filter
    {
        return stockDao.getMostRecentDebtorForStock(code)
    }

    override fun getTransactionInfoForStock(stockCode: String): Flow<List<TransactionInfo>> { // gets a list of transaction info by stockCode
        return stockDao.getTransactionInfoForStock(stockCode)
    }

    override suspend fun adjustStock(transaction: StockTransaction) { // adjusts a stock transaction
        stockDao.performAdjustment(transaction)
    }

    override suspend fun processInvoice(header: InvoiceHeader, items: List<InvoiceDetail>) {

        invoiceHeaderDao.insertInvoiceHeaders(listOf(header))
        invoiceDetailDao.insertInvoiceDetails(items)


        debtorDao.updateDebtorFinancials(
            code = header.accountCode,
            totalInclVat = header.totalSellAmtExVat + header.vat,
            salesExVat = header.totalSellAmtExVat,
            totalCost = header.totalCost
        )


        val debtorTrans = DebtorTransaction(
            accountCode = header.accountCode,
            date = header.date,
            transactionType = "Invoice",
            documentNo = header.invoiceNum,
            grossTransactionValue = header.totalSellAmtExVat + header.vat,
            vatValue = header.vat
        )
        debtorDao.insertTransaction(debtorTrans)


        items.forEach { item ->
            val stockItem = stockDao.getStock(item.stockCode)
            if (stockItem != null) {

                stockDao.recordStockSale(
                    code = item.stockCode,
                    qtySold = item.qtySold,
                    saleAmount = item.total
                )

                val stockTrans = StockTransaction(
                    stockCode = item.stockCode,
                    date = header.date,
                    transactionType = "Invoice",
                    documentNum = header.invoiceNum,
                    qty = -item.qtySold,
                    unitCost = stockItem.cost,
                    unitSell = stockItem.sellingPrice
                )
                stockDao.insertTransaction(stockTrans)
            }
        }
    }
}