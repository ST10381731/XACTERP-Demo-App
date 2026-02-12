package com.example.stellarstocks.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
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
import com.example.stellarstocks.ui.theme.Black
import com.example.stellarstocks.ui.theme.DarkGreen
import com.example.stellarstocks.ui.theme.LightGreen
import com.example.stellarstocks.viewmodel.StockViewModel
import kotlin.math.roundToInt

/*
* Stock Adjustment Screen
* allows user to search for a stock via dialog to display its current details
* users can view stock on hand, cost and selling price
* users have the ability to change the transaction type for the adjustment to adjustment or purchase
* selecting adjustment will only change the stock on hand (can be negative or positive)
* selecting purchase will change stock on hand and update the stock masters total purchase excl vat (positive only)
* validation in place to prevent adjustments if it will put stock on hand in a negative value*/
@Composable
fun StockAdjustmentScreen(viewModel: StockViewModel = viewModel(), navController: NavController) {
    val searchCode by viewModel.adjustmentSearchCode.collectAsState()
    val foundStock by viewModel.foundAdjustmentStock.collectAsState()
    val adjustmentQty by viewModel.adjustmentQty.collectAsState()
    val adjustmentType by viewModel.adjustmentType.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val context = LocalContext.current
    var showSearchDialog by remember { mutableStateOf(false) } //search dialog variable

    LaunchedEffect(toastMessage) { // initialise toast messages on screen start
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    if (showSearchDialog) { //determine if search dialog should be shown
        StockSearchDialog(
            viewModel = viewModel,
            onDismiss = {
                showSearchDialog = false // close search dialog on dismiss
                viewModel.resetSearch()}, // reset search query
            onStockSelected = {
                viewModel.onStockSelectedForAdjustment(it) // set selected stock on stock selection
                showSearchDialog = false
                viewModel.resetSearch()
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
        IconButton(onClick = { navController.popBackStack() }) {
            //back button with navigation to previous screen
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
            OutlinedTextField(// search field with hint
                value = searchCode,
                onValueChange = { viewModel.onAdjustmentSearchChange(it) },
                label = { Text("Select a Stock Code via the Search Button") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                readOnly = true,
                enabled = false
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
                    StockDetailRow("Code:", foundStock!!.stockCode) //display stock code
                    StockDetailRow("Description:", foundStock!!.stockDescription) //display stock description
                    StockDetailRow("Current On Hand:", foundStock!!.stockOnHand.toString()) //display stock on hand
                    StockDetailRow("Cost:", "R ${foundStock!!.cost}") //display stock cost
                    StockDetailRow("Selling Price:", "R ${foundStock!!.sellingPrice}") //display stock selling price
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Transaction Type", fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton( //radio buttons to change transaction type
                    selected = adjustmentType == "Adjustment",
                    onClick = { viewModel.setAdjustmentType("Adjustment") },
                    colors = RadioButtonDefaults.colors(selectedColor = DarkGreen)
                )
                Text("Adjustment", modifier = Modifier.clickable { viewModel.setAdjustmentType("Adjustment") })


                Spacer(Modifier.width(16.dp))

                RadioButton(
                    selected = adjustmentType == "Purchase",
                    onClick = { viewModel.setAdjustmentType("Purchase") },
                    colors = RadioButtonDefaults.colors(selectedColor = DarkGreen)
                )
                Text("Purchase", modifier = Modifier.clickable { viewModel.setAdjustmentType("Purchase") }) //display selected transaction type
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Quantity", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(// quantity text field
                value = adjustmentQty.toString(),
                onValueChange = {
                    val num = it.toIntOrNull() // convert quantity input
                    if (num != null) { // validate quantity input
                        viewModel.onAdjustmentQtyChange(num) // update quantity
                    } else if (it.isEmpty() || it == "-") { // if quantity is empty or negative, set to 0
                        viewModel.onAdjustmentQtyChange(0) // update quantity
                    }
                },
                label = { Text(if(adjustmentType == "Purchase") "Quantity (+ only)" else "Quantity (+/-)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), //restrict user input to numbers only
                modifier = Modifier.width(200.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.confirmAdjustment() },
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirm Transaction")
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Search for a stock item to begin", color = Color.Gray)
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
                    onValueChange = { viewModel.onSearchQueryChange(it) }, //update search query
                    label = { Text("Search by code or description") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = Black
                                )
                            }
                        }
                    },
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(filteredStock) { stock ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onStockSelected(stock)} //on click, set selected stock
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