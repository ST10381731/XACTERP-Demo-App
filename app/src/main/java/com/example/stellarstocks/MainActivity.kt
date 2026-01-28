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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.example.stellarstocks.ui.screens.DebtorDetailsScreen
import com.example.stellarstocks.ui.screens.StockAdjustmentScreen
import com.example.stellarstocks.ui.screens.StockCreationScreen
import com.example.stellarstocks.ui.screens.StockDetailsScreen
import com.example.stellarstocks.ui.theme.DarkGreen
import com.example.stellarstocks.ui.theme.LightGreen
import com.example.stellarstocks.ui.theme.StellarStocksTheme
import com.example.stellarstocks.ui.theme.Yellow
import com.example.stellarstocks.viewmodel.DebtorViewModel
import com.example.stellarstocks.viewmodel.DebtorViewModelFactory
import com.example.stellarstocks.viewmodel.StockViewModel
import com.example.stellarstocks.viewmodel.StockViewModelFactory

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
                        label = { Text(item.label) }
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

            composable(Screen.Invoice.route) { InvoiceScreen() }

            composable(Screen.StockMenu.route) { StockMenuScreen(navController) }

            composable(Screen.StockEnquiry.route) {
                StockEnquiryScreen(stockViewModel, navController)
            }

            composable(Screen.StockDetails.route) { backStackEntry ->
                val stockCode = backStackEntry.arguments?.getString("stockCode")
                if (stockCode != null) {
                    StockDetailsScreen(stockCode, stockViewModel)
                }
            }

            composable(Screen.StockCreation.route) {
                StockCreationScreen(stockViewModel)
            }

            composable(Screen.StockAdjustment.route) {
                StockAdjustmentScreen(viewModel = stockViewModel)
            }

            composable(Screen.DebtorMenu.route) { DebtorMenuScreen(navController) }

            composable(Screen.DebtorEnquiry.route) {
                DebtorEnquiryScreen(debtorViewModel, navController)
            }

            composable(Screen.DebtorCreation.route) {
                DebtorCreationScreen(debtorViewModel)
            }

            composable(Screen.DebtorDetails.route) { backStackEntry ->
                val accountCode = backStackEntry.arguments?.getString("accountCode")
                if (accountCode != null) {
                    DebtorDetailsScreen(accountCode, debtorViewModel)
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
fun InvoiceScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Invoicing Menu. Need to research invoicing previews")
    }
}

@Composable
fun StockMenuScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { navController.navigate(Screen.StockEnquiry.route) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = LightGreen)
        ) { Text("Stock Enquiry") }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate(Screen.StockCreation.route) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = LightGreen)
        ) { Text("Stock Maintenance") }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate(Screen.StockAdjustment.route) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = LightGreen)
        ) { Text("Stock Adjustment") }
    }
}

@Composable
fun DebtorMenuScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { navController.navigate(Screen.DebtorEnquiry.route) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = LightGreen)
        ) { Text("Debtor Enquiry") }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate(Screen.DebtorCreation.route) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = LightGreen)
        ) { Text("Debtor Maintenance") }
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
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { debtorViewModel.onSearchQueryChange(it) },
            label = { Text("Search Account Code") },
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
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { stockViewModel.onSearchQueryChange(it) },
            label = { Text("Search Stock Code") },
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

@Composable
fun StockCreationScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Stock Creation Screen")
    }
}

