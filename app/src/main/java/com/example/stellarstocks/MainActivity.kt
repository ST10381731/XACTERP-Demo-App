package com.example.stellarstocks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.stellarstocks.ui.navigation.Screen
import com.example.stellarstocks.ui.screens.DebtorCreationScreen
import com.example.stellarstocks.ui.screens.DebtorCreationSearchDialog
import com.example.stellarstocks.ui.screens.DebtorDetailsScreen
import com.example.stellarstocks.ui.screens.StockAdjustmentScreen
import com.example.stellarstocks.ui.screens.StockCreationScreen
import com.example.stellarstocks.ui.screens.StockDetailsScreen
import com.example.stellarstocks.ui.screens.StockSearchDialog
import com.example.stellarstocks.ui.theme.Black
import com.example.stellarstocks.ui.theme.DarkGreen
import com.example.stellarstocks.ui.theme.LightGreen
import com.example.stellarstocks.ui.theme.StellarStocksTheme
import com.example.stellarstocks.ui.theme.Yellow
import com.example.stellarstocks.viewmodel.DebtorViewModel
import com.example.stellarstocks.viewmodel.DebtorViewModelFactory
import com.example.stellarstocks.viewmodel.InvoiceViewModel
import com.example.stellarstocks.viewmodel.InvoiceViewModelFactory
import com.example.stellarstocks.viewmodel.StockViewModel
import com.example.stellarstocks.viewmodel.StockViewModelFactory
import java.time.LocalDate
import kotlin.math.floor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StellarStocksTheme {
                AppEntryPoint()
            }
        }
    }
}

data class BottomNavItem( // bottom navigation bar
    val label: String,
    val icon: ImageVector,
    val route: String
)

fun createWavePath( // image for landing page
    width: Float,
    height: Float,
    waveHeight: Float
): Path { //function draws a wave from top left to right
    return Path().apply {
        reset()
        // Start at top-left
        moveTo(0f, 0f)
        // Draw line down to the start of the wave
        lineTo(0f, height)

        cubicTo(
            x1 = width * 0.25f, y1 = height + waveHeight, // pulls down
            x2 = width * 0.75f, y2 = height - waveHeight, // pulls up
            x3 = width, y3 = height                       // End point
        )

        // Close the shape by going to top-right and back to start
        lineTo(width, 0f)
        close()
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false
) {
    Text(
        text = text,
        modifier = Modifier
            .border(1.dp, Color.LightGray)
            .weight(weight)
            .padding(8.dp),
        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal
    )
}

@Composable
fun AppEntryPoint() {
    val pagerState = rememberPagerState(pageCount = { 2 })

    VerticalPager( //view pager for landing page and main app
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        when (page) {
            0 -> LandingPage()
            1 -> MainApp()
        }
    }
}

@Composable
fun LandingPage() { //landing page for app
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val wavePath = createWavePath(
                width = size.width,
                height = size.height * 0.45f,
                waveHeight = 100f
            )
            drawPath(path = wavePath, color = Yellow)
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            val wavePath = createWavePath(
                width = size.width,
                height = size.height * 0.32f,
                waveHeight = 100f
            )
            drawPath(path = wavePath, color = DarkGreen)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.48f),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "Name + Logo",
                fontSize = 40.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 40.dp)
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = "Swipe up",
                tint = DarkGreen,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Swipe to continue",
                color = DarkGreen,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController() // navigation controller
    val context = LocalContext.current // context for database

    val app = context.applicationContext as StellarStocksApplication // application context
    app.database
    val repository = app.repository // repository for database


    val debtorViewModel: DebtorViewModel = viewModel(factory = DebtorViewModelFactory(repository)) // view model for debtor
    val stockViewModel: StockViewModel = viewModel(factory = StockViewModelFactory(repository)) // view model for stock

    val navItems = listOf( // bottom navigation bar items
        BottomNavItem("Home", Icons.Default.Home, Screen.Home.route),
        BottomNavItem("Stocks", Icons.Default.Inventory, Screen.StockMenu.route),
        BottomNavItem("Invoices", Icons.Default.Description, Screen.Invoice.route),
        BottomNavItem("Debtors", Icons.Default.People, Screen.DebtorMenu.route)
    )

    Scaffold(
        bottomBar = {
            NavigationBar { // bottom navigation bar
                val navBackStackEntry by navController.currentBackStackEntryAsState() // current back stack entry
                val currentDestination = navBackStackEntry?.destination // current destination

                navItems.forEach { item ->// loop through navigation items
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true, // check if current destination is the same as the item
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { // pop up to start destination
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost( // navigation host
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() } // home screen

            composable(Screen.Invoice.route) { InvoiceScreen(stockViewModel, debtorViewModel) } // invoice screen

            composable(Screen.StockMenu.route) { StockMenuScreen(navController) } // stock menu screen

            composable(Screen.StockEnquiry.route) {
                StockEnquiryScreen(stockViewModel, navController) // stock enquiry screen
            }

            composable(Screen.StockDetails.route) { backStackEntry -> // stock details screen
                val stockCode = backStackEntry.arguments?.getString("stockCode")
                if (stockCode != null) {
                    StockDetailsScreen(stockCode, stockViewModel, navController)
                }
            }

            composable(Screen.StockCreation.route) { // stock creation screen
                StockCreationScreen(stockViewModel, navController)
            }

            composable(Screen.StockAdjustment.route) { // stock adjustment screen
                StockAdjustmentScreen(viewModel = stockViewModel, navController = navController)
            }

            composable(Screen.DebtorMenu.route) { DebtorMenuScreen(navController) } // debtor menu screen

            composable(Screen.DebtorEnquiry.route) { // debtor enquiry screen
                DebtorEnquiryScreen(debtorViewModel, navController)
            }

            composable(Screen.DebtorCreation.route) { // debtor creation screen
                DebtorCreationScreen(debtorViewModel, navController)
            }

            composable(Screen.DebtorDetails.route) { backStackEntry -> // debtor details screen
                val accountCode = backStackEntry.arguments?.getString("accountCode")
                if (accountCode != null) {
                    DebtorDetailsScreen(accountCode, debtorViewModel, navController)
                }
            }
        }
    }
}

@Composable
fun HomeScreen() { // home screen
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Home Screen. Need to research graphing")
    }
}

@Composable
fun InvoiceScreen(
    stockViewModel: StockViewModel = viewModel(factory = StockViewModelFactory((LocalContext.current.applicationContext as StellarStocksApplication).repository)),
    debtorViewModel: DebtorViewModel = viewModel(factory = DebtorViewModelFactory((LocalContext.current.applicationContext as StellarStocksApplication).repository)),
    invoiceViewModel: InvoiceViewModel = viewModel(factory = InvoiceViewModelFactory((LocalContext.current.applicationContext as StellarStocksApplication).repository))
) {
    var showStockSearchDialog by remember { mutableStateOf(false) } // stock search dialog state
    var showDebtorSearchDialog by remember { mutableStateOf(false) } // debtor search dialog state
    var showQtyDialog by remember { mutableStateOf(false) } // quantity dialog state

    var tempSelectedStock by remember { mutableStateOf<com.example.stellarstocks.data.db.models.StockMaster?>(null) } // temporary selected stock state for invoice preview

    var isEditMode by remember { mutableStateOf(false) } // edit mode for quantity dialog
    var editInitialQty by remember { mutableIntStateOf(1) } // initial quantity for quantity dialog
    var editInitialDiscount by remember { mutableDoubleStateOf(0.0) } // initial discount for quantity dialog

    val scrollState = rememberScrollState() // scroll state for column
    val context = LocalContext.current

    // Invoice State
    val selectedDebtor by invoiceViewModel.selectedDebtor.collectAsState() // selected debtor state
    val invoiceItems by invoiceViewModel.invoiceItems.collectAsState() // invoice items state
    val totalExVat by invoiceViewModel.totalExVat.collectAsState() // total without VAT
    val vat by invoiceViewModel.vat.collectAsState() // VAT state
    val grandTotal by invoiceViewModel.grandTotal.collectAsState() // grand total
    val toastMessage by invoiceViewModel.toastMessage.collectAsState()
    val invoiceNum by invoiceViewModel.invoiceNum.collectAsState()


    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            invoiceViewModel.clearToast()
        }
    }


    if (showStockSearchDialog) { //show search stock dialog
        StockSearchDialog(
            viewModel = stockViewModel,
            onDismiss = { showStockSearchDialog = false },
            onStockSelected = { stock ->
                tempSelectedStock = stock // set stock added to invoice
                isEditMode = false
                editInitialQty = 1
                editInitialDiscount = 0.0
                showStockSearchDialog = false
                showQtyDialog = true
            }
        )
    }

    if (showQtyDialog && tempSelectedStock != null) { //show quantity dialog after adding an item to invoice
        AddStockDialog(
            stock = tempSelectedStock!!, // stock added to invoice cannot be null
            initialQty = editInitialQty,
            initialDiscount = editInitialDiscount,
            isEditMode = isEditMode,
            onDismiss = { showQtyDialog = false }, //dismiss dialog if user cancels
            onConfirm = { qty, discount ->
                if (isEditMode) {

                    invoiceViewModel.updateInvoiceItem(tempSelectedStock!!, qty, discount)// Update existing item
                } else {

                    invoiceViewModel.addToInvoice(tempSelectedStock!!, qty, discount) // Add new item
                }
                showQtyDialog = false
                tempSelectedStock = null
            }
        )
    }

    if (showDebtorSearchDialog) { //show search debtor dialog
        DebtorCreationSearchDialog(
            viewModel = debtorViewModel,
            onDismiss = { showDebtorSearchDialog = false },
            onDebtorSelected = { debtor ->
                invoiceViewModel.setDebtor(debtor)
                showDebtorSearchDialog = false
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
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween // space between buttons
        ) {
            Button( // button for selecting a debtor
                onClick = { showDebtorSearchDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = LightGreen),
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Icon(Icons.Default.People, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Select Debtor")
            }

            Button( // button for adding a stock item
                onClick = { showStockSearchDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = LightGreen),
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Icon(Icons.Default.Inventory, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add Item")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TicketView( // ticket view background for invoice
            content = {
                Column {
                    Text("INVOICE #${invoiceNum}", fontWeight = FontWeight.Bold, fontSize = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Text(LocalDate.now().toString(), textAlign = TextAlign.Center, color = Color.Gray, modifier = Modifier.fillMaxWidth()) // date of invoice

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal excl. VAT:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Black)
                        Text(String.format("R%.2f", totalExVat), fontSize = 12.sp, color = Black)
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("VAT (15%):", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Black)
                        Text(String.format("R%.2f", vat), fontSize = 12.sp, color = Black)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TOTAL:", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkGreen)
                        Text(String.format("R%.2f", grandTotal), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkGreen)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Debtor Section
                    if (selectedDebtor != null) { // show debtor details if selected
                        Text("BILL TO:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                        Text(selectedDebtor!!.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkGreen)
                        Text(selectedDebtor!!.accountCode, fontSize = 14.sp, color = Black)
                        Text(selectedDebtor!!.address1, fontSize = 12.sp, color= Black)
                    } else {
                        Text("No Debtor Selected", color = Color.Red, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Invoice Details separator
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color(0xff000000)).padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Item (Tap to Edit)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Total", fontWeight = FontWeight.Bold, fontSize = 12.sp) //per line item total
                    }

                    // Invoice Items List
                    if (invoiceItems.isEmpty()) {
                        Text("Invoice is empty", modifier = Modifier.padding(vertical = 20.dp).fillMaxWidth(), textAlign = TextAlign.Center, color = Color.Gray)
                    } else {
                        invoiceItems.forEach { item -> // loop through invoice items
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        tempSelectedStock = item.stock
                                        editInitialQty = item.qty
                                        editInitialDiscount = item.discountPercent
                                        isEditMode = true
                                        showQtyDialog = true
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.stock.stockDescription, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkGreen) // stock description
                                    Row {
                                        Text("${item.qty} x R${item.stock.sellingPrice}", fontSize = 12.sp, color = Color.Gray) // quantity and price
                                        if (item.discountPercent > 0) {
                                            Spacer(Modifier.width(8.dp))
                                            Text("(-${item.discountPercent}%)", fontSize = 12.sp, color = Color.Red) // discount percent
                                        }
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(String.format("R%.2f", item.lineTotal), fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp), color= Black) // per line total

                                    Icon( // clickable delete icon
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint = Color.Red,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable { invoiceViewModel.removeFromInvoice(item) }
                                    )
                                }
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                        }
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(24.dp))


        Button(// Confirm Button
            onClick = { invoiceViewModel.confirmInvoice() },
            colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = selectedDebtor != null && invoiceItems.isNotEmpty()
        ) {
            Text("CONFIRM & PROCESS INVOICE", fontWeight = FontWeight.Bold)
        }
    }
}


@Composable
fun AddStockDialog(// Dialog to add a stock item
    stock: com.example.stellarstocks.data.db.models.StockMaster,
    initialQty: Int = 1,
    initialDiscount: Double = 0.0,
    isEditMode: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (Int, Double) -> Unit
) {
    var qtyText by remember { mutableStateOf(initialQty.toString()) } // quantity state
    var discountText by remember { mutableStateOf(initialDiscount.toString()) } // discount state

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) { //dialog for adding a stock item
        Card( //card to display items
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isEditMode) "Edit Item" else "Add Item", //switch text depending on mode
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(stock.stockDescription, fontWeight = FontWeight.Medium, color = DarkGreen) // stock description
                Text("Price: R${stock.sellingPrice}", color = Color.Gray, fontSize = 12.sp) //stock selling price

                Spacer(Modifier.height(16.dp))

                OutlinedTextField( // text field for quantity
                    value = qtyText,
                    onValueChange = { qtyText = it },
                    label = { Text("Quantity") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField( // text field for discount
                    value = discountText,
                    onValueChange = { discountText = it },
                    label = { Text("Discount %") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") } // cancel button
                    Button(
                        onClick = {
                            val qty = qtyText.toIntOrNull() ?: 0 // get quantity from text field
                            val disc = discountText.toDoubleOrNull() ?: 0.0 // get discount from text field
                            if (qty > 0) onConfirm(qty, disc) // check if quantity is greater than 0
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                    ) { Text(if (isEditMode) "Update" else "Add") } // change button based on mode
                }
            }
        }
    }
}

/*
* Title- Make a Ticket View with Jetpack Compose
* Author- Kush Saini
* Accessed- 30/01/2026
* URL- https://medium.com/@kushsaini/make-a-ticketview-with-jetpack-compose-ea0c8f7a00a8
* */
@Preview
@Composable
private fun TicketView( // custom background for invoice
    content: @Composable () -> Unit = {
        Text("Ticket View")
    }
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primary
    ) {

        Box(
            modifier = Modifier
                .padding(8.dp)
                .shadow(
                    2.dp,
                    shape = TicketShape(8f, 4f),
                    clip = true
                )
                .background(Color.White)
        ) {

            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

/*
* Title- Make a Ticket View with Jetpack Compose
* Author- Kush Saini
* Accessed- 30/01/2026
* URL- https://medium.com/@kushsaini/make-a-ticketview-with-jetpack-compose-ea0c8f7a00a8
* */
class TicketShape(
    private val teethWidthDp: Float,
    private val teethHeightDp: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ) = Outline.Generic(Path().apply {

        moveTo(
            size.width * 0.99f,
            size.height * 0.01f
        )

        val teethHeightPx = teethHeightDp * density.density
        var fullTeethWidthPx = teethWidthDp * density.density
        var halfTeethWidthPx = fullTeethWidthPx / 2
        var currentDrawPositionX = size.width * 0.99f
        var teethBasePositionY = size.height * 0.01f + teethHeightPx
        val shapeWidthPx = size.width * 0.99f - size.width * 0.01f

        val teethCount = shapeWidthPx / fullTeethWidthPx
        val minTeethCount = floor(teethCount)

        if (teethCount != minTeethCount) { // check to allow drawing if shape width is a multiple of teeth count
            val newTeethWidthPx = shapeWidthPx / minTeethCount
            fullTeethWidthPx = newTeethWidthPx
            halfTeethWidthPx = fullTeethWidthPx / 2
        }

        var drawnTeethCount = 1

        // draw half of first teeth
        lineTo(
            currentDrawPositionX - halfTeethWidthPx,
            teethBasePositionY + teethHeightPx
        )

        // draw remaining teethes
        while (drawnTeethCount < minTeethCount) {

            currentDrawPositionX -= halfTeethWidthPx

            // draw right half of teeth
            lineTo(
                currentDrawPositionX - halfTeethWidthPx,
                teethBasePositionY - teethHeightPx
            )

            currentDrawPositionX -= halfTeethWidthPx

            // draw left half of teeth
            lineTo(
                currentDrawPositionX - halfTeethWidthPx,
                teethBasePositionY + teethHeightPx
            )

            drawnTeethCount++
        }

        currentDrawPositionX -= halfTeethWidthPx

        // draw half of last teeth
        lineTo(
            currentDrawPositionX - halfTeethWidthPx,
            teethBasePositionY - teethHeightPx
        )

        // draw left edge
        lineTo(
            size.width * 0.01f,
            size.height * 0.99f
        )

        drawnTeethCount = 1
        teethBasePositionY = size.height * 0.99f - teethHeightPx
        currentDrawPositionX = size.width * 0.01f

        // draw half of first teeth
        lineTo(
            currentDrawPositionX,
            teethBasePositionY + teethHeightPx
        )

        lineTo(
            currentDrawPositionX + halfTeethWidthPx,
            teethBasePositionY - teethHeightPx
        )

        // draw remaining teethes
        while (drawnTeethCount < minTeethCount) {

            currentDrawPositionX += halfTeethWidthPx

            // draw left half of teeth
            lineTo(
                currentDrawPositionX + halfTeethWidthPx,
                teethBasePositionY + teethHeightPx
            )

            currentDrawPositionX += halfTeethWidthPx

            // draw right half of teeth
            lineTo(
                currentDrawPositionX + halfTeethWidthPx,
                teethBasePositionY - teethHeightPx
            )

            drawnTeethCount++
        }

        currentDrawPositionX += halfTeethWidthPx

        // draw half of last teeth
        lineTo(
            currentDrawPositionX + halfTeethWidthPx,
            teethBasePositionY + teethHeightPx
        )

        // left edge will automatically be drawn to close the path with the top-left arc
        close()
    })

}

data class DebtorMenuItemData( // data class for debtor menu tiles
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String
)

val debtorMenuItems = listOf( // list of debtor menu tiles
    DebtorMenuItemData(
        title = "Debtor Enquiry",
        description = "Search and view debtors",
        icon = Icons.Default.Search,
        route = Screen.DebtorEnquiry.route
    ),
    DebtorMenuItemData(
        title = "Debtor Maintenance",
        description = "Create and edit debtors",
        icon = Icons.Default.Edit,
        route = Screen.DebtorCreation.route
    )
)

@Composable
fun DebtorMenuTile(item: DebtorMenuItemData, navController: NavController) { // debtor menu tile layouts
    Card(
        modifier = Modifier
            .padding(8.dp)
            .clickable { navController.navigate(item.route) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = LightGreen)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .size(160.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(item.icon, contentDescription = item.title, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))

            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))

            Text(item.description, textAlign = TextAlign.Center, fontSize = 12.sp, color = Color.Gray)
        }
    }
}


data class StockMenuItemData( // data class for stock menu tiles
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String
)

val stockMenuItems = listOf( // list of stock menu tiles
    StockMenuItemData(
        title = "Stock Enquiry",
        description = "Search and view stock items",
        icon = Icons.Default.Search,
        route = Screen.StockEnquiry.route
    ),
    StockMenuItemData(
        title = "Stock Maintenance",
        description = "Create and edit stock items",
        icon = Icons.Default.Edit,
        route = Screen.StockCreation.route
    ),
    StockMenuItemData(
        title = "Stock Adjustment",
        description = "Adjust stock levels",
        icon = Icons.Default.AddShoppingCart,
        route = Screen.StockAdjustment.route
    )
)

@Composable
fun StockMenuTile(item: StockMenuItemData, navController: NavController) { // stock menu tile layouts
    Card(
        modifier = Modifier
            .padding(8.dp)
            .clickable { navController.navigate(item.route) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = LightGreen)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .size(160.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(item.icon, contentDescription = item.title, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))

            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))

            Text(item.description, textAlign = TextAlign.Center, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun StockMenuScreen(navController: NavController) { // stock menu screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Stocks Menu",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGreen,
            modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(stockMenuItems) { item ->
                StockMenuTile(item = item, navController = navController)
            }
        }
    }
}

@Composable
fun DebtorMenuScreen(navController: NavController) { // debtor menu screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Debtors Menu",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGreen,
            modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(debtorMenuItems) { item ->
                DebtorMenuTile(item = item, navController = navController)
            }
        }
    }
}

@Composable
fun DebtorEnquiryScreen(debtorViewModel: DebtorViewModel, navController: NavController) { // debtor enquiry screen
    val debtorList by debtorViewModel.filteredDebtors.collectAsState() // list of debtors
    val searchQuery by debtorViewModel.searchQuery.collectAsState() // search query to filter debtors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            IconButton(onClick = { navController.popBackStack() }) { //back button to navigate back to debtors menu
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = DarkGreen
                )
            }
            Text(
                "Debtor Enquiry",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen
            )
        }
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { debtorViewModel.onSearchQueryChange(it) },
            label = { Text("Search Account Code or Name") }, // label for search field
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        Row(Modifier.fillMaxWidth()) {
            TableCell(text = "Code", weight = .25f, isHeader = true)
            TableCell(text = "Name", weight = .5f, isHeader = true)
            TableCell(text = "Balance", weight = .25f, isHeader = true)
        }

        if (debtorList.isEmpty()) { // if no debtors found, display message
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No debtors found matching '$searchQuery'", color = Color.Gray)
            }
        } else { // if debtors found, display list of debtors
            LazyColumn {
                items(debtorList) { debtor ->
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(Screen.DebtorDetails.createRoute(debtor.accountCode))
                            }
                    ) {
                        TableCell(text = debtor.accountCode, weight = .25f)
                        TableCell(text = debtor.name, weight = .5f)
                        TableCell(text = String.format("R%.2f", debtor.balance) , weight = .25f)
                    }
                }
            }
        }
    }
}

@Composable
fun StockEnquiryScreen(stockViewModel: StockViewModel, navController: NavController) { // stock enquiry screen
    val stockList by stockViewModel.filteredStock.collectAsState()
    val searchQuery by stockViewModel.searchQuery.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = DarkGreen
                )
            }
            Text("Stock Enquiry", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
        }
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { stockViewModel.onSearchQueryChange(it) },
            label = { Text("Search Stock Code or Name") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        Row(Modifier.fillMaxWidth()) {
            TableCell(text = "Code", weight = .4f, isHeader = true)
            TableCell(text = "Description", weight = .6f, isHeader = true)
            TableCell(text = "Qty", weight = .4f, isHeader = true)
            TableCell(text = "Cost", weight = .4f, isHeader = true)
        }

        if (stockList.isEmpty()) { // if no stock found, display message
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No stock found matching '$searchQuery'", color = Color.Gray)
            }
        } else { // if stock found, display list of stock
            LazyColumn {
                items(stockList) { stock ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(Screen.StockDetails.createRoute(stock.stockCode))
                            }
                    ) {
                        TableCell(text = stock.stockCode, weight = .4f)
                        TableCell(text = stock.stockDescription, weight = .6f)
                        TableCell(text = stock.stockOnHand.toString(), weight = .4f)
                        TableCell(text = String.format("R%.2f", stock.cost), weight = .4f)
                    }
                }
            }
        }
    }
}
