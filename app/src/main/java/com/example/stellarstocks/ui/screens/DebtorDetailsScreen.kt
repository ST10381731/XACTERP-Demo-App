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
import com.example.stellarstocks.data.db.models.DebtorTransactionInfo
import com.example.stellarstocks.ui.theme.DarkGreen
import com.example.stellarstocks.ui.theme.LightGreen
import com.example.stellarstocks.viewmodel.DebtorViewModel
import com.example.stellarstocks.viewmodel.SortOption
import java.text.SimpleDateFormat
import java.util.Locale

/*
* Debtor Details Screen
* displays all information relating to debtor from debtor master table such as address, name, etc.
* users can view debtor transaction history
* users have the ability to click a debtor transaction entry to view the full details of the transaction
* users can filter the transaction table to reveal most recent items sold, highest value and lowest value
*/
@Composable
fun DebtorDetailsScreen(
    accountCode: String,
    viewModel: DebtorViewModel,
    navController: NavHostController
) {
    val debtor by viewModel.selectedDebtor.collectAsState()
    val transactions by viewModel.visibleTransactions.collectAsState() // List of transactions to display
    val currentSort by viewModel.currentSort.collectAsState() // Current sort option

    var expanded by remember { mutableStateOf(false) } // variable to control dropdown menu

    LaunchedEffect(accountCode) {
        viewModel.selectDebtorForDetails(accountCode)
    // Fetch debtor details upon user clicking on a debtor in enquiry
    }

    if (debtor == null) { // if debtor is not found, show loading screen
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
        Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { navController.popBackStack() }) {
            // navigate back to enquiry screen
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkGreen)
        }
        Text( // Debtor Details Header
            text = "Debtor Details",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGreen,
        )
    }
        Spacer(modifier = Modifier.height(16.dp))

        Card( // Debtor Details Card
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                DebtorDetailRow("Account Code:", currentDebtor.accountCode)
                DebtorDetailRow("Name:", currentDebtor.name)
                HorizontalDivider(Modifier.padding(vertical = 8.dp))

                DebtorDetailRow("Primary Address: ", currentDebtor.address1)
                HorizontalDivider(Modifier.padding(vertical = 8.dp))

                DebtorDetailRow("Secondary Address: ", currentDebtor.address2)
                HorizontalDivider(Modifier.padding(vertical = 8.dp))

                DebtorDetailRow("Sales YTD:", String.format("R%.2f", currentDebtor.salesYearToDate))
                DebtorDetailRow("Cost YTD:", String.format("R%.2f", currentDebtor.costYearToDate))
                DebtorDetailRow("Balance:", String.format("R%.2f", currentDebtor.balance))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Debtor Transaction History",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen
            )

        }
        Box {
            Button( // sort button
                onClick = { expanded = true },
                colors = ButtonDefaults.buttonColors(containerColor = LightGreen)
            ) {
                Text("Sort By: ${getSortLabel(currentSort)}", color = Color.Black)
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Sort", tint = Color.Black)
            }

            DropdownMenu( // Sort Dropdown Menu that appears after clicking sort button
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
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
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .background(LightGreen)
                .padding(8.dp)
        ) { // Transaction History Header
            Text("Date", Modifier.weight(0.7f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Doc", Modifier.weight(0.5f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Type", Modifier.weight(0.7f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Items", Modifier.weight(1.0f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Value", Modifier.weight(0.6f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End)
        }

        LazyColumn {// Transaction History List
            items(transactions) { trans -> // loop through each transaction in table and display
                DebtorTransactionRow(trans)
            }
        }
    }
}
fun getSortLabel(option: SortOption): String { // function to get sort label based on option
    return when(option) {
        SortOption.RECENT_ITEM_SOLD -> "Recent Item Sold"
        SortOption.HIGHEST_VALUE -> "Highest Value"
        SortOption.LOWEST_VALUE -> "Lowest Value"
    }
}

@Composable
fun DebtorDetailRow(label: String, value: String) { // function to display a row of debtor details
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(label, fontWeight = FontWeight.SemiBold, color = Color.Gray,  modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}

@Composable
fun DebtorTransactionRow(trans: DebtorTransactionInfo) { // function to display a row of debtor transactions
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    var expanded by remember { mutableStateOf(false) } // variable to control dropdown menu

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

        // Doc Number
        Text(trans.documentNum.toString(), Modifier.weight(0.5f), fontSize = 11.sp)

        // Transaction Type
        Text(trans.transactionType, Modifier.weight(0.7f), fontSize = 11.sp)

        // Items
        Text(
            text = trans.items ?: "-",
            modifier = Modifier.weight(1.0f),
            fontSize = 11.sp,
            maxLines = if (expanded) Int.MAX_VALUE else 1, // Expandable text
            overflow = TextOverflow.Ellipsis  // ellipsis if text is too long
        )

        // Value
        Text(String.format("R%.2f", trans.value), Modifier.weight(0.6f), fontSize = 11.sp,
            textAlign = TextAlign.End)
    }
}