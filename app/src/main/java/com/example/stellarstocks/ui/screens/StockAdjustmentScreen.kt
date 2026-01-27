package com.example.stellarstocks.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stellarstocks.ui.theme.DarkGreen
import com.example.stellarstocks.ui.theme.LightGreen
import com.example.stellarstocks.viewmodel.StockViewModel
import kotlin.math.roundToInt

@Composable
fun StockAdjustmentScreen(viewModel: StockViewModel = viewModel()) {
    val searchCode by viewModel.adjustmentSearchCode.collectAsState()
    val foundStock by viewModel.foundAdjustmentStock.collectAsState()
    val adjustmentQty by viewModel.adjustmentQty.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Stock Adjustment",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGreen,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchCode,
                onValueChange = { viewModel.onAdjustmentSearchChange(it) },
                label = { Text("Enter Stock Code") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { viewModel.searchForAdjustment() },
                colors = ButtonDefaults.buttonColors(containerColor = LightGreen),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.height(56.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (foundStock != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
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

            // Slider
            Slider(
                value = adjustmentQty.toFloat(),
                onValueChange = { viewModel.onAdjustmentQtyChange(it.roundToInt()) },
                valueRange = -50f..50f,
                colors = SliderDefaults.colors(thumbColor = DarkGreen, activeTrackColor = LightGreen),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = adjustmentQty.toString(),
                onValueChange = {
                    val num = it.toIntOrNull()
                    if (num != null) {// Only update if it's a valid number
                        viewModel.onAdjustmentQtyChange(num)
                    } else if (it.isEmpty() || it == "-") { // Allow user type negative sign
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