package com.example.stellarstocks.data.db.dao

import com.example.stellarstocks.data.db.models.DebtorMaster
import com.example.stellarstocks.data.db.models.DebtorTransaction
import com.example.stellarstocks.data.db.models.InvoiceDetail
import com.example.stellarstocks.data.db.models.InvoiceHeader
import com.example.stellarstocks.data.db.models.StockMaster
import com.example.stellarstocks.data.db.models.StockTransaction
import java.util.Date

object SeedData {
    private fun daysAgo(days: Int): Date = Date(System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L))

    val debtors = listOf(
        DebtorMaster("ACC001", "Tech Solutions KZN", "12, West Street, Phoenix, 4002", "", 4830.0, 4830.0, 1200.0, true),
        DebtorMaster("ACC002", "Jozi Jola Consulting", "88, Maude Ave, Sandton, 4060", "", 1380.0, 1380.0, 800.0, true),
        DebtorMaster("ACC003", "Cape Logistics", "45, Port Road, Bayview, 4001", "", 4025.0, 4025.0, 2500.0, true),
        DebtorMaster("ACC004", "Waltons Stationery", "101, Croftdene Dr, Chatsworth, 4092", "", 632.2, 632.2, 300.0, true),
        DebtorMaster("ACC005", "Page Automation IT", "5, Main Road, Nandi Dr, 4015", "", 0.0, 0.0, 0.0, true)
    )

    val stock = listOf(
        StockMaster("STK001", "Ergonomic Office Chair", 1200.0, 1800.0, 1200.0, 1800.0, 10, 1, 9, true),
        StockMaster("STK002", "Wireless Mechanical Keyboard", 800.0, 1200.0, 3600.0, 2400.0, 10, 3, 7, true),
        StockMaster("STK003", "24-inch IPS Monitor", 2500.0, 3500.0, 2500.0, 3500.0, 1, 1, 0, true),
        StockMaster("STK004", "USB-C Docking Station", 1500.0, 2200.0, 0.0, 0.0, 0, 0, 0, true),
        StockMaster("STK005", "Laptop Stand", 300.0, 550.0, 300.0, 550.0, 1, 1, 0, true)
    )

    val invoiceHeaders = listOf(
        InvoiceHeader(1, "ACC001", daysAgo(10), 1800.0, 270.0, 2070.0),
        InvoiceHeader(2, "ACC001", daysAgo(9), 2400.0, 360.0, 2760.0),
        InvoiceHeader(3, "ACC004", daysAgo(8), 550.0, 82.5, 632.2),
        InvoiceHeader(4, "ACC002", daysAgo(5), 1200.0, 180.0, 1380.0),
        InvoiceHeader(5, "ACC003", daysAgo(1), 3500.0, 525.0, 4025.0),
    )

    val invoiceDetails = listOf(
        InvoiceDetail(1, 1, "STK001", 1, 1200.0, 1800.0, 0.0, 1800.0),
        InvoiceDetail(2, 1, "STK002", 2, 800.0, 1200.0, 0.0, 2400.0),
        InvoiceDetail(3, 1, "STK005", 1, 300.0, 550.0, 0.0, 550.0),
        InvoiceDetail(4, 1, "STK002", 1, 800.0, 1200.0, 0.0, 1200.0),
        InvoiceDetail(5, 1, "STK003", 1, 2500.0, 3500.0, 0.0, 3500.0),
    )

    val debtorTransactions = listOf(
        DebtorTransaction(1, "ACC001", daysAgo(10), "Invoice", 1, 2070.0, 270.0),
        DebtorTransaction(2, "ACC001", daysAgo(9), "Invoice", 2, 2760.0, 360.0),
        DebtorTransaction(3, "ACC004", daysAgo(8), "Invoice", 3, 632.2, 82.5),
        DebtorTransaction(4, "ACC002", daysAgo(5), "Invoice", 4, 1380.0, 180.0),
        DebtorTransaction(5, "ACC003", daysAgo(1), "Invoice", 5, 4025.0, 525.0),
    )

    val stockTransactions = listOf(
        StockTransaction(1, "STK001", daysAgo(30), "Adjustment", 1001, 10, 1200.0, 0.0),
        StockTransaction(2, "STK002", daysAgo(15), "Adjustment", 1002, 10, 800.0, 0.0),
        StockTransaction(3, "STK003", daysAgo(12), "Adjustment", 1003, 1, 2500.0, 0.0),
        StockTransaction(4, "STK005", daysAgo(11), "Adjustment", 1004, 1, 1500.0, 0.0),
        StockTransaction(5, "STK001", daysAgo(10), "Invoice", 1, -1, 0.0, 1800.0),
        StockTransaction(7, "STK002", daysAgo(9), "Invoice", 2, -2, 0.0, 2400.0),
        StockTransaction(8, "STK005", daysAgo(8), "Invoice", 3, -1, 0.0, 550.0),
        StockTransaction(9, "STK002", daysAgo(5), "Invoice", 4, -1, 0.0, 1200.0),
        StockTransaction(10, "STK003", daysAgo(1), "Invoice", 5, -1, 0.0, 3500.0),
    )
}