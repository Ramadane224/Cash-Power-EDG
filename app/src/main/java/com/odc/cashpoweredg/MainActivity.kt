package com.odc.cashpoweredg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.odc.cashpoweredg.di.AppContainer
import com.odc.cashpoweredg.navigation.AppNavigation
import com.odc.cashpoweredg.ui.theme.CashPowerEDGTheme

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appContainer = AppContainer(applicationContext)

        enableEdgeToEdge()

        setContent {
            CashPowerEDGTheme {
                AppNavigation()
            }
        }
    }
}