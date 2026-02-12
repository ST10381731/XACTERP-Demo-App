package com.example.stellarstocks.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.stellarstocks.data.db.models.StockMaster
import com.example.stellarstocks.ui.navigation.Screen
import com.example.stellarstocks.ui.theme.DarkGreen
import com.example.stellarstocks.ui.theme.LightGreen
import com.example.stellarstocks.ui.theme.Orange
import com.example.stellarstocks.ui.theme.Red
import com.example.stellarstocks.viewmodel.DebtorViewModel
import com.example.stellarstocks.viewmodel.StockViewModel

/*
* Stock Creation Screen
-Creation Mode-
* allows user to create new stock items by entering the required information into '*' fields
* stock code is auto created to prevent user errors
* validation to prevent double spacing or starting/ending input with whitespace for description
* validation to prevent letters and special characters in cost and selling price
* validation to prevent negative cost or selling price
* validation to prevent selling price being lower than cost price

* Users can seamlessly switch between creation and edit mode by clicking the highlighted text

-Edit mode-
* edit mode allows users to search for stock items via part stock code or description
* all inputs are disabled until a relevant stock item has been selected
* all inputs auto-populate with table data once a stock item is selected
* prior validations remain active during edit reducing user error
* edit mode allows users to delete a stock which will deactivate the stock in master table
*/
@Composable
fun StockCreationScreen(viewModel: StockViewModel = viewModel(), navController: NavController) {
    val isEditMode by viewModel.isEditMode.collectAsState() // Check if in edit mode
    var showConfirmationDialog by remember { mutableStateOf(false) } // delete confirmation dialog state

    val stockCode by viewModel.stockCode.collectAsState()
    val description by viewModel.description.collectAsState()
    val cost by viewModel.cost.collectAsState()
    val sellingPrice by viewModel.sellingPrice.collectAsState()

    var costTfv by remember { mutableStateOf(TextFieldValue(cost.toString())) }
    // Text field values for cost to manipulate the cost price input
    var sellingPriceTfv by remember { mutableStateOf(TextFieldValue(sellingPrice.toString())) }
    // Text field values for selling price to manipulate the sell price input

    val toastMessage by viewModel.toastMessage.collectAsState()
    val context = LocalContext.current

    val scrollState = rememberScrollState()
    var showSearchDialog by remember { mutableStateOf(false) }

    LaunchedEffect(toastMessage) { // initialise toast messages
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    LaunchedEffect(cost) { // to update the cost text field value
        val currentInputVal = costTfv.text.toDoubleOrNull() ?: 0.0
        if (currentInputVal != cost) {
            val newText = cost.toString()
            costTfv = TextFieldValue(text = newText, selection = TextRange(newText.length))
        }
    }

    LaunchedEffect(sellingPrice) { // to update the selling price text field value
        val currentInputVal = sellingPriceTfv.text.toDoubleOrNull() ?: 0.0
        if (currentInputVal != sellingPrice) {
            val newText = sellingPrice.toString()
            sellingPriceTfv = TextFieldValue(text = newText, selection = TextRange(newText.length))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationChannel.collect { shouldNavigate ->
            if (shouldNavigate) {
                navController.navigate(Screen.StockEnquiry.route) {
                    // Navigate to the stock enquiry screen after creation/edit
                    popUpTo(Screen.StockMenu.route) { inclusive = false }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!isEditMode) viewModel.generateNewCode() // Generate a new stockCode if not in edit mode
    }

    if (showSearchDialog) { // Show the search dialog if it's true
        StockCreationSearchDialog(
            viewModel = viewModel,
            onDismiss = {
                showSearchDialog = false // Dismiss the dialog when clicked outside
                viewModel.resetSearch()}, // Reset the search
            onStockSelected = { stock ->
                viewModel.onStockCodeChange(stock.stockCode) // Set the stock code
                viewModel.onDescriptionChange(stock.stockDescription) // Set the description
                viewModel.onCostChange(stock.cost) // Set the cost
                viewModel.onSellingPriceChange(stock.sellingPrice) // Set the selling price
                showSearchDialog = false
                viewModel.resetSearch()
            }
        )
    }

    if (showConfirmationDialog) { // show dialog to confirm deletion
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text(text = "Confirm Deletion") },
            text = {
                Text(
                    "Are you sure you want to delete this Stock?\n\n" +
                            "Stock Code: $stockCode\n" +
                            "Description: $description\n" +
                            "Cost: $cost\n" +
                            "Selling Price: $sellingPrice\n"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStock() //call delete stock function
                        showConfirmationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                ) { Text("Yes") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmationDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Red)
                ) { Text("Cancel") }
            },
            containerColor = Color.White
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Orange
            ) // Back button to go to the previous screen
        }
    }
        Text(
            text = if (isEditMode) "Edit Mode" else "Creation Mode", // change text depending on mode
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Orange,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .clickable { viewModel.toggleMode() }
        )
        Text(
            text = "(Tap text above to switch mode)", // text to explain switching modes
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField( // Text field for stock code
                value = stockCode,
                onValueChange = { if (isEditMode) viewModel.onStockCodeChange(it) },
                label = { Text(if (isEditMode) "Select a Stock Code via the Search Button" else "Auto Account Code") },
                enabled = isEditMode && stockCode.isNotBlank(),
                modifier = Modifier.weight(1f),
                singleLine = true,
                readOnly = true
            )

            if (isEditMode) { // If in edit mode, show the search button
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { showSearchDialog = true }, // Show the search dialog when clicked
                    colors = ButtonDefaults.buttonColors(containerColor = LightGreen),
                    modifier = Modifier.height(56.dp),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField( // Text field for description
            value = description,
            onValueChange = { input ->
                if (input.length <= 50) {
                    val sanitised = input.trimStart().replace("  ", " ")
                    // Prevent starting with space and prevent double spacing
                    viewModel.onDescriptionChange(sanitised)
                }
            },
            label = { Text("Stock Description *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled= stockCode.isNotBlank()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField( // Text field for cost
            value = costTfv,
            onValueChange = { input ->
                if (input.text.length <= 15) {
                    if (input.text.count { it == '.' } <= 1 && input.text.all { it.isDigit() || it == '.' }) {
                        // Only allow digits and max one decimal point
                        costTfv = input
                        viewModel.onCostChange(input.text.toDoubleOrNull() ?: 0.0)
                    }
                }
            },
            label = { Text("Cost of Item * ") },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState -> // on selecting input, highlight all text
                    if (focusState.isFocused) {
                        val text = costTfv.text
                        costTfv = costTfv.copy(selection = TextRange(0, text.length))
                    }
                },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            enabled = stockCode.isNotBlank()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField( // Text field for selling price
            value = sellingPriceTfv,
            onValueChange = { input ->
                if (input.text.length <= 15) {
                    if (input.text.count { it == '.' } <= 1 && input.text.all { it.isDigit() || it == '.' }) { // Only allow digits and max one decimal point
                    // Only allow digits and max one decimal point
                    sellingPriceTfv = input
                    viewModel.onSellingPriceChange(input.text.toDoubleOrNull() ?: 0.0)
                    }
                }
            },
            label = { Text("Selling Price * ") },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->// on selecting input, highlight all text
                    if (focusState.isFocused) {
                        val text = sellingPriceTfv.text
                        sellingPriceTfv = sellingPriceTfv.copy(selection = TextRange(0, text.length))
                    }
                },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            enabled = stockCode.isNotBlank()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button( // Button to save or update the stock
                onClick = { viewModel.saveStock() },
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                modifier = Modifier.weight(1f),
                enabled = stockCode.isNotBlank()
            ) {
                Text(
                    if (isEditMode) "Update Details" else "Confirm Creation" // change title depending on mode
                )
            }

            if (isEditMode) { // If in edit mode, show the delete button
                Button(
                    onClick = { showConfirmationDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.weight(1f),
                    enabled = stockCode.isNotBlank()
                ) {
                    Text("Delete Stock")
                }
            }
        }
    }
}

@Composable
fun StockCreationSearchDialog( // Search dialog for selecting a stock item
    viewModel: StockViewModel,
    onDismiss: () -> Unit,
    onStockSelected: (StockMaster) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState() // State to hold the search query
    val filteredStock by viewModel.filteredStock.collectAsState() // State to hold the filtered stock items

    Dialog(onDismissRequest = onDismiss) { // Dialog to show the search results
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text( // header for the search dialog
                    text = "Select Stock Item",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField( // Text field for searching by code or description
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    label = { Text("Search by code or description") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(filteredStock) { stock -> // loop through stock master to find filtered stock item
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onStockSelected(stock) }
                                .padding(vertical = 12.dp)
                        ) {
                            Text(stock.stockCode, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)) // Display the stock code
                            Text(stock.stockDescription, modifier = Modifier.weight(2f)) // Display the stock description
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}