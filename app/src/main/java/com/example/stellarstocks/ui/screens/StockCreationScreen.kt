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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.stellarstocks.ui.navigation.Screen
import com.example.stellarstocks.ui.theme.DarkGreen
import com.example.stellarstocks.ui.theme.LightGreen
import com.example.stellarstocks.ui.theme.Orange
import com.example.stellarstocks.viewmodel.DebtorViewModel
import com.example.stellarstocks.viewmodel.StockViewModel

@Composable
fun StockCreationScreen(viewModel: StockViewModel = viewModel(), navController: NavController) {
    val isEditMode by viewModel.isEditMode.collectAsState()
    val stockCode by viewModel.stockCode.collectAsState()
    val description by viewModel.description.collectAsState()
    val cost by viewModel.cost.collectAsState()
    val sellingPrice by viewModel.sellingPrice.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val context = LocalContext.current

    val scrollState = rememberScrollState()

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationChannel.collect { shouldNavigate ->
            if (shouldNavigate) {
                navController.navigate(Screen.StockEnquiry.route) {
                    popUpTo(Screen.StockMenu.route) { inclusive = false }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!isEditMode) viewModel.generateNewCode()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isEditMode) "Edit Mode" else "Creation Mode",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Orange,
            modifier = Modifier
                .padding(bottom = 16.dp)
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
                value = stockCode,
                onValueChange = { if (isEditMode) viewModel.onStockCodeChange(it) },
                label = { Text(if (isEditMode) "Search Stock Code" else "Auto Account Code") },
                enabled = isEditMode,
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            if (isEditMode) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.searchStock() },
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
            value = description,
            onValueChange = { viewModel.onDescriptionChange(it) },
            label = { Text("Stock Description *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = cost.toString(),
            onValueChange = { viewModel.onCostChange(it.toDoubleOrNull() ?: 0.0) },
            label = { Text("Cost of Item * ") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = sellingPrice.toString(),
            onValueChange = { viewModel.onSellingPriceChange(it.toDoubleOrNull() ?: 0.0) },
            label = { Text("Selling Price * ") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { viewModel.saveStock() },
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isEditMode) "Update Details" else "Confirm Creation")
            }

            if (isEditMode) {
                Button(
                    onClick = { viewModel.deleteStock() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Delete Stock")
                }
            }
        }
    }
}