package com.example.stellarstocks.data.db.repository

import androidx.room.withTransaction
import com.example.stellarstocks.data.db.StellarStocksDatabase
import com.example.stellarstocks.data.db.models.*

class StellarStocksRepositoryImpl(
    private val db: StellarStocksDatabase
) : StellarStocksRepository {

    private val debtorDao = db.debtorDao()
    private val stockDao = db.stockDao()
    private val invoiceDao = db.invoiceDao()

    // Debtor Logic
    override suspend fun insertDebtor(debtor: DebtorMaster) = debtorDao.insertDebtor(debtor)
    override fun getAllDebtors() = debtorDao.getAllDebtors()
    override suspend fun getDebtor(code: String) = debtorDao.getDebtor(code)
    override fun getDebtorTransactions(code: String) = debtorDao.getTransactions(code)

    // Stock Logic
    override suspend fun insertStock(stock: StockMaster) = stockDao.insertStock(stock)
    override fun getAllStock() = stockDao.getAllStock()
    override suspend fun getStock(code: String) = stockDao.getStock(code)
    override fun getStockTransactions(code: String) = stockDao.getTransactions(code)

    // Invoice Logic
    override suspend fun processInvoice(header: InvoiceHeader, items: List<InvoiceDetail>) {
        db.withTransaction {
            // Invoice Header
            val newInvoiceId = invoiceDao.insertHeader(header)

            // Invoice Items
            val itemsWithId = items.map { it.copy(invoiceNum = newInvoiceId.toInt()) }
            invoiceDao.insertItems(itemsWithId)

            // Update Debtor Balance
            debtorDao.updateBalance(header.accountCode, header.totalSellAmtExVat)

            // Record Debtor Transaction
            debtorDao.insertTransaction(
                DebtorTransaction(
                    accountCode = header.accountCode,
                    date = header.date,
                    transactionType = "INV",
                    documentNo = newInvoiceId.toString(),
                    grossTransactionValue = header.totalSellAmtExVat + header.vat,
                    vatValue = header.vat
                )
            )

            // Update Stock Levels and Record Transactions
            itemsWithId.forEach { item ->
                stockDao.updateStockLevel(item.stockCode, item.qtySold)

                stockDao.insertTransaction(
                    StockTransaction(
                        stockCode = item.stockCode,
                        date = header.date,
                        transactionType = "INV",
                        documentNum = newInvoiceId.toString(),
                        qty = -item.qtySold,
                        unitCost = item.unitCost,
                        unitSell = item.unitSell
                    )
                )
            }
        }
    }
}