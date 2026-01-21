package com.example.stellarstocks.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object DebtorList : Screen("debtor_list")
    data object AddDebtor : Screen("add_debtor")
    data object StockList : Screen("stock_list")
    data object AddStock : Screen("add_stock")
    data object Invoice : Screen("invoice")
}