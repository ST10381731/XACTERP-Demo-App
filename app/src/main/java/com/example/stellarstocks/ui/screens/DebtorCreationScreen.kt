package com.example.stellarstocks.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.stellarstocks.data.db.models.DebtorMaster
import com.example.stellarstocks.data.db.models.StockMaster
import com.example.stellarstocks.ui.navigation.Screen
import com.example.stellarstocks.ui.theme.Black
import com.example.stellarstocks.ui.theme.DarkGreen
import com.example.stellarstocks.ui.theme.LightGreen
import com.example.stellarstocks.ui.theme.Orange
import com.example.stellarstocks.ui.theme.Red
import com.example.stellarstocks.viewmodel.DebtorViewModel
import com.example.stellarstocks.viewmodel.StockViewModel

@Composable
fun DebtorCreationScreen(viewModel: DebtorViewModel = viewModel(), navController: NavController) {
    val accountCode by viewModel.accountCode.collectAsState()
    val name by viewModel.name.collectAsState()
    var showConfirmationDialog by remember { mutableStateOf(false) } // delete confirmation dialog state


    // Address 1 States
    val addrLine1 by viewModel.addrLine1.collectAsState()
    val addrLine2 by viewModel.addrLine2.collectAsState()
    val suburb by viewModel.suburb.collectAsState()
    val postalCode by viewModel.postalCode.collectAsState()

    // Address 2 States
    val showAddress2 by viewModel.showAddress2.collectAsState()
    val addr2Line1 by viewModel.addr2Line1.collectAsState()
    val addr2Line2 by viewModel.addr2Line2.collectAsState()
    val addr2Suburb by viewModel.addr2Suburb.collectAsState()
    val addr2PostalCode by viewModel.addr2PostalCode.collectAsState()


    val isEditMode by viewModel.isEditMode.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val context = LocalContext.current

    val scrollState = rememberScrollState() // default scroll state for the column

    var showSearchDialog by remember { mutableStateOf(false) } // variable to determine if search dialog should be shown

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    LaunchedEffect(Unit) {
        if (!isEditMode) viewModel.generateNewCode() // if not in edit mode, generate a new code
    }

    LaunchedEffect(Unit) {
        viewModel.navigationChannel.collect { shouldNavigate ->
            if (shouldNavigate) {
                navController.navigate(Screen.DebtorEnquiry.route) { // navigate to debtor enquiry screen
                    popUpTo(Screen.DebtorMenu.route) { inclusive = false }
                }
            }
        }
    }

    if (showSearchDialog) {
        DebtorCreationSearchDialog(
            viewModel = viewModel,
            onDismiss = {
                showSearchDialog = false
                viewModel.resetSearch()
            },
            onDebtorSelected = { debtor ->
                viewModel.onSearchCodeChange(debtor.accountCode)
                viewModel.loadDebtorDetails(debtor.accountCode) // loadDebtorDetails to split address
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
                    "Are you sure you want to delete this Debtor?\n\n" +
                            "Account Code: $accountCode\n" +
                            "Name: $name"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDebtor() //call delete debtor function
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
            IconButton(onClick = { navController.popBackStack() }) { // navigate back to debtor menu
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Orange
                )
            }
        }
            Text(
                text = if (isEditMode) "Edit Mode" else "Creation Mode", // change text based on mode
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Orange,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .clickable { viewModel.toggleMode() } // toggle between edit and creation mode on click
            )

        Text(
            text = "(Tap text above to switch mode)",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField( // text field for account code
                value = accountCode,
                onValueChange = { if (isEditMode) viewModel.onSearchCodeChange(it) },
                label = { Text(if (isEditMode) "Select an Account Code via the Search Button" else "Auto Account Code") },
                enabled = isEditMode && accountCode.isNotBlank(),
                modifier = Modifier.weight(1f),
                singleLine = true,
                readOnly = true
            )

            if (isEditMode) { // if in edit mode, show search button
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { showSearchDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = LightGreen),
                    modifier = Modifier.height(56.dp),
                    shape = MaterialTheme.shapes.extraSmall,
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField( // text field for name
            value = name,
            onValueChange = { viewModel.onNameChange(it) },
            label = { Text("Client Name *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled= accountCode.isNotBlank()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Primary Address *", fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.Start).padding(top=8.dp))

        // Numbers Only
        OutlinedTextField(
            value = addrLine1,
            onValueChange = { input ->
                if (input.all { it.isDigit() }) { // Numbers only
                    viewModel.onAddrLine1Change(input)
                }
            },
            label = { Text("Street Number *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            enabled= accountCode.isNotBlank()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Address Line 2
        OutlinedTextField(
            value = addrLine2,
            onValueChange = { input ->
                if (input.all { it.isLetterOrDigit() || it.isWhitespace() }) {
                    viewModel.onAddrLine2Change(input)
                }
            },
            label = { Text("Street Name *") },
            modifier = Modifier.fillMaxWidth(),
            enabled= accountCode.isNotBlank()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth()) {
            // Suburb
            OutlinedTextField(
                value = suburb,
                onValueChange = { input ->
                    // Letters and Whitespace only
                    if (input.all { it.isLetter() || it.isWhitespace() }) {
                        viewModel.onSuburbChange(input)
                    }
                },
                label = { Text("Suburb *") },
                modifier = Modifier.weight(1f).padding(end = 4.dp),
                enabled= accountCode.isNotBlank()
            )

            // Postal Code
            OutlinedTextField(
                value = postalCode,
                onValueChange = { input ->
                    // Digits only, Max length 4
                    if (input.length <= 4 && input.all { it.isDigit() }) {
                        viewModel.onPostalCodeChange(input)
                    }
                },
                label = { Text("Postal Code *") },
                modifier = Modifier.weight(1f).padding(start = 4.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled= accountCode.isNotBlank()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Add Secondary Address? (Optional)", fontWeight = FontWeight.Bold)
            Switch(
                checked = showAddress2,
                onCheckedChange = { viewModel.onShowAddress2Change(it) },
                colors = SwitchDefaults.colors(checkedThumbColor = DarkGreen, checkedTrackColor = LightGreen),
                enabled= accountCode.isNotBlank(),
            )
        }

        if (showAddress2) {
            Text("Secondary Address", fontWeight = FontWeight.Bold, color = DarkGreen, modifier = Modifier.align(Alignment.Start))

            OutlinedTextField(
                value = addr2Line1,
                onValueChange = { input -> if (input.all { it.isDigit() }) viewModel.onAddr2Line1Change(input) },
                label = { Text("Street Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = addr2Line2,
                onValueChange = { input ->
                    if (input.all { it.isLetterOrDigit() || it.isWhitespace() }) {
                        viewModel.onAddr2Line2Change(input)
                    }
                },
                label = { Text("Street Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = addr2Suburb,
                    onValueChange = { input ->
                        if (input.all { it.isLetter() || it.isWhitespace() }) {
                            viewModel.onAddr2SuburbChange(input)
                        }
                    },
                    label = { Text("Suburb") },
                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                )
                OutlinedTextField(
                    value = addr2PostalCode,
                    onValueChange = { input ->
                        if (input.length <= 4 && input.all { it.isDigit() }) {
                            viewModel.onAddr2PostalCodeChange(input)
                        }
                    },
                    label = { Text("Postal Code") },
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { viewModel.saveDebtor() },
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                enabled = accountCode.isNotBlank()
            ) {
                Text(if (isEditMode) "Update Debtor" else "Create Debtor")
            }

            if (isEditMode) {
                Button(
                    onClick = { showConfirmationDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Red),
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    enabled = accountCode.isNotBlank()
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
fun DebtorCreationSearchDialog( // search dialog for selecting debtor
    viewModel: DebtorViewModel,
    onDismiss: () -> Unit,
    onDebtorSelected: (DebtorMaster) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState() // variable to hold search query
    val filteredDebtor by viewModel.filteredDebtors.collectAsState() // variable to hold filtered debtors

    Dialog(onDismissRequest = onDismiss) { // dialog box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text( // title of dialog
                    text = "Select Debtor",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField( // text field for search
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) }, // update search query
                    label = { Text("Search by account code or name") },
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
                LazyColumn { // Table to hold list of debtors
                    items(filteredDebtor) { debtor ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDebtorSelected(debtor) }
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                debtor.accountCode,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(debtor.name, modifier = Modifier.weight(2f))
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}


