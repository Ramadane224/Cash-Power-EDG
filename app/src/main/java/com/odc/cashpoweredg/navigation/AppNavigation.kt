package com.odc.cashpoweredg.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.odc.cashpoweredg.data.repository.CashPowerRepository
import com.odc.cashpoweredg.data.repository.DefaultCashPowerRepository
import com.odc.cashpoweredg.ui.screens.DashboardScreen
import com.odc.cashpoweredg.ui.screens.FormScreen
import com.odc.cashpoweredg.ui.screens.HistoryScreen
import com.odc.cashpoweredg.ui.screens.SettingsScreen
import com.odc.cashpoweredg.viewmodel.CreditViewModel
import com.odc.cashpoweredg.viewmodel.DashboardViewModel
import com.odc.cashpoweredg.viewmodel.HistoryViewModel
import com.odc.cashpoweredg.viewmodel.SettingsViewModel

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    repository: CashPowerRepository = DefaultCashPowerRepository.instance
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // ViewModels créés avec la fabrique qui injecte le Repository
    val dashboardViewModel: DashboardViewModel =
        viewModel(factory = DashboardViewModel.factory(repository))
    val creditViewModel: CreditViewModel =
        viewModel(factory = CreditViewModel.factory(repository))
    val historyViewModel: HistoryViewModel =
        viewModel(factory = HistoryViewModel.factory(repository))
    val settingsViewModel: SettingsViewModel =
        viewModel(factory = SettingsViewModel.factory(repository))

    // Navigation vers un onglet : une seule définition, réutilisée partout
    val navigateTo: (String) -> Unit = { route ->
        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                Screen.destinations.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = { navigateTo(screen.route) },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.label
                            )
                        },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(viewModel = dashboardViewModel)
            }
            composable(Screen.Form.route) {
                FormScreen(viewModel = creditViewModel)
            }
            composable(Screen.History.route) {
                HistoryScreen(viewModel = historyViewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}