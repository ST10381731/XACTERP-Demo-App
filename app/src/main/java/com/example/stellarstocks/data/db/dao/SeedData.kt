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
        DebtorMaster("ACC001", "Tech Solutions KZN", "12 West Street", "Durban", 15000.0, 45000.0, 30000.0),
        DebtorMaster("ACC002", "Jozi Consulting", "88 Maude Ave", "Sandton", 4000.0, 12000.0, 8000.0),
        DebtorMaster("ACC003", "Cape Logistics", "45 Port Road", "Cape Town", 10000.0, 25000.0, 15000.0),
        DebtorMaster("ACC004", "Chatsworth Stationery", "101 Croftdene Dr", "Chatsworth", 1500.0, 5000.0, 3500.0),
        DebtorMaster("ACC005", "Midlands IT", "5 Main Road", "Howick", 0.0, 0.0, 0.0)
    )

    val stock = listOf(
        StockMaster("STK001", "Ergonomic Office Chair", 1200.0, 1800.0, 12000.0, 18000.0, 10, 10, 0),
        StockMaster("STK002", "Wireless Mechanical Keyboard", 800.0, 1200.0, 16000.0, 12000.0, 20, 10, 10),
        StockMaster("STK003", "24-inch IPS Monitor", 2500.0, 3500.0, 25000.0, 0.0, 10, 0, 10),
        StockMaster("STK004", "USB-C Docking Station", 1500.0, 2200.0, 15000.0, 4400.0, 10, 2, 8),
        StockMaster("STK005", "Laptop Stand Aluminium", 300.0, 550.0, 6000.0, 2750.0, 20, 5, 15)
    )

    val invoiceHeaders = listOf(
        InvoiceHeader(1001, "ACC001", daysAgo(10), 1800.0, 270.0, 1200.0),
        InvoiceHeader(1002, "ACC001", daysAgo(2), 2400.0, 360.0, 1600.0),
        InvoiceHeader(1003, "ACC004", daysAgo(1), 550.0, 82.5, 300.0),
        InvoiceHeader(1004, "ACC002", daysAgo(5), 1200.0, 180.0, 800.0),
        InvoiceHeader(1005, "ACC003", daysAgo(8), 3500.0, 525.0, 2500.0)
    )

    val invoiceDetails = listOf(
        InvoiceDetail(1001, 1, "STK001", 1, 1200.0, 1800.0, 0.0, 1800.0),
        InvoiceDetail(1002, 1, "STK002", 2, 800.0, 1200.0, 0.0, 2400.0),
        InvoiceDetail(1003, 1, "STK005", 1, 300.0, 550.0, 0.0, 550.0),
        InvoiceDetail(1004, 1, "STK002", 1, 800.0, 1200.0, 0.0, 1200.0),
        InvoiceDetail(1005, 1, "STK003", 1, 2500.0, 3500.0, 0.0, 3500.0)
    )

    val debtorTransactions = listOf(
        DebtorTransaction(1, "ACC001", daysAgo(10), "Invoice", 1001, 2070.0, 270.0),
        DebtorTransaction(2, "ACC001", daysAgo(5), "Payment", 9999, -1000.0, 0.0),
        DebtorTransaction(3, "ACC002", daysAgo(20), "Invoice", 1003, 750.0, 0.0),
        DebtorTransaction(4, "ACC002", daysAgo(5), "Invoice", 1004, 1380.0, 180.0),
        DebtorTransaction(5, "ACC003", daysAgo(8), "Invoice", 1005, 4025.0, 525.0)
    )

    val stockTransactions = listOf(
        StockTransaction(1, "STK001", daysAgo(30), "Purchase", 9001, 10, 1200.0, 0.0),
        StockTransaction(2, "STK001", daysAgo(10), "Sale", 1001, -1, 0.0, 1800.0),
        StockTransaction(3, "STK002", daysAgo(15), "Purchase", 9002, 20, 800.0, 0.0),
        StockTransaction(4, "STK002", daysAgo(2), "Sale", 1002, -2, 0.0, 1200.0),
        StockTransaction(5, "STK003", daysAgo(8), "Sale", 1005, -1, 0.0, 3500.0)
    )
}