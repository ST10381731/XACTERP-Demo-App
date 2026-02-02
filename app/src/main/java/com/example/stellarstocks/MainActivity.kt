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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

fun createWavePath(
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

    VerticalPager(
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
fun LandingPage() {
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
    val navController = rememberNavController()
    val context = LocalContext.current

    val app = context.applicationContext as StellarStocksApplication
    app.database
    val repository = app.repository


    val debtorViewModel: DebtorViewModel = viewModel(factory = DebtorViewModelFactory(repository))
    val stockViewModel: StockViewModel = viewModel(factory = StockViewModelFactory(repository))

    val navItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, Screen.Home.route),
        BottomNavItem("Stocks", Icons.Default.Inventory, Screen.StockMenu.route),
        BottomNavItem("Invoices", Icons.Default.Description, Screen.Invoice.route),
        BottomNavItem("Debtors", Icons.Default.People, Screen.DebtorMenu.route)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
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
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }

            composable(Screen.Invoice.route) { InvoiceScreen(stockViewModel, debtorViewModel) }

            composable(Screen.StockMenu.route) { StockMenuScreen(navController) }

            composable(Screen.StockEnquiry.route) {
                StockEnquiryScreen(stockViewModel, navController)
            }

            composable(Screen.StockDetails.route) { backStackEntry ->
                val stockCode = backStackEntry.arguments?.getString("stockCode")
                if (stockCode != null) {
                    StockDetailsScreen(stockCode, stockViewModel, navController)
                }
            }

            composable(Screen.StockCreation.route) {
                StockCreationScreen(stockViewModel, navController)
            }

            composable(Screen.StockAdjustment.route) {
                StockAdjustmentScreen(viewModel = stockViewModel, navController = navController)
            }

            composable(Screen.DebtorMenu.route) { DebtorMenuScreen(navController) }

            composable(Screen.DebtorEnquiry.route) {
                DebtorEnquiryScreen(debtorViewModel, navController)
            }

            composable(Screen.DebtorCreation.route) {
                DebtorCreationScreen(debtorViewModel, navController)
            }

            composable(Screen.DebtorDetails.route) { backStackEntry ->
                val accountCode = backStackEntry.arguments?.getString("accountCode")
                if (accountCode != null) {
                    DebtorDetailsScreen(accountCode, debtorViewModel, navController)
                }
            }
        }
    }
}

@Composable
fun HomeScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Home Screen. Need to research graphing")
    }
}

@Composable
fun InvoiceScreen(stockViewModel: StockViewModel, debtorViewModel: DebtorViewModel) {
    var showStockSearchDialog by remember { mutableStateOf(false) }
    var showDebtorSearchDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val selectedDebtor by debtorViewModel.selectedDebtor.collectAsState()
    val selectedStock  by stockViewModel.selectedStock.collectAsState()

    if (showStockSearchDialog) { //stock pop-out search
        StockSearchDialog(
            viewModel = stockViewModel,
            onDismiss = { showStockSearchDialog = false },
            onStockSelected = {
                stockViewModel.onStockSelectedForAdjustment(it)
                showStockSearchDialog = false
            }
        )
    }

    if (showDebtorSearchDialog) { //debtor pop-out search
        DebtorCreationSearchDialog(
            viewModel = debtorViewModel,
            onDismiss = { showDebtorSearchDialog = false },
            onDebtorSelected = { debtor ->
                debtorViewModel.selectDebtorForDetails(debtor.accountCode)
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
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { showDebtorSearchDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = LightGreen),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.height(56.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search Debtor")
            }

            Button(
                onClick = { showStockSearchDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = LightGreen),
                modifier = Modifier.height(56.dp),
                shape = MaterialTheme.shapes.extraSmall
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search Stock")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        TicketView(
            content = {
                Column {
                    Text(
                        "Invoice",
                        fontWeight = FontWeight.Bold,
                        color = Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Date: " + LocalDate.now().toString(),
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Invoice Number: ",
                        fontWeight = FontWeight.Bold,
                        color = Black,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (selectedDebtor != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Debtor: ${selectedDebtor!!.name}",
                            fontWeight = FontWeight.Bold,
                            color = DarkGreen,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            "Address 1:",
                            color = Black,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            selectedDebtor!!.address1,
                            color = Black,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            "Address 2(optional):",
                            color = Black,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            selectedDebtor!!.address2,
                            color = Black,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Total Cost Excl. VAT: ", fontWeight = FontWeight.Bold, color = Black)
                        Text("VAT: ", fontWeight = FontWeight.Bold, color = Black)
                        HorizontalDivider()
                        Text("Total Cost: ", fontWeight = FontWeight.Bold, color = DarkGreen)
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(20.dp))
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No debtor selected", color = Black)
                    }
                    if (selectedStock != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Stock: ${selectedStock!!.stockCode}", fontWeight = FontWeight.Bold, color = DarkGreen)
                        Text(
                            "Description: ${selectedStock!!.stockDescription}",
                            fontWeight = FontWeight.Bold,
                            color = DarkGreen,
                            textAlign = TextAlign.End
                        )
                        Text(
                            "Qty: ${selectedStock!!.stockOnHand}",
                            fontWeight = FontWeight.Bold,
                            color = DarkGreen,
                            textAlign = TextAlign.End
                        )
                        Text(
                            "Cost: R${selectedStock!!.cost}",
                            fontWeight = FontWeight.Bold,
                            color = DarkGreen,
                            textAlign = TextAlign.End
                        )


                }else {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No stock selected", color = Black)

                    }

                }
            }
        )
    }
}

@Preview
@Composable
private fun TicketView(
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

data class DebtorMenuItemData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String
)

val debtorMenuItems = listOf(
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
fun DebtorMenuTile(item: DebtorMenuItemData, navController: NavController) {
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


data class StockMenuItemData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String
)

val stockMenuItems = listOf(
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
fun StockMenuTile(item: StockMenuItemData, navController: NavController) {
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
fun StockMenuScreen(navController: NavController) {
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
fun DebtorMenuScreen(navController: NavController) {
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
fun DebtorEnquiryScreen(debtorViewModel: DebtorViewModel, navController: NavController) {
    val debtorList by debtorViewModel.filteredDebtors.collectAsState()
    val searchQuery by debtorViewModel.searchQuery.collectAsState()

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
                "Debtor Enquiry",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen
            )
        }
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { debtorViewModel.onSearchQueryChange(it) },
            label = { Text("Search Account Code or Name") },
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

        if (debtorList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No debtors found matching '$searchQuery'", color = Color.Gray)
            }
        } else {
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
                        TableCell(text = "R${debtor.balance}", weight = .25f)
                    }
                }
            }
        }
    }
}

@Composable
fun StockEnquiryScreen(stockViewModel: StockViewModel, navController: NavController) {
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

        if (stockList.isEmpty()) {
            Box(
                modifier = Modifier
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
                            .clickable {
                                navController.navigate(Screen.StockDetails.createRoute(stock.stockCode))
                            }
                    ) {
                        TableCell(text = stock.stockCode, weight = .4f)
                        TableCell(text = stock.stockDescription, weight = .6f)
                        TableCell(text = stock.stockOnHand.toString(), weight = .4f)
                        TableCell(text = stock.cost.toString(), weight = .4f)
                    }
                }
            }
        }
    }
}
