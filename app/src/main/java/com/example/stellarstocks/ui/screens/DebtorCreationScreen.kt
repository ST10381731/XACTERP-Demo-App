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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.example.stellarstocks.ui.theme.DarkGreen
import com.example.stellarstocks.ui.theme.LightGreen
import com.example.stellarstocks.ui.theme.Orange
import com.example.stellarstocks.viewmodel.DebtorViewModel
import com.example.stellarstocks.viewmodel.StockViewModel

@Composable
fun DebtorCreationScreen(viewModel: DebtorViewModel = viewModel(), navController: NavController) {
    val isEditMode by viewModel.isEditMode.collectAsState()
    val accountCode by viewModel.accountCode.collectAsState()
    val name by viewModel.name.collectAsState()
    val address1 by viewModel.address1.collectAsState()
    val address2 by viewModel.address2.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val context = LocalContext.current

    val scrollState = rememberScrollState()

    var showSearchDialog by remember { mutableStateOf(false) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    LaunchedEffect(Unit) {
        if (!isEditMode) viewModel.generateNewCode()
    }

    LaunchedEffect(Unit) {
        viewModel.navigationChannel.collect { shouldNavigate ->
            if (shouldNavigate) {
                navController.navigate(Screen.DebtorEnquiry.route) {
                    popUpTo(Screen.DebtorMenu.route) { inclusive = false }
                }
            }
        }
    }

    if (showSearchDialog) {
        DebtorCreationSearchDialog(
            viewModel = viewModel,
            onDismiss = { showSearchDialog = false },
            onDebtorSelected = { debtor ->
                viewModel.onSearchCodeChange(debtor.accountCode)
                viewModel.onNameChange(debtor.name)
                viewModel.onAddress1Change(debtor.address1)
                viewModel.onAddress2Change(debtor.address2)
                showSearchDialog = false
            }
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
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Orange
                )
            }
        }
            Text(
                text = if (isEditMode) "Edit Mode" else "Creation Mode",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Orange,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .clickable { viewModel.toggleMode() }
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
            OutlinedTextField(
                value = accountCode,
                onValueChange = { if (isEditMode) viewModel.onSearchCodeChange(it) },
                label = { Text(if (isEditMode) "Select an Account Code via the Search Button" else "Auto Account Code") },
                enabled = isEditMode,
                modifier = Modifier.weight(1f),
                singleLine = true,
                readOnly = true
            )

            if (isEditMode) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { showSearchDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = LightGreen),
                    modifier = Modifier.height(56.dp),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { viewModel.onNameChange(it) },
            label = { Text("Client Name *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = address1,
            onValueChange = { viewModel.onAddress1Change(it) },
            label = { Text("Primary Address (Required)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 3,
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = address2,
            onValueChange = { viewModel.onAddress2Change(it) },
            label = { Text("Secondary Address (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 3,
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { viewModel.saveDebtor() },
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isEditMode) "Update Details" else "Confirm Creation")
            }

            if (isEditMode) {
                Button(
                    onClick = { viewModel.deleteDebtor() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Delete Debtor")
                }
            }
        }
    }
}

@Composable
fun DebtorCreationSearchDialog(
    viewModel: DebtorViewModel,
    onDismiss: () -> Unit,
    onDebtorSelected: (DebtorMaster) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredDebtor by viewModel.filteredDebtors.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Debtor",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    label = { Text("Search by account code or name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
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


