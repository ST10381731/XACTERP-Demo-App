package com.example.stellarstocks.data.db.repository

import androidx.room.util.copy
import androidx.room.withTransaction
import com.example.stellarstocks.data.db.StellarStocksDatabase
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
import java.util.Calendar

class StellarStocksRepositoryImpl @Inject constructor(
    private val db: StellarStocksDatabase,
    private val debtorDao: DebtorDao,
    private val stockDao: StockDao,
    private val invoiceHeaderDao: InvoiceHeaderDao,
    private val invoiceDetailDao: InvoiceDetailDao
) : StellarStocksRepository {

    // Debtors
    override suspend fun insertDebtor(debtor: DebtorMaster) =
        debtorDao.insertDebtor(debtor) // inserts a debtor

    override suspend fun updateDebtor(debtor: DebtorMaster) =
        debtorDao.updateDebtor(debtor) // updates a debtor

    override suspend fun deleteDebtor(code: String) =
        debtorDao.deleteDebtor(code) // deletes a debtor

    override fun getAllDebtors(): Flow<List<DebtorMaster>> =
        debtorDao.getAllDebtors() // gets a list of all debtors

    override suspend fun getDebtor(code: String): DebtorMaster? =
        debtorDao.getDebtor(code) // gets a debtor by code

    override suspend fun getLastDebtorCode(): String? {
        return debtorDao.getHighestAccountCode() // gets the last debtor code added
    }

    override fun getDebtorTransactionInfo(accountCode: String): Flow<List<DebtorTransactionInfo>> {
        return debtorDao.getDebtorTransactionInfo(accountCode) // gets a list of debtor transaction info according to accountCode
    }

    override fun getAllDebtorTransactions(): Flow<List<DebtorTransaction>> {
        return debtorDao.getAllTransactions() //for graphing
    }

    // Stock
    override suspend fun insertStock(stock: StockMaster) =
        stockDao.insertStock(stock) // inserts a stock according to stockCode

    override suspend fun updateStock(stock: StockMaster) =
        stockDao.updateStock(stock) // updates a stock according to stockCode

    override suspend fun deleteStock(code: String) =
        stockDao.deleteStock(code) // deletes a stock according to stockCode

    override fun getAllStock(): Flow<List<StockMaster>> =
        stockDao.getAllStock() // gets a list of all stocks

    override suspend fun getStock(code: String): StockMaster? =
        stockDao.getStock(code) // gets a stock by stockCode

    override suspend fun getLastStockCode(): String? {
        return stockDao.getHighestStockCode()// gets the last stock code added
    }

    override fun getStockTransactions(code: String): Flow<List<StockTransaction>> =
        stockDao.getStockTransactions(code) // gets a list of stock transactions by stockCode


    override suspend fun getMostRecentDebtorForStock(code: String): String? {
        return stockDao.getMostRecentDebtorForStock(code)// gets the most recent debtor for stock transaction filter
    }

    override fun getTransactionInfoForStock(stockCode: String): Flow<List<TransactionInfo>> {
        return stockDao.getTransactionInfoForStock(stockCode)// gets a list of transaction info by stockCode
    }

    override suspend fun adjustStock(transaction: StockTransaction) {
        stockDao.performAdjustment(transaction)// adjusts a stock transaction
    }

    override suspend fun processInvoice(header: InvoiceHeader, items: List<InvoiceDetail>) { // processes an invoice
        db.withTransaction { // wrap entire method in transaction to maintain atomicity
            invoiceHeaderDao.insertInvoiceHeaders(listOf(header))
            invoiceDetailDao.insertInvoiceDetails(items)

            items.forEach { item -> // Process each item in the invoice

                val stock = stockDao.getStock(item.stockCode) // Get Stock

                if (stock != null) {
                    stockDao.recordStockSale(
                        code = item.stockCode,
                        qtySold = item.qtySold, // update stock sold
                        saleAmount = item.total //update total sales
                    )

                    val stockTrans = StockTransaction( // Add Stock Transaction
                        stockCode = item.stockCode,
                        date = header.date,
                        transactionType = "Invoice",
                        documentNum = header.invoiceNum,
                        qty = -item.qtySold, // Negative for sale
                        unitCost = stock.cost,
                        unitSell = stock.sellingPrice
                    )
                    stockDao.insertTransaction(stockTrans)
                }
            }

            val debtor = debtorDao.getDebtor(header.accountCode) // Get Debtor from invoice header

            if (debtor != null) {
                val invoiceTotal = header.totalSellAmtExVat + header.vat // get final total from invoice header
                val invoiceCost = header.totalCost // get total cost from invoice header

                val currentInvoiceYear =
                    Calendar.getInstance().apply { time = header.date }.get(Calendar.YEAR) // get current year

                var newSalesYTD = debtor.salesYearToDate
                var newCostYTD = debtor.costYearToDate
                var newSalesLastYear = debtor.salesLastYear
                var newCostLastYear = debtor.costLastYear
                var newFinancialYear = debtor.financialYear

                if (currentInvoiceYear > debtor.financialYear) { // update stats for new financial year
                    newSalesLastYear = debtor.salesYearToDate
                    newCostLastYear = debtor.costYearToDate

                    newSalesYTD = invoiceTotal // update total sales for the new financial year
                    newCostYTD = invoiceCost // update total cost for the new financial year
                    newFinancialYear = currentInvoiceYear // update financial year
                } else if (currentInvoiceYear == debtor.financialYear) { // update stats for the current financial year
                    newSalesYTD += invoiceTotal // update total sales for the current financial year
                    newCostYTD += invoiceCost //update the total cost for the current financial year
                }


                debtorDao.updateDebtor(// Update Debtors Master with new stats
                    debtor.copy(
                        balance = debtor.balance + invoiceTotal,
                        salesYearToDate = newSalesYTD,
                        costYearToDate = newCostYTD,
                        salesLastYear = newSalesLastYear,
                        costLastYear = newCostLastYear,
                        financialYear = newFinancialYear
                    )
                )

                val debtorTrans = DebtorTransaction(// Add Debtor Transaction
                    accountCode = header.accountCode,
                    date = header.date,
                    transactionType = "Invoice",
                    documentNo = header.invoiceNum,
                    grossTransactionValue = invoiceTotal,
                    vatValue = header.vat
                )
                debtorDao.insertTransaction(debtorTrans)
            }
        }
    }
}