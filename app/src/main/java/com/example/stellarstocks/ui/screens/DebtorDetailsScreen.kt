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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stellarstocks.data.db.models.DebtorTransaction
import com.example.stellarstocks.ui.theme.DarkGreen
import com.example.stellarstocks.ui.theme.LightGreen
import com.example.stellarstocks.viewmodel.DebtorViewModel
import com.example.stellarstocks.viewmodel.SortOption
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DebtorDetailsScreen(
    accountCode: String,
    viewModel: DebtorViewModel
) {
    val debtor by viewModel.selectedDebtor.collectAsState()
    val transactions by viewModel.visibleTransactions.collectAsState()
    val currentSort by viewModel.currentSort.collectAsState()

    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(accountCode) {
        viewModel.selectDebtorForDetails(accountCode)
    }

    if (debtor == null) {
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
        Text(
            text = "Debtor Details",
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
                DetailRow("Account Code:", debtor!!.accountCode)
                DetailRow("Name:", debtor!!.name)
                DetailRow("Address:", "${debtor!!.address1}, ${debtor!!.address2}")
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                DetailRow("Balance:", "R ${debtor!!.balance}")
                DetailRow("Sales YTD:", "R ${debtor!!.salesYearToDate}")
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
                    Text("Sort By: ${getSortLabel(currentSort)}", color = Color.Black)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Sort", tint = Color.Black)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Most Recent Item Sold") },
                        onClick = {
                            viewModel.updateSort(SortOption.MOST_RECENT)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Highest Transaction Value") },
                        onClick = {
                            viewModel.updateSort(SortOption.HIGHEST_VALUE)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Lowest Transaction Value") },
                        onClick = {
                            viewModel.updateSort(SortOption.LOWEST_VALUE)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Table Header
        Row(
            Modifier
                .fillMaxWidth()
                .background(LightGreen)
                .padding(8.dp)
        ) {
            Text("Date", Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Text("Doc No", Modifier.weight(0.8f), fontWeight = FontWeight.Bold)
            Text("Type", Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Text("Value", Modifier.weight(1f), fontWeight = FontWeight.Bold)
        }

        // Table content
        LazyColumn {
            items(transactions) { trans ->
                TransactionRow(trans)
            }
        }
    }
}

fun getSortLabel(option: SortOption): String {
    return when(option) {
        SortOption.MOST_RECENT -> "Date"
        SortOption.HIGHEST_VALUE -> "Highest Value"
        SortOption.LOWEST_VALUE -> "Lowest Value"
    }
}

@Composable
fun DetailRow(label: String, value: String) {
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
fun TransactionRow(trans: DebtorTransaction) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    Row(
        Modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = Color.LightGray)
            .padding(8.dp)
    ) {
        Text(dateFormat.format(trans.date), Modifier.weight(1f), fontSize = 14.sp)
        Text(trans.documentNo.toString(), Modifier.weight(0.8f), fontSize = 14.sp)
        Text(trans.transactionType, Modifier.weight(1f), fontSize = 14.sp)
        Text("R ${trans.grossTransactionValue}", Modifier.weight(1f), fontSize = 14.sp)
    }
}