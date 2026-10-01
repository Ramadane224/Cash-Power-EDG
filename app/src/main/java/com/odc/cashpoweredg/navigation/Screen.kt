package com.odc.cashpoweredg.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Tableau", Icons.Default.Dashboard)
    data object Form : Screen("form", "Saisie", Icons.Default.AddCircle)
    data object History : Screen("history", "Historique", Icons.Default.History)
    data object Settings : Screen("settings", "Paramètres", Icons.Default.Settings)

    companion object {
        val destinations = listOf(Dashboard, Form, History, Settings)
    }
}
