package com.example.stellarstocks.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object StockMenu : Screen("stock_menu")
    object Invoice : Screen("invoice")

    // Debtors
    object DebtorMenu : Screen("debtor_menu")
    object DebtorEnquiry : Screen("debtor_enquiry")
    object DebtorCreation : Screen("debtor_creation")

    // Details Screen
    object DebtorDetails : Screen("debtor_details/{accountCode}") {
        fun createRoute(accountCode: String) = "debtor_details/$accountCode"
    }

    // Stocks
    object StockEnquiry : Screen("stock_enquiry")
    object StockCreation : Screen("stock_creation")
    object StockAdjustment : Screen("stock_adjustment")
    object StockEdit : Screen("stock_edit")

    object StockDetails : Screen("stock_details/{stockCode}") {
        fun createRoute(stockCode: String) = "stock_details/$stockCode"
    }
}