package com.odc.cashpoweredg.navigation

sealed class Screen(val route: String, val label: String) {
    data object Dashboard : Screen("dashboard", "Tableau")
    data object Form : Screen("form", "Achat")
    data object History : Screen("history", "Historique")
    data object Settings : Screen("settings", "Paramètres")

    companion object {
        val destinations = listOf(Dashboard, Form, History, Settings)
    }
}