package com.example.stellarstocks.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stellarstocks.data.db.models.StockTransaction
import com.example.stellarstocks.ui.theme.DarkGreen
import com.example.stellarstocks.ui.theme.LightGreen
import com.example.stellarstocks.viewmodel.StockSortOption
import com.example.stellarstocks.viewmodel.StockViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

@Composable
fun StockDetailsScreen(
    stockCode: String,
    viewModel: StockViewModel
) {
    val stock by viewModel.selectedStock.collectAsState()
    val transactions by viewModel.visibleTransactions.collectAsState()
    val currentSort by viewModel.currentSort.collectAsState()

    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(stockCode) {
        viewModel.selectStockForDetails(stockCode)
    }

    if (stock == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentStock = stock!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Stock Details",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGreen,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                StockDetailRow("Stock Code:", currentStock.stockCode)
                StockDetailRow("Description:", currentStock.stockDescription)
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                StockDetailRow("Qty On Hand:", currentStock.stockOnHand.toString())
                StockDetailRow("Cost Price:", "R ${currentStock.cost}")
                StockDetailRow("Selling Price:", "R ${currentStock.sellingPrice}") // Correct field name
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                StockDetailRow("Total Sales (Ex VAT):", "R ${currentStock.totalSalesExclVat}")
                StockDetailRow("Total Purchases (Ex VAT):", "R ${currentStock.totalPurchasesExclVat}")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Transaction History",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen
            )

            // Sort Dropdown
            Box {
                Button(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.buttonColors(containerColor = LightGreen)
                ) {
                    Text("Sort By: ${getStockSortLabel(currentSort)}", color = Color.Black)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Sort", tint = Color.Black)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Most Recent") },
                        onClick = {
                            viewModel.updateSort(StockSortOption.MOST_RECENT)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Highest Qty") },
                        onClick = {
                            viewModel.updateSort(StockSortOption.HIGHEST_VALUE)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Lowest Qty") },
                        onClick = {
                            viewModel.updateSort(StockSortOption.LOWEST_VALUE)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .background(LightGreen)
                .padding(8.dp)
        ) {
            Text("Date", Modifier.weight(0.8f), fontWeight = FontWeight.Bold)
            Text("Doc", Modifier.weight(0.6f), fontWeight = FontWeight.Bold)
            Text("Type", Modifier.weight(0.8f), fontWeight = FontWeight.Bold)
            Text("Qty", Modifier.weight(0.4f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
            Text("Value", Modifier.weight(0.7f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End) // Changed to Value
        }

        LazyColumn {
            items(transactions) { trans ->
                StockTransactionRow(trans)
            }
        }
    }
}

// for labels
fun getStockSortLabel(option: StockSortOption): String {
    return when(option) {
        StockSortOption.MOST_RECENT -> "Date"
        StockSortOption.HIGHEST_VALUE -> "High Qty"
        StockSortOption.LOWEST_VALUE -> "Low Qty"
    }
}


@Composable
fun StockDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StockTransactionRow(trans: StockTransaction) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val unitPrice = if (trans.transactionType == "Purchase") trans.unitCost else trans.unitSell


    val totalValue = unitPrice * abs(trans.qty)// Calculate Total Value for display

    Row(
        Modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = Color.LightGray)
            .padding(8.dp)
    ) {
        Text(dateFormat.format(trans.date), Modifier.weight(0.8f), fontSize = 12.sp)
        Text(trans.documentNum.toString(), Modifier.weight(0.6f), fontSize = 12.sp)
        Text(trans.transactionType, Modifier.weight(0.8f), fontSize = 12.sp)
        Text(trans.qty.toString(), Modifier.weight(0.4f), fontSize = 12.sp, textAlign = TextAlign.End)
        Text("R $totalValue", Modifier.weight(0.7f), fontSize = 12.sp, textAlign = TextAlign.End)
    }
}