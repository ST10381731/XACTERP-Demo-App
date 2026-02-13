package com.example.stellarstocks.data.db.repository

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
        debtorDao.insertDebtor(debtor)
    // insert a debtor

    override suspend fun updateDebtor(debtor: DebtorMaster) =
        debtorDao.updateDebtor(debtor)
    // update a debtor

    override suspend fun deleteDebtor(code: String) =
        debtorDao.deleteDebtor(code)
    // delete a debtor

    override fun getAllDebtors(): Flow<List<DebtorMaster>> =
        debtorDao.getAllDebtors()
    // get a list of all debtors for enquiry search, sort and graphing

    override suspend fun getDebtor(code: String): DebtorMaster? =
        debtorDao.getDebtor(code)
    // get a debtor by code

    override suspend fun getLastDebtorCode(): String? {
        return debtorDao.getHighestAccountCode()
    // gets the last debtor code added for auto creation of new account code
    }

    // Debtor Transactions
    override fun getDebtorTransactionInfo(accountCode: String): Flow<List<DebtorTransactionInfo>> {
        return debtorDao.getDebtorTransactionInfo(accountCode)
    // gets a list of debtor transaction info according to accountCode for transaction table
    }

    override fun getAllDebtorTransactions(): Flow<List<DebtorTransaction>> {
        return debtorDao.getAllTransactions()
    //get all debtor transactions for graph on landing page
    }

    // Stock
    override suspend fun insertStock(stock: StockMaster) =
        stockDao.insertStock(stock)
    // insert a stock according to stockCode

    override suspend fun updateStock(stock: StockMaster) =
        stockDao.updateStock(stock)
    // update a stock according to stockCode

    override suspend fun deleteStock(code: String) =
        stockDao.deleteStock(code)
    // delete a stock according to stockCode

    override fun getAllStock(): Flow<List<StockMaster>> =
        stockDao.getAllStock()
    // get a list of all stocks for enquiry search, sort and graphing

    override suspend fun getStock(code: String): StockMaster? =
        stockDao.getStock(code)
    // get a stock by stockCode

    override suspend fun getLastStockCode(): String? {
        return stockDao.getHighestStockCode()
    // get the last stock code added
    }

    override fun getTransactionInfoForStock(stockCode: String): Flow<List<TransactionInfo>> {
        return stockDao.getTransactionInfoForStock(stockCode)
    // gets a list of transaction info by stockCode for stock details transaction table
    }

    override suspend fun adjustStock(transaction: StockTransaction) {
        stockDao.performAdjustment(transaction)
    // adjusts a stock transaction
    }

    override suspend fun getNextAdjustmentNum(): Int {
        val max = stockDao.getMaxAdjustmentDocNum() ?: 0
        return max + 1
        // get the next document number for adjustment
    }

    override suspend fun getNextInvoiceNum(): Int {
        val last = invoiceHeaderDao.getLastInvoiceNum() ?: 0
        return last + 1
        // get the next invoice number
    }

    override suspend fun processInvoice(header: InvoiceHeader, items: List<InvoiceDetail>) {
        db.withTransaction {

            /* insert invoice header */
            val headerToInsert = header.copy(invoiceNum = 0)
            // Create a copy of the header with a zero invoice number
            val invoiceId = invoiceHeaderDao.insertInvoiceHeader(headerToInsert).toInt()
            // Insert the header and get the generated ID

            /* insert invoice details */
            val invoiceItems = items.map { it.copy(invoiceNum = invoiceId) }
            // Create a list of invoice items with the generated ID
            invoiceDetailDao.insertInvoiceDetails(invoiceItems)
            // Insert the invoice items into invoice items table

            /* update debtor master */
            val debtor = debtorDao.getDebtor(header.accountCode)
            // Get Debtor from invoice header
            if (debtor != null) {
                val invoiceTotal = header.totalSellAmtExVat + header.vat // header total calculation
                val invoiceCost = header.totalCost
                val currentInvoiceYear = Calendar.getInstance().apply { time = header.date }
                    .get(Calendar.YEAR) // get current year from invoice date

                var newSalesYTD = debtor.salesYearToDate
                var newCostYTD = debtor.costYearToDate
                var newSalesLastYear = debtor.salesLastYear
                var newCostLastYear = debtor.costLastYear
                var newFinancialYear = debtor.financialYear

                if (currentInvoiceYear > debtor.financialYear) { //if new financial year
                    newSalesLastYear = debtor.salesYearToDate
                    newCostLastYear = debtor.costYearToDate
                    newSalesYTD = invoiceTotal
                    newCostYTD = invoiceCost
                    newFinancialYear = currentInvoiceYear
                } else if (currentInvoiceYear == debtor.financialYear) { //if current financial year
                    newSalesYTD += invoiceTotal
                    newCostYTD += invoiceCost
                }

                debtorDao.updateDebtor( // update Debtors Master with new stats
                    debtor.copy(
                        balance = debtor.balance + invoiceTotal,
                        salesYearToDate = newSalesYTD,
                        costYearToDate = newCostYTD,
                        salesLastYear = newSalesLastYear,
                        costLastYear = newCostLastYear,
                        financialYear = newFinancialYear
                    )
                )

                /* insert debtor transaction */
                debtorDao.insertTransaction(// add Debtor Transaction
                    DebtorTransaction(
                        accountCode = header.accountCode,
                        date = header.date,
                        transactionType = "Invoice",
                        documentNo = invoiceId,
                        grossTransactionValue = invoiceTotal,
                        vatValue = header.vat
                    )
                )
            }

            /* update stock master */
            items.forEach { item -> // process each item in the invoice
                val stock = stockDao.getStock(item.stockCode)
                if (stock != null) {
                    val effectiveUnitSell = if (item.qtySold != 0) {
                        item.total / item.qtySold
                    } else {
                        0.0
                    }
                    stockDao.recordStockSale( // update Stock master for that item code
                        code = item.stockCode,
                        qtySold = item.qtySold,
                        saleAmount = item.total
                    )

                    /* update stock transaction */
                    stockDao.insertTransaction( // insert a new stock transaction
                        StockTransaction(
                            stockCode = item.stockCode,
                            date = header.date,
                            transactionType = "Invoice",
                            documentNum = invoiceId,
                            qty = -item.qtySold, // negative qty for an invoice
                            unitCost = stock.cost,
                            unitSell = effectiveUnitSell
                        )
                    )
                }
            }
        }
    }
}