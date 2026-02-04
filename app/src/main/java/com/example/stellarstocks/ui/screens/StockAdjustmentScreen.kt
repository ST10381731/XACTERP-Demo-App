package com.example.stellarstocks.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.stellarstocks.data.db.models.StockMaster
import com.example.stellarstocks.ui.theme.DarkGreen
import com.example.stellarstocks.ui.theme.LightGreen
import com.example.stellarstocks.viewmodel.StockViewModel
import kotlin.math.roundToInt

@Composable
fun StockAdjustmentScreen(viewModel: StockViewModel = viewModel(), navController: NavController) {
    val searchCode by viewModel.adjustmentSearchCode.collectAsState() //search code variable
    val foundStock by viewModel.foundAdjustmentStock.collectAsState() //found stock variable
    val adjustmentQty by viewModel.adjustmentQty.collectAsState() //adjustment quantity variable
    val toastMessage by viewModel.toastMessage.collectAsState()
    val context = LocalContext.current
    var showSearchDialog by remember { mutableStateOf(false) } //search dialog variable

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    if (showSearchDialog) { //determine if search dialog should be shown
        StockSearchDialog(
            viewModel = viewModel,
            onDismiss = { showSearchDialog = false },
            onStockSelected = {
                viewModel.onStockSelectedForAdjustment(it)
                showSearchDialog = false
            }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navController.popBackStack() }) { //back button with navigation to previous screen
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = DarkGreen
            )
        }
        Text( //screen header
            text = "Stock Adjustment",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGreen,
        )
    }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(// search field with search button
                value = searchCode,
                onValueChange = { viewModel.onAdjustmentSearchChange(it) },
                label = { Text("Select a Stock Code via the Search Button") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                readOnly = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button( //search button with icon
                onClick = { showSearchDialog = true }, //show search dialog on button click
                colors = ButtonDefaults.buttonColors(containerColor = LightGreen),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.height(56.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (foundStock != null) { //display stock details if stock is found
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) { //stock details card
                    Text("Stock Details", fontWeight = FontWeight.Bold, color = DarkGreen, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    StockDetailRow("Code:", foundStock!!.stockCode)
                    StockDetailRow("Description:", foundStock!!.stockDescription)
                    StockDetailRow("Current On Hand:", foundStock!!.stockOnHand.toString())
                    StockDetailRow("Cost:", "R ${foundStock!!.cost}")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Adjust Quantity", fontWeight = FontWeight.SemiBold)

            OutlinedTextField(
                value = adjustmentQty.toString(),
                onValueChange = {
                    val num = it.toIntOrNull()
                    if (num != null) {// Only update if it's a valid number
                        viewModel.onAdjustmentQtyChange(num)
                    } else if (it.isEmpty() || it == "-") { // Allow clearing and negative values
                        viewModel.onAdjustmentQtyChange(0)
                    }
                },
                label = { Text("Adjustment Amount (+/-)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(200.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.confirmAdjustment() },
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirm Adjustment")
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Search for a stock item to begin adjustment", color = Color.Gray)
            }
        }
    }
}

@Composable
fun StockSearchDialog( //search dialog for selecting stock
    viewModel: StockViewModel,
    onDismiss: () -> Unit,
    onStockSelected: (StockMaster) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState() //search query variable
    val filteredStock by viewModel.filteredStock.collectAsState() //filtered stock variable

    Dialog(onDismissRequest = onDismiss) { //dialog box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Stock",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    label = { Text("Search by code or description") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(filteredStock) { stock ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onStockSelected(stock) }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(stock.stockCode, modifier = Modifier.weight(1f))
                            Text(stock.stockDescription, modifier = Modifier.weight(2f))
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}