package com.example.stellarstocks.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stellarstocks.ui.theme.DarkGreen
import com.example.stellarstocks.ui.theme.LightGreen
import com.example.stellarstocks.viewmodel.DebtorViewModel

@Composable
fun DebtorCreationScreen(viewModel: DebtorViewModel = viewModel()) {
    val isEditMode by viewModel.isEditMode.collectAsState()
    val accountCode by viewModel.accountCode.collectAsState()
    val name by viewModel.name.collectAsState()
    val address1 by viewModel.address1.collectAsState()
    val address2 by viewModel.address2.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val context = LocalContext.current


    val scrollState = rememberScrollState() // Allow scrolling if the keyboard covers the bottom buttons

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
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
            color = DarkGreen,
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
                value = accountCode,
                onValueChange = { if (isEditMode) viewModel.onSearchCodeChange(it) },
                label = { Text(if (isEditMode) "Search Account Code" else "Auto Account Code") },
                enabled = isEditMode,
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            if (isEditMode) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.searchDebtor() },
                    colors = ButtonDefaults.buttonColors(containerColor = LightGreen),
                    // Align button height with text field
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