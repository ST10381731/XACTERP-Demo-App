/*package com.example.stellarstocks.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stellarstocks.data.db.models.StockTransaction
import com.example.stellarstocks.ui.theme.DarkGreen
import com.example.stellarstocks.ui.theme.LightGreen
import com.example.stellarstocks.viewmodel.StockViewModel
import java.text.SimpleDateFormat
import java.util.Locale
@Composable
fun StockDetailsScreen(
    stockCode: String,
    viewModel: StockViewModel
) {
    val stock by viewModel.selectedStock.collectAsState()
    val transactions by viewModel.selectedTransactions.collectAsState()

    // Trigger load on entry
    LaunchedEffect(stockCode) {
        viewModel.selectStockForDetails(stockCode)
    }

    if (stock == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- TITLE ---
        Text(
            text = "Stock Details",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGreen,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // --- MASTER DATA CARD ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                DetailRow("Stock Code:", stock!!.stockCode)
                DetailRow("Description:", stock!!.stockDescription)
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                DetailRow("Qty On Hand:", stock!!.stockOnHand.toString())
                DetailRow("Cost Price:", "R ${stock!!.cost}")
                DetailRow("Selling Price:", "R ${stock!!.sellingPrice}")
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                DetailRow("Total Sales (Ex VAT):", "R ${stock!!.totalSalesExclVAT}")
                DetailRow("Total Purchases (Ex VAT):", "R ${stock!!.totalPurchasesExclVAT}")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- TRANSACTION TABLE HEADER ---
        Text(
            text = "Transaction History",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGreen,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // --- TABLE HEADERS ---
        Row(
            Modifier
                .fillMaxWidth()
                .background(LightGreen)
                .padding(8.dp)
        ) {
            Text("Date", Modifier.weight(0.8f), fontWeight = FontWeight.Bold)
            Text("Doc", Modifier.weight(0.7f), fontWeight = FontWeight.Bold)
            Text("Type", Modifier.weight(0.8f), fontWeight = FontWeight.Bold)
            Text("Qty", Modifier.weight(0.4f), fontWeight = FontWeight.Bold)
            Text("Price", Modifier.weight(0.6f), fontWeight = FontWeight.Bold)
        }

        // --- TABLE CONTENT ---
        LazyColumn {
            items(transactions) { trans ->
                StockTransactionRow(trans)
            }
        }
    }
}

@Composable
fun StockTransactionRow(trans: StockTransaction) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    // Show Cost for Purchases, Selling Price for Sales
    val priceDisplay = if (trans.transactionType == "Purchase") trans.unitCost else trans.unitSell

    Row(
        Modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = Color.LightGray)
            .padding(8.dp)
    ) {
        Text(dateFormat.format(trans.date), Modifier.weight(0.8f), fontSize = 12.sp)
        Text(trans.stockTransDocumentNum, Modifier.weight(0.7f), fontSize = 12.sp)
        Text(trans.transactionType, Modifier.weight(0.8f), fontSize = 12.sp)
        Text(trans.qty.toString(), Modifier.weight(0.4f), fontSize = 12.sp)
        Text("R $priceDisplay", Modifier.weight(0.6f), fontSize = 12.sp)
    }
}*/