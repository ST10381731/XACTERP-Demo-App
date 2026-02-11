@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.stellarstocks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
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
import com.example.stellarstocks.ui.theme.LimeGreen
import com.example.stellarstocks.ui.theme.Orange
import com.example.stellarstocks.ui.theme.ProfessionalLightBlue
import com.example.stellarstocks.ui.theme.Red
import com.example.stellarstocks.ui.theme.StellarStocksTheme
import com.example.stellarstocks.ui.theme.Yellow
import com.example.stellarstocks.viewmodel.DebtorListSortOption
import com.example.stellarstocks.viewmodel.DebtorViewModel
import com.example.stellarstocks.viewmodel.DebtorViewModelFactory
import com.example.stellarstocks.viewmodel.InvoiceItem
import com.example.stellarstocks.viewmodel.InvoiceViewModel
import com.example.stellarstocks.viewmodel.InvoiceViewModelFactory
import com.example.stellarstocks.viewmodel.StockListSortOption
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

/*
* The purpose of this function to generate the wave image used in the background of the application*/
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

/*
This composable function serves as the main entry point for the user interface. It uses a
VerticalPager to create a two-page layout that the user can swipe through vertically.
The first page is the LandingPage, which displays business analytics.
The second page is the MainApp, which contains the core functionality of the application.
*/
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

