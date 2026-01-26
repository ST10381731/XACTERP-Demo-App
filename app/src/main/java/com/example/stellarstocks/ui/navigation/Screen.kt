package com.example.stellarstocks.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Invoice : Screen("invoice")
    data object DebtorEnquiry: Screen("debtor_enquiry")
    data object DebtorMenu: Screen("settings")
    data object DebtorDetails: Screen ("debtor_details")
    data object DebtorEdit : Screen ("debtor_edit")
    data object DebtorCreation: Screen("debtor_creation")
    data object StockMenu: Screen("stock_menu")
    data object StockEnquiry: Screen("stock_enquiry")
    data object StockDetails: Screen("stock_details")
    data object StockEdit: Screen("stock_edit")
    data object StockCreation: Screen("stock_creation")
    data object StockAdjustment: Screen("stock_adjustment")
}