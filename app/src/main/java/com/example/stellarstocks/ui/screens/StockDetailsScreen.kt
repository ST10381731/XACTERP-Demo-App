package com.example.stellarstocks.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.stellarstocks.data.db.models.TransactionInfo
import com.example.stellarstocks.ui.theme.DarkGreen
import com.example.stellarstocks.ui.theme.LightGreen
import com.example.stellarstocks.viewmodel.StockSortOption
import com.example.stellarstocks.viewmodel.StockViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun StockDetailsScreen(
    stockCode: String,
    viewModel: StockViewModel,
    navController: NavHostController
) {
    val stock by viewModel.selectedStock.collectAsState() // variable to manage stockViewModel state
    val transactions by viewModel.visibleTransactions.collectAsState() // variable to manage transaction table
    val currentSort by viewModel.currentSort.collectAsState() // variable to manage sort option

    var expanded by remember { mutableStateOf(false) } // variable to manage dropdown menu

    LaunchedEffect(stockCode) {
        viewModel.selectStockForDetails(stockCode) // update selected stock
    }

    if (stock == null) { // if stock is null, show loading screen
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
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
        IconButton(onClick = { navController.popBackStack() }) { // back button to return to previous screen
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkGreen)
        }

        Text("Stock Details", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
    }

        Card( // stock details card
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) { // table to display all stock details
                StockDetailRow("Stock Code:", currentStock.stockCode)
                StockDetailRow("Description:", currentStock.stockDescription)
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                StockDetailRow("Qty On Hand:", currentStock.stockOnHand.toString())
                StockDetailRow("Cost Price:", "R ${currentStock.cost}")
                StockDetailRow("Selling Price:", "R ${currentStock.sellingPrice}")
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                StockDetailRow("Total Sales (Ex VAT):", "R ${currentStock.totalSalesExclVat}")
                StockDetailRow("Total Purchases (Ex VAT):", "R ${currentStock.totalPurchasesExclVat}")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row( // row to display transaction history
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
                        text = { Text("Full List") },
                        onClick = {
                            viewModel.updateSort(StockSortOption.FULL_LIST)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Recent Debtor") },
                        onClick = {
                            viewModel.updateSort(StockSortOption.RECENT_DEBTOR_ONLY)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Highest Quantity") },
                        onClick = {
                            viewModel.updateSort(StockSortOption.HIGHEST_QUANTITY)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Lowest Quantity") },
                        onClick = {
                            viewModel.updateSort(StockSortOption.LOWEST_QUANTITY)
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
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) { // header for transaction table
            Text("Date", Modifier.weight(0.8f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Text("Acc", Modifier.weight(0.6f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Text("Doc", Modifier.weight(0.5f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Text("Type", Modifier.weight(0.7f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Text("Qty", Modifier.weight(0.4f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End, fontSize = 11.sp)
            Text("Value", Modifier.weight(0.7f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End, fontSize = 11.sp)
        }

        LazyColumn { // transaction table
            items(transactions) { trans ->
                StockTransactionRow(trans)
            }
        }
    }
}

fun getStockSortLabel(option: StockSortOption): String { // function to get sort option label
    return when(option) {
        StockSortOption.FULL_LIST -> "Full List"
        StockSortOption.RECENT_DEBTOR_ONLY -> "Recent Debtor"
        StockSortOption.HIGHEST_QUANTITY -> "Highest Quantity"
        StockSortOption.LOWEST_QUANTITY -> "Lowest Quantity"
    }
}

@Composable
fun StockDetailRow(label: String, value: String) { // function to display stock details
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
fun StockTransactionRow(trans: TransactionInfo) { // function to display transaction details
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Row(
        Modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = Color.LightGray)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text( // display transaction date
            text = dateFormat.format(trans.date),
            modifier = Modifier.weight(0.8f),
            fontSize = 11.sp
        )

        Text( // display account code
            text = trans.accountCode ?: "-",
            modifier = Modifier.weight(0.6f),
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text( // display document number
            text = trans.documentNum.toString(),
            modifier = Modifier.weight(0.5f),
            fontSize = 11.sp,
            maxLines = 1
        )

        Text( // display transaction type
            text = trans.transactionType,
            modifier = Modifier.weight(0.7f),
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text( // display transaction quantity
            text = trans.qty.toString(),
            modifier = Modifier.weight(0.4f),
            fontSize = 11.sp,
            textAlign = TextAlign.End,
            maxLines = 1
        )
        Text( // display transaction value
            text = String.format("R%.2f", trans.value),
            modifier = Modifier.weight(0.7f),
            fontSize = 11.sp,
            textAlign = TextAlign.End,
            maxLines = 1
        )
    }
}