/*
 * A composable function that displays the landing page of the application,
 * The screen displays vital business information such tables of the most popular stock,
 * top debtors, and monthly sales in a graph form factor.
*/
@Composable
fun LandingPage() { //landing page for app with graph and analytics
    val context = LocalContext.current
    val app = context.applicationContext as StellarStocksApplication

    val debtorViewModel: DebtorViewModel =
        viewModel(factory = DebtorViewModelFactory(app.repository))
    val stockViewModel: StockViewModel = viewModel(factory = StockViewModelFactory(app.repository))

    val monthlySales by debtorViewModel.monthlySales.collectAsState()
    val topDebtors by debtorViewModel.topDebtors.collectAsState()
    val popularStock by stockViewModel.popularStock.collectAsState()

    val financialYearDataPoints = remember(monthlySales){ //for graph to link data points accurately to months on x axis
        val monthRemapping = mapOf(
            0 to 10,
            1 to 11,
            2 to 0,
            3 to 1,
            4 to 2,
            5 to 3,
            6 to 4,
            7 to 5,
            8 to 6,
            9 to 7,
            10 to 8,
            11 to 9
        )

        monthlySales.mapNotNull { (monthIndex, sales) ->
            // Find the new financial year index for the original calendar month
            val newIndex = monthRemapping[monthIndex.toInt()]
            if (newIndex != null) {
                // Create the new Pair with the remapped index
                Pair(newIndex.toFloat(), sales)
            } else {
                null
            }
        }.sortedBy { it.first }
    }

    Box(modifier = Modifier // background wave image
        .fillMaxSize()
        .background(Color(0xFFF5F5F5))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val wavePath =
                createWavePath(width = size.width, height = size.height * 0.25f, waveHeight = 80f)
            drawPath(path = wavePath, color = Yellow.copy(alpha = 0.5f))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item { // Title for landing page
                Text(
                    text = "Business Analytics",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 48.dp, bottom = 16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            item { // Graph of sales for the financial year on landing page
                SimpleLineChart(dataPoints = financialYearDataPoints)
            }
            item { // Table of top debtors on landing page
                DashboardTable(
                    title = "Top Debtors",
                    headers = listOf("Name", "Code", "Sales"),
                    data = topDebtors.map {
                        listOf(
                            it.name,
                            it.accountCode,
                            "R${String.format("%.2f", it.salesYearToDate)}"
                        )
                    }
                )
            }
            item { // Table of most popular stock on landing page
                DashboardTable(
                    title = "Popular Stock",
                    headers = listOf("Item", "Sold", "On Hand"),
                    data = popularStock.map {
                        listOf(
                            it.stockDescription,
                            it.qtySold.toString(),
                            it.stockOnHand.toString()
                        )
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 30.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon( // swipe up icon
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = "Swipe up",
                tint = DarkGreen,
                modifier = Modifier.size(32.dp)
            )
            Text( // hint text
                text = "Swipe to Enter App",
                color = DarkGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


/*This function creates the simple line graph found on the landing page.
* It creates a simple grid with x-axis and y-axis labels.
* It creates a list of dataPoints which is a pair of x and y values.
* It joins each dataPoint with a DarkGreen line to display the sales growth through the months.
* If the dataPoints list is empty, it displays a message "No Sales Data yet" instead of the chart.*/
@Composable
fun SimpleLineChart(
    dataPoints:List<Pair<Float, Float>>,
    lineColor: Color = DarkGreen
) {
    if (dataPoints.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(start = 50.dp, bottom = 50.dp, end = 20.dp, top = 20.dp),

            contentAlignment = Alignment.Center
        ) {
            Text("No Sales Data yet", color = Color.Gray)
        }
        return
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.White, shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Text("Total Sales for the Financial Year", fontWeight = FontWeight.Bold, color = DarkGreen)
        Spacer(modifier = Modifier.height(16.dp))

        val textPaint = remember {
            android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 25f
                textAlign = android.graphics.Paint.Align.CENTER
            }
        }

        Box(modifier = Modifier.horizontalScroll(scrollState)) {
            Canvas(
                modifier = Modifier
                    .width(800.dp) //width of chart
                    .height(300.dp) //height of chart
            ) {
                val maxX = 11f
                val maxY = (dataPoints.maxOfOrNull { it.second } ?: 100f) * 1.2f
                val gridColor = Color.LightGray
                val yAxisSteps = 4

                for (i in 0..yAxisSteps) {
                    val y = size.height - (size.height / yAxisSteps * i)
                    drawLine(
                        color = gridColor,
                        start = androidx.compose.ui.geometry.Offset(0f, y),
                        end = androidx.compose.ui.geometry.Offset(size.width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }

                val months = listOf(
                    "Mar",
                    "Apr",
                    "May",
                    "Jun",
                    "Jul",
                    "Aug",
                    "Sep",
                    "Oct",
                    "Nov",
                    "Dec",
                    "Jan",
                    "Feb",
                )
                months.forEachIndexed { index, _ ->
                    val x = (index / maxX) * size.width
                    drawLine(
                        color = gridColor,
                        start = androidx.compose.ui.geometry.Offset(x, 0f),
                        end = androidx.compose.ui.geometry.Offset(x, size.height),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }


                drawIntoCanvas { canvas ->
                    for (i in 0..yAxisSteps) {
                        val value = maxY / yAxisSteps * i
                        val y = size.height - (value / maxY * size.height)
                        canvas.nativeCanvas.drawText(
                            String.format("%.0f", value),
                            -15f, // Position text to the left of the chart
                            y + textPaint.textSize / 2,
                            textPaint.apply { textAlign = android.graphics.Paint.Align.RIGHT }
                        )
                    }
                }

                drawIntoCanvas { canvas ->
                    months.forEachIndexed { index, month ->
                        val x = (index / maxX) * size.width
                        canvas.nativeCanvas.drawText(
                            month,
                            x,
                            size.height + 40f, // Position text below the chart
                            textPaint.apply { textAlign = android.graphics.Paint.Align.CENTER }
                        )
                    }
                }

                val path = Path()

                dataPoints.forEachIndexed { index, point ->
                    val x = (point.first / maxX) * size.width
                    val y = size.height - ((point.second / maxY) * size.height)

                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    drawCircle(
                        color = lineColor,
                        radius = 8f,
                        center = androidx.compose.ui.geometry.Offset(x, y)
                    )
                }
                drawPath(
                    path = path,
                    color = lineColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f)
                )
                drawLine(
                    color = Color.Gray,
                    start = androidx.compose.ui.geometry.Offset(0f, size.height),
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                    strokeWidth = 2f
                )
            }
        }
    }
}

/*
 * This function is designed to present data in tabular formate with a title and column headers.
 * It is used on the landing page to show Top Debtors and Popular Stock.
 * If the data list is empty, it shows a "No data available" message.
 **/
    @Composable
    fun DashboardTable(title: String, headers: List<String>, data: List<List<String>>) {
        Card( // Card holding table data
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkGreen)
                Spacer(modifier = Modifier.height(8.dp))


                Row(Modifier.background(LightGreen.copy(alpha = 0.3f))) {// Table Headers
                    headers.forEach { header ->
                        Text(
                            header, Modifier
                                .weight(1f)
                                .padding(8.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp
                        )
                    }
                }

                if (data.isEmpty()) {// Data Rows
                    Text(
                        "No data available",
                        modifier = Modifier.padding(8.dp),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                } else {
                    data.forEach { row ->
                        Row {
                            row.forEach { cell ->
                                Text(
                                    cell, Modifier
                                        .weight(1f)
                                        .padding(8.dp), fontSize = 12.sp
                                )
                            }
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                    }
                }
            }
        }
    }


/*This function creates the navigation bar and its contents- Stock, Invoices, Debtors
* A NavHost is used to define the navigation routes to each composable screens.
* This setup allows for navigating between various features of the app,
* such as stock and debtor maintenance, and invoice creation.
*/
    @Composable
    fun MainApp() {
        val navController = rememberNavController() // navigation controller
        val context = LocalContext.current // context for database

        val app = context.applicationContext as StellarStocksApplication // application context
        app.database
        val repository = app.repository // repository for database


        val debtorViewModel: DebtorViewModel =
            viewModel(factory = DebtorViewModelFactory(repository)) // view model for debtor
        val stockViewModel: StockViewModel =
            viewModel(factory = StockViewModelFactory(repository)) // view model for stock

        val navItems = listOf( // bottom navigation bar items
            BottomNavItem("Stocks", Icons.Default.Inventory, Screen.StockMenu.route),
            BottomNavItem("Invoices", Icons.Default.Description, Screen.Invoice.route),
            BottomNavItem("Debtors", Icons.Default.People, Screen.DebtorMenu.route)
        )

        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = DarkGreen
                ) { // bottom navigation bar
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
                            colors = NavigationBarItemColors(
                                DarkGreen, Yellow, LimeGreen,
                                Yellow, Yellow, Black, Black
                            ),
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost( // navigation host sets routes for redirecting user to different screens
                navController = navController,
                startDestination = Screen.Invoice.route,
                modifier = Modifier.padding(innerPadding)
            ) {
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


/* This screen is the main screen of the application after the user swipes up on the landing page
* This composable is used to create business invoices that will influence all other data in the application.
*  It allows the user to:
 * - Select a debtor for the invoice.
 * - Add stock items to the invoice.
 * - Specify the quantity and discount for each item.
 * - View a running total of the invoice, including subtotal, VAT, and grand total.
 * - Edit or remove items already added to the invoice.
 * - Confirm and process the final invoice.
 */
    @Composable
    fun InvoiceScreen(
        stockViewModel: StockViewModel = viewModel(factory = StockViewModelFactory(
            (LocalContext.current.applicationContext as StellarStocksApplication).repository)),
        debtorViewModel: DebtorViewModel = viewModel(factory = DebtorViewModelFactory(
            (LocalContext.current.applicationContext as StellarStocksApplication).repository)),
        invoiceViewModel: InvoiceViewModel = viewModel(factory = InvoiceViewModelFactory(
            (LocalContext.current.applicationContext as StellarStocksApplication).repository))
    ) {
        var showStockSearchDialog by remember { mutableStateOf(false) } // stock search dialog state
        var showDebtorSearchDialog by remember { mutableStateOf(false) } // debtor search dialog state
        var showQtyDialog by remember { mutableStateOf(false) } // quantity dialog state

        var tempSelectedStock by remember { mutableStateOf<com.example.stellarstocks.data.db.models.StockMaster?>(null) } // temporary selected stock state for invoice preview

        var editingInvoiceItem by remember { mutableStateOf<InvoiceItem?>(null) }

        var showConfirmationDialog by remember { mutableStateOf(false) } // invoice confirmation dialog state
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
        val invoiceNum by invoiceViewModel.invoiceNum.collectAsState() // invoice number

        val currentInvoiceItem = invoiceItems.find { it.stock.stockCode == tempSelectedStock?.stockCode }
        val qtyInInvoice = currentInvoiceItem?.qty ?: 0

        val isInvoiceProcessed by invoiceViewModel.isInvoiceProcessed.collectAsState() // invoice processed state

        LaunchedEffect(toastMessage) { // show toast message
            toastMessage?.let {
                android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
                invoiceViewModel.clearToast()
            }
        }

        if (showConfirmationDialog) { // show dialog to confirm invoice
            AlertDialog(
                onDismissRequest = { showConfirmationDialog = false },
                title = { Text(text = "Confirm Invoice") },
                text = {
                    Text(
                        "Are you sure you want to process this invoice?\n\nTotal: R${
                            String.format("%.2f", grandTotal)}"
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            invoiceViewModel.confirmInvoice() //call confirm invoice function
                            showConfirmationDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                    ) { Text("Yes, Process") }
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

        if (showDebtorSearchDialog) { //show search debtor dialog
            DebtorCreationSearchDialog(
                viewModel = debtorViewModel,
                onDismiss = {
                    showDebtorSearchDialog = false
                    debtorViewModel.resetSearch() // reset search
                },
                onDebtorSelected = { debtor ->
                    invoiceViewModel.setDebtor(debtor) // set selected debtor to invoice
                    showDebtorSearchDialog = false
                    debtorViewModel.resetSearch()
                }
            )
        }

        if (showStockSearchDialog) { //show search stock dialog
            StockSearchDialog(
                viewModel = stockViewModel,
                onDismiss = {
                    showStockSearchDialog = false
                    stockViewModel.resetSearch()
                },
                onStockSelected = { stock ->
                    tempSelectedStock = stock // set stock added to invoice
                    isEditMode = false //
                    editInitialQty = 1
                    editInitialDiscount = 0.00
                    showStockSearchDialog = false
                    showQtyDialog = true
                    stockViewModel.resetSearch()
                }
            )
        }

        if (showQtyDialog && tempSelectedStock != null) { //show quantity dialog after adding an item to invoice
            val totalQtyInInvoice = invoiceItems // total quantity in invoice currently
                .filter { it.stock.stockCode == tempSelectedStock!!.stockCode }
                .sumOf { it.qty } // sum of all quantities relating to the filtered stock code in invoice

            val qtyForDialogCalc = if (isEditMode && editingInvoiceItem != null) { // calculate quantity for dialog if editing an item
                totalQtyInInvoice - editingInvoiceItem!!.qty
            } else {
                totalQtyInInvoice
            }

            AddStockDialog(
                stock = tempSelectedStock!!, // stock added to invoice cannot be null
                existingQtyInInvoice = qtyForDialogCalc,
                initialQty = editInitialQty,
                initialDiscount = editInitialDiscount,
                isEditMode = isEditMode,
                onDismiss = { showQtyDialog = false }, //dismiss dialog if user cancels
                onConfirm = { qty, discount ->
                    if (isEditMode && editingInvoiceItem != null) { // if in edit mode and item is not null

                        invoiceViewModel.updateInvoiceItem(editingInvoiceItem!!, qty, discount) // update relevant item fields in invoice
                    } else {

                        invoiceViewModel.addToInvoice(tempSelectedStock!!, qty, discount) // add item to invoice
                    }
                    showQtyDialog = false
                    tempSelectedStock = null
                    editingInvoiceItem = null
                }
            )
        }
        Box( // background wave image
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val wavePath =
                    createWavePath(
                        width = size.width,
                        height = size.height * 0.25f,
                        waveHeight = 80f
                    )
                drawPath(path = wavePath, color = Yellow.copy(alpha = 0.5f))
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
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button( // button to select debtor
                        onClick = { showDebtorSearchDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = LightGreen),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        shape = MaterialTheme.shapes.small,
                        enabled = !isInvoiceProcessed
                    ) {
                        Icon(Icons.Default.People, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Select Debtor")
                    }

                    Button( // button to add stock
                        onClick = { showStockSearchDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = LightGreen),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        shape = MaterialTheme.shapes.small,
                        enabled = !isInvoiceProcessed
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
                            Text(//invoice title
                                "INVOICE #${invoiceNum}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                color = Black
                            )
                            Text(// date of invoice
                                LocalDate.now().toString(),
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Subtotal excl. VAT:",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = Black
                                )
                                Text(// subtotal excl vat rounded to 2 decimal places
                                    String.format("R%.2f", totalExVat),
                                    fontSize = 12.sp,
                                    color = Black
                                )
                            }

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "VAT (15%):",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = Black
                                )
                                Text(//vat total rounded to 2 decimal places
                                    String.format("R%.2f", vat),
                                    fontSize = 12.sp,
                                    color = Black
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "TOTAL:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = DarkGreen
                                )
                                Text(// final total rounded to 2 decimal places
                                    String.format("R%.2f", grandTotal),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = DarkGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))

                            // Debtor Section
                            if (selectedDebtor != null) { // show debtor details if selected
                                Text(
                                    "BILL TO:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(// debtor name
                                    selectedDebtor!!.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = DarkGreen
                                )
                                Text(// debtor account code
                                    selectedDebtor!!.accountCode,
                                    fontSize = 14.sp,
                                    color = Black
                                )

                                Text(
                                    "Primary Address:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                                Text(// debtor primary address
                                    selectedDebtor!!.address1.replace(", ", "\n"),
                                    fontSize = 12.sp,
                                    color = Black
                                )

                                if (selectedDebtor!!.address2.isNotBlank()) {
                                    Text(
                                        "Secondary Address:",
                                        modifier = Modifier.fillMaxWidth(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray,
                                        textAlign = TextAlign.End
                                    )
                                    Text(// debtor secondary address if applicable
                                        selectedDebtor!!.address2.replace(", ", "\n"),
                                        modifier = Modifier.fillMaxWidth(),
                                        fontSize = 12.sp,
                                        color = Black,
                                        textAlign = TextAlign.End
                                    )
                                }
                            } else {
                                Text(
                                    "No Debtor Selected",
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            // Invoice Details separator
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xff000000))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Item (Tap to Edit Added Items)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(//per line item total
                                    "Total (Excl VAT)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }


                            if (invoiceItems.isEmpty()) {// Invoice Items List
                                Text("Invoice is empty", modifier = Modifier.padding(vertical = 20.dp).fillMaxWidth(), textAlign = TextAlign.Center, color = Color.Gray)
                            } else {
                                invoiceItems.forEach { item -> // loop through invoice items
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = !isInvoiceProcessed) { // enable click only if invoice is not processed

                                                tempSelectedStock = item.stock
                                                editingInvoiceItem = item // Capture the specific item
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
                                            Text(item.stock.stockDescription, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkGreen)
                                            Row {
                                                Text("${item.qty} x R${item.stock.sellingPrice}", fontSize = 12.sp, color = Color.Gray) // item quantity and price
                                                if (item.discountPercent > 0) { // if item has discount
                                                    Spacer(Modifier.width(8.dp))
                                                    Text("(-${item.discountPercent}%)", fontSize = 12.sp, color = Red)
                                                }
                                            }
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(String.format("R%.2f", item.lineTotal), fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp), color = Black)

                                            if (!isInvoiceProcessed) { // if invoice is not processed, allow user to remove item from invoice items
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Remove",
                                                    tint = Color.Red,
                                                    modifier = Modifier.size(20.dp).clickable { invoiceViewModel.removeFromInvoice(item) }
                                                )
                                            }
                                        }
                                    }
                                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                                }
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))


                if (isInvoiceProcessed) { // if invoice is processed, show new invoice button

                    Button( // button to start a new invoice
                        onClick = { invoiceViewModel.startNewInvoice() },
                        colors = ButtonDefaults.buttonColors(containerColor = ProfessionalLightBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("Start New Invoice", fontWeight = FontWeight.Bold, color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(8.dp))


                    Button( // button to visually show invoice is processed
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = false
                    ) {
                        Text("Invoice Processed", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {

                    Button( // button to confirm invoice
                        onClick = { showConfirmationDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = selectedDebtor != null && invoiceItems.isNotEmpty()
                    ) {
                        Text("CONFIRM & PROCESS INVOICE", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

/*
* This dialog composable allows for searching of all stock using a search that
* constantly updates the content displayed in real time.
* This dialog displays details of the selected stock item, including its description, price, and stock on hand.
* It provides input fields for specifying the quantity and discount percentage.
* It includes input validation to ensure that quantity is a digit and the discount is a decimal number
* and both are positive.
* The dialog operates in two modes, controlled by the isEditMode parameter:
* - Add Mode: Enables adding items to invoice.
* - Edit Mode: The input fields are pre-filled with the existing item's data. Allows updating the quantity and discount.
*/
@Composable
fun AddStockDialog(
    stock: com.example.stellarstocks.data.db.models.StockMaster, // stock to be added to invoice
    existingQtyInInvoice: Int = 0, // to hold existing quantity in invoice
    initialQty: Int = 0, // initial quantity for dialog
    initialDiscount: Double = 0.0, // initial discount for dialog
    isEditMode: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (Int, Double) -> Unit
) {
    var qtyState by remember {
        mutableStateOf(
            TextFieldValue(
                text = if (initialQty > 0) initialQty.toString() else "1", // initial quantity
                selection = TextRange(if (initialQty > 0) initialQty.toString().length else 1)
            )
        )
    }

    var discountState by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialDiscount.toString(),
                selection = TextRange(initialDiscount.toString().length)
            )
        )
    }

    val maxAllowed = if (isEditMode) stock.stockOnHand else (stock.stockOnHand - existingQtyInInvoice) // calculate max allowed quantity in stock
    val availableDisplay = if (maxAllowed < 0) 0 else maxAllowed // calculate available stock quantity

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isEditMode) "Edit Item" else "Add Item",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(stock.stockDescription, fontWeight = FontWeight.Medium, color = DarkGreen)

                Spacer(Modifier.height(8.dp))

                // Stock Info Display
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFF0F0F0), shape = MaterialTheme.shapes.small).padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("On Hand", fontSize = 10.sp, color = Color.Gray)
                        Text("${stock.stockOnHand}", fontWeight = FontWeight.Bold)
                    }
                    if (!isEditMode && existingQtyInInvoice > 0) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("In Invoice", fontSize = 10.sp, color = Color.Gray)
                            Text("$existingQtyInInvoice", fontWeight = FontWeight.Bold, color = Orange)
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if(isEditMode) "Max Qty" else "Remaining", fontSize = 10.sp, color = Color.Gray)
                        Text("$availableDisplay", fontWeight = FontWeight.Bold, color = if(availableDisplay == 0) Red else DarkGreen)
                    }
                }

                Text("Price: R${stock.sellingPrice}", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = qtyState,
                    onValueChange = { input ->
                        if (input.text.all { it.isDigit() }) {
                            qtyState = input
                        }
                    },
                    label = { Text("Quantity") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            // Auto-highlight logic
                            if (focusState.isFocused) {
                                val text = qtyState.text
                                qtyState = qtyState.copy(selection = TextRange(0, text.length))
                            }
                        },
                    isError = (qtyState.text.toIntOrNull() ?: 0) > maxAllowed
                )

                if ((qtyState.text.toIntOrNull() ?: 0) > maxAllowed) {
                    Text("Max available is $availableDisplay", color = Red, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = discountState,
                    onValueChange = { input ->
                        if (input.text.count { it == '.' } <= 1 && input.text.all { it.isDigit() || it == '.' }) {
                            discountState = input
                        }
                    },
                    label = { Text("Discount %") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            // Auto-highlight logic
                            if (focusState.isFocused) {
                                val text = discountState.text
                                discountState = discountState.copy(selection = TextRange(0, text.length))
                            }
                        }
                )

                Spacer(Modifier.height(24.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Red)) { Text("Cancel") }
                    Button(
                        onClick = {
                            val qty = qtyState.text.toIntOrNull() ?: 0
                            val disc = discountState.text.toDoubleOrNull() ?: 0.0

                            if (qty > 0 && qty <= maxAllowed && disc >= 0) {
                                onConfirm(qty, disc)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                        enabled = (qtyState.text.toIntOrNull() ?: 0) <= maxAllowed
                    ) { Text(if (isEditMode) "Update" else "Add") }
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

/*
* A data class that represents the information required to display a single item
* in the debtor menu. Each instance holds the data for one tile on the DebtorMenuScreen.
*/
    @Composable
    fun DebtorMenuTile(
        item: DebtorMenuItemData,
        navController: NavController
    ) { // debtor menu tile layouts
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

                Text(
                    item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    item.description,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
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

/*
* A data class that represents the information required to display a single item
* in the stock menu. Each instance holds the data for one tile on the StockMenuScreen.
*/
    @Composable
    fun StockMenuTile(
        item: StockMenuItemData,
        navController: NavController
    ) { // stock menu tile layouts
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

                Text(
                    item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    item.description,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }


/*
* This function creates a Card that acts as a clickable tile. When clicked,
 * it uses the navController to navigate to the route specified
*/
    @Composable
    fun StockMenuScreen(navController: NavController) { // stock menu screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val wavePath =
                    createWavePath(
                        width = size.width,
                        height = size.height * 0.25f,
                        waveHeight = 80f
                    )
                drawPath(path = wavePath, color = Yellow.copy(alpha = 0.5f))
            }
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
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                LazyVerticalGrid( // grid layout for stock menu tiles
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(stockMenuItems) { item -> // loop through stock menu tiles
                        StockMenuTile(item = item, navController = navController)
                    }
                }
            }
        }
    }

/*
* This function creates a Card that acts as a clickable tile. When clicked,
* it uses the navController to navigate to the route specified.
*/
    @Composable
    fun DebtorMenuScreen(navController: NavController) { // debtor menu screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val wavePath =
                    createWavePath(
                        width = size.width,
                        height = size.height * 0.25f,
                        waveHeight = 80f
                    )
                drawPath(path = wavePath, color = Yellow.copy(alpha = 0.5f))
            }
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
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                LazyVerticalGrid( // grid layout for debtor menu tiles
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(debtorMenuItems) { item -> // loop through debtor menu tiles
                        DebtorMenuTile(item = item, navController = navController)
                    }
                }
            }
        }
    }

/*
 * This screen allows users to:
 * - See a list of all debtors.
 * - Search for specific debtors by their account code or name via a search bar.
 * - Sort the list of debtors based on different criteria
 * - Navigate back to the previous screen.
 * - Tap on a debtor in the list to navigate to their details screen.
*/
    @Composable
    fun DebtorEnquiryScreen(
        debtorViewModel: DebtorViewModel,
        navController: NavController
    ) { // debtor enquiry screen
        val debtorList by debtorViewModel.filteredDebtors.collectAsState() // list of debtors
        val searchQuery by debtorViewModel.searchQuery.collectAsState() // search query to filter debtors

        val currentSort by debtorViewModel.debtorListSort.collectAsState()
        var showSortMenu by remember { mutableStateOf(false) }

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
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { debtorViewModel.onSearchQueryChange("") }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = Black
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Button(
                        onClick = { showSortMenu = true },
                        colors = ButtonDefaults.buttonColors(containerColor = LightGreen),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Sort By: ${getDebtorListSortLabel(currentSort)}",
                            color = Color.Black
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Sort",
                            tint = Color.Black
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Code: Ascending") },
                            onClick = {
                                debtorViewModel.updateDebtorListSort(DebtorListSortOption.CODE_ASC)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Code: Descending") },
                            onClick = {
                                debtorViewModel.updateDebtorListSort(DebtorListSortOption.CODE_DESC)
                                showSortMenu = false
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Lowest Balance") },
                            onClick = {
                                debtorViewModel.updateDebtorListSort(DebtorListSortOption.BALANCE_ASC)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Highest Balance") },
                            onClick = {
                                debtorViewModel.updateDebtorListSort(DebtorListSortOption.BALANCE_DESC)
                                showSortMenu = false
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max)
                        .border(1.dp, Color.Gray)
                ) { // header row for table
                    TableCell(text = "Code", weight = .25f, isHeader = true)
                    TableCell(text = "Name", weight = .5f, isHeader = true)
                    TableCell(text = "Balance", weight = .25f, isHeader = true)
                }

                if (debtorList.isEmpty()) { // if no debtors found, display message
                    Box(
                        modifier = Modifier
                            .weight(1f)
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
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Max)
                                    .clickable {
                                        navController.navigate(
                                            Screen.DebtorDetails.createRoute(
                                                debtor.accountCode
                                            )
                                        )
                                    }
                            ) {
                                TableCell(text = debtor.accountCode, weight = .25f)
                                TableCell(text = debtor.name, weight = .5f)
                                TableCell(
                                    text = String.format("R%.2f", debtor.balance),
                                    weight = .25f
                                )
                            }
                        }
                    }
                }
            }
        }

/*
* This function helps determine the type of sort description to be displayed in the sort menu.
*/
    fun getDebtorListSortLabel(option: DebtorListSortOption): String {
        return when (option) {
            DebtorListSortOption.CODE_ASC -> "Code: Asc"
            DebtorListSortOption.CODE_DESC -> "Code: Desc"
            DebtorListSortOption.BALANCE_ASC -> "Lowest Balance"
            DebtorListSortOption.BALANCE_DESC -> "Highest Balance"
        }
    }

/*
 * This screen allows users to:
 * - See a list of all stock.
 * - Search for specific stock by its stock code or description via a search bar.
 * - Sort the list of stock based on different criteria
 * - Navigate back to the previous screen.
 * - Tap on a stock in the list to navigate to its details screen.
*/
    @Composable
    fun StockEnquiryScreen(stockViewModel: StockViewModel, navController: NavController) {
        val stockList by stockViewModel.filteredStock.collectAsState()
        val searchQuery by stockViewModel.searchQuery.collectAsState()
        val currentSort by stockViewModel.stockListSort.collectAsState()
        var showSortMenu by remember { mutableStateOf(false) }

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
                    Text(
                        "Stock Enquiry",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen
                    )
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { stockViewModel.onSearchQueryChange(it) },
                    label = { Text("Search Stock Code or Name") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { stockViewModel.onSearchQueryChange("") }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = Black
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Button(
                        onClick = { showSortMenu = true },
                        colors = ButtonDefaults.buttonColors(containerColor = LightGreen),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Sort By: ${getStockListSortLabel(currentSort)}",
                            color = Color.Black
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Sort",
                            tint = Color.Black
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Code: Ascending") },
                            onClick = {
                                stockViewModel.updateStockListSort(StockListSortOption.CODE_ASC)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Code: Descending") },
                            onClick = {
                                stockViewModel.updateStockListSort(StockListSortOption.CODE_DESC)
                                showSortMenu = false
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Highest Qty") },
                            onClick = {
                                stockViewModel.updateStockListSort(StockListSortOption.QTY_DESC)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Lowest Qty") },
                            onClick = {
                                stockViewModel.updateStockListSort(StockListSortOption.QTY_ASC)
                                showSortMenu = false
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Highest Cost") },
                            onClick = {
                                stockViewModel.updateStockListSort(StockListSortOption.COST_DESC)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Lowest Cost") },
                            onClick = {
                                stockViewModel.updateStockListSort(StockListSortOption.COST_ASC)
                                showSortMenu = false
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max)// ensures header cells are uniform height if titles wrap
                        .border(1.dp, Color.Gray)
                ) {
                    TableCell(text = "Code", weight = 0.2f, isHeader = true)
                    TableCell(text = "Description", weight = 0.4f, isHeader = true)
                    TableCell(text = "Qty", weight = 0.15f, isHeader = true)
                    TableCell(text = "Unit Cost", weight = 0.25f, isHeader = true)
                }

                if (stockList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No stock found matching '$searchQuery'", color = Color.Gray)
                    }
                } else {
                    LazyColumn {
                        items(stockList) { stock ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Max)
                                    .clickable {
                                        navController.navigate(Screen.StockDetails.createRoute(stock.stockCode))
                                    }
                            ) {
                                TableCell(text = stock.stockCode, weight = 0.2f) // stock codes
                                TableCell(
                                    text = stock.stockDescription,
                                    weight = 0.4f
                                ) // stock descriptions
                                TableCell(
                                    text = stock.stockOnHand.toString(),
                                    weight = 0.15f
                                ) // stock on hand
                                TableCell(
                                    text = String.format("R%.2f", stock.cost),
                                    weight = 0.25f
                                ) // stock cost
                            }
                        }
                    }
                }
            }
    }

/*
* This function helps determine the type of sort description to be displayed in the sort menu.
*/
    fun getStockListSortLabel(option: StockListSortOption): String {
        return when (option) {
            StockListSortOption.CODE_ASC -> "Code: Asc"
            StockListSortOption.CODE_DESC -> "Code: Desc"
            StockListSortOption.QTY_DESC -> "Highest Qty"
            StockListSortOption.QTY_ASC -> "Lowest Qty"
            StockListSortOption.COST_DESC -> "Highest Cost"
            StockListSortOption.COST_ASC -> "Lowest Cost"
        }
    }

/*
* This function creates a Card that acts as a clickable tile. When clicked,
 * it uses the navController to navigate to the route specified.
*/
@Composable
fun RowScope.TableCell( // table cell layout
    text: String,
    weight: Float,
    isHeader: Boolean = false
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight() // Stretches to fill the Intrinsic Height of the Row
            .border(0.5.dp, Color.LightGray) // Creates the grid line effect
            .padding(8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (isHeader) 16.sp else 14.sp
        )
    }
}
