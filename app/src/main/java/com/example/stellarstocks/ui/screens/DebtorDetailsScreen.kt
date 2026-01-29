package com.example.stellarstocks.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.stellarstocks.data.db.models.DebtorTransactionInfo
import com.example.stellarstocks.ui.theme.DarkGreen
import com.example.stellarstocks.ui.theme.LightGreen
import com.example.stellarstocks.viewmodel.DebtorViewModel
import com.example.stellarstocks.viewmodel.SortOption
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DebtorDetailsScreen(
    accountCode: String,
    viewModel: DebtorViewModel,
    navController: NavHostController
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

    val currentDebtor = debtor!!

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
                DebtorDetailRow("Account Code:", currentDebtor.accountCode)
                DebtorDetailRow("Name:", currentDebtor.name)
                DebtorDetailRow("Address:", "${currentDebtor.address1}, ${currentDebtor.address2}")
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                DebtorDetailRow("Balance:", "R ${currentDebtor.balance}")
                DebtorDetailRow("Sales YTD:", "R ${currentDebtor.salesYearToDate}")
                DebtorDetailRow("Cost YTD:", "R ${currentDebtor.costYearToDate}")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                        text = { Text("Full List") },
                        onClick = {
                            viewModel.updateSort(SortOption.FULL_LIST)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Recent Item Sold") },
                        onClick = {
                            viewModel.updateSort(SortOption.RECENT_ITEM_SOLD)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Highest Value") },
                        onClick = {
                            viewModel.updateSort(SortOption.HIGHEST_VALUE)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Lowest Value") },
                        onClick = {
                            viewModel.updateSort(SortOption.LOWEST_VALUE)
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
            Text("Date", Modifier.weight(0.7f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Doc", Modifier.weight(0.5f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Type", Modifier.weight(0.7f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Items", Modifier.weight(1.0f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Value", Modifier.weight(0.6f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End)
        }

        LazyColumn {
            items(transactions) { trans ->
                DebtorTransactionRow(trans)
            }
        }
    }
}

fun getSortLabel(option: SortOption): String {
    return when(option) {
        SortOption.FULL_LIST -> "Full List"
        SortOption.RECENT_ITEM_SOLD -> "Recent Item Sold"
        SortOption.HIGHEST_VALUE -> "Highest Value"
        SortOption.LOWEST_VALUE -> "Lowest Value"
    }
}

@Composable
fun DebtorDetailRow(label: String, value: String) {
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
fun DebtorTransactionRow(trans: DebtorTransactionInfo) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    var expanded by remember { mutableStateOf(false) }

    Row(
        Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .border(width = 0.5.dp, color = Color.LightGray)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date
        Text(dateFormat.format(trans.date), Modifier.weight(0.7f), fontSize = 11.sp)

        // Doc No
        Text(trans.documentNum.toString(), Modifier.weight(0.5f), fontSize = 11.sp)

        // Transaction Type
        Text(trans.transactionType, Modifier.weight(0.7f), fontSize = 11.sp)

        // Items
        Text(
            text = trans.items ?: "-",
            modifier = Modifier.weight(1.0f),
            fontSize = 11.sp,
            maxLines = if (expanded) Int.MAX_VALUE else 1,
            overflow = TextOverflow.Ellipsis
        )

        // Value
        Text(String.format("R%.2f", trans.value), Modifier.weight(0.6f), fontSize = 11.sp, textAlign = TextAlign.End)
    }
}