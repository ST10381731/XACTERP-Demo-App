package com.example.stellarstocks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.stellarstocks.data.db.StellarStocksDatabase
import com.example.stellarstocks.data.db.models.DebtorMaster
import com.example.stellarstocks.data.db.models.StockMaster
import com.example.stellarstocks.data.db.repository.StellarStocksRepositoryImpl
import com.example.stellarstocks.ui.navigation.Screen
import com.example.stellarstocks.ui.theme.DarkGreen
import com.example.stellarstocks.ui.theme.LightGreen
import com.example.stellarstocks.ui.theme.StellarStocksTheme
import com.example.stellarstocks.ui.theme.Yellow
import com.example.stellarstocks.viewmodel.DebtorViewModel
import com.example.stellarstocks.viewmodel.DebtorViewModelFactory
import com.example.stellarstocks.viewmodel.StockViewModel

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


fun createWavePath(width: Float, height: Float, waveHeight: Float): Path { //function draws a wave from top left to right
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


        Canvas(modifier = Modifier.fillMaxSize()) {// overlaps the top part of the yellow wave
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
                text = "Name+Logo",
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
                text = "swipe to continue",
                color = DarkGreen,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun MainApp() {
    val navController = androidx.navigation.compose.rememberNavController()
    val context = LocalContext.current
    val db = StellarStocksDatabase.getDatabase(context)
    val repository = StellarStocksRepositoryImpl(db.debtorDao(),db.stockDao(), db.invoiceHeaderDao(), db.invoiceDetailDao())
    val debtorViewModel: DebtorViewModel = viewModel(factory = DebtorViewModelFactory(repository))
    val stockViewModel: StockViewModel = viewModel()

    val navItems = listOf(  // List of items for the navigation bar
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
        // all destinations
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.StockMenu.route) { StockMenuScreen(navController) }
            composable(Screen.Invoice.route) { InvoiceScreen() }
            composable(Screen.DebtorMenu.route) { DebtorMenuScreen(navController) }
            composable(Screen.DebtorEnquiry.route) { DebtorEnquiryScreen(debtorViewModel) }
            composable(Screen.DebtorCreation.route) { DebtorCreationScreen() }
            composable(Screen.DebtorEdit.route) { DebtorEditScreen() }
            composable(Screen.StockCreation.route) { StockCreationScreen() }
            composable(Screen.StockEnquiry.route) { StockEnquiryScreen(stockViewModel) }
            composable(Screen.StockAdjustment.route) { StockAdjustmentScreen() }
            composable(Screen.StockEdit.route) { StockCreationScreen() }

        }
    }
}

@Composable
fun HomeScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Home Screen. Need to link stats from tables and print them here")
    }
}


@Composable
fun InvoiceScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Invoicing Menu")
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
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = LightGreen
            )
        ) {
            Text("Stock Enquiry")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate(Screen.StockCreation.route) },
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = LightGreen
            )
        ) {
            Text("Stock Maintenance")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate(Screen.StockAdjustment.route) },
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = LightGreen
            )
        ) {
            Text("Stock Adjustment")
        }
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
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = LightGreen
            )
        ) {
            Text("Debtor Enquiry")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate(Screen.DebtorCreation.route) },
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = LightGreen
            )
        ) {
            Text("Debtor Maintenance")
        }

    }
}

@Composable
fun DebtorEnquiryScreen(debtorViewModel: DebtorViewModel) {
    val debtorList by debtorViewModel.debtors.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth()) {
            TableCell(text = "Account Code", weight = .5f, isHeader = true)
            TableCell(text = "Address", weight = .5f, isHeader = true)
            TableCell(text = "Balance", weight = .5f, isHeader = true)
        }
        if (debtorList.isEmpty()) {
            Text("No Debtors Found")
        }
        LazyColumn {
            items(debtorList) { debtor ->
                DebtorListItem(debtor)
            }
        }
    }
}

@Composable
fun DebtorListItem(debtor: DebtorMaster) {
    Row(Modifier.fillMaxWidth()) {
        TableCell(text = debtor.accountCode, weight = .25f)
        TableCell(text = debtor.address1, weight = .5f)
        TableCell(text = debtor.balance.toString(), weight = .25f)
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
fun DebtorDetailsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Debtor Details Screen")
    }
}


@Composable
fun DebtorEditScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Debtor Edit Screen")
    }
}

@Composable
fun DebtorCreationScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Debtor Creation Screen")
    }
}

@Composable
fun StockEnquiryScreen(stockViewModel: StockViewModel) {
    val stockList by stockViewModel.stock.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth()) {
            TableCell(text = "Stock Code", weight = .25f, isHeader = true)
            TableCell(text = "Description", weight = .5f, isHeader = true)
            TableCell(text = "Stock on hand", weight = .25f, isHeader = true)
            TableCell(text = "Cost", weight = .25f, isHeader = true)
        }
        if (stockList.isEmpty()) {
            Text("No Stock Found")
        }
        LazyColumn {
            items(stockList) { stock ->
                StockList(stock)
            }
        }
    }
}

@Composable
fun StockList(stock: StockMaster) {
    Row(Modifier.fillMaxWidth()) {
        TableCell(text = stock.stockCode, weight = .25f)
        TableCell(text = stock.stockDescription, weight = .5f)
        TableCell(text = stock.stockOnHand.toString(), weight = .25f)
        TableCell(text = stock.cost.toString(), weight = .25f)
    }
}

@Composable
fun StockDetailsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Stock Details Screen")
    }
}

@Composable
fun StockAdjustmentScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Stock Adjustment Screen")
    }
}

@Composable
fun StockCreationScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Stock Creation Screen")
    }
}


data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StellarStocksTheme {
        LandingPage()
    }
}
