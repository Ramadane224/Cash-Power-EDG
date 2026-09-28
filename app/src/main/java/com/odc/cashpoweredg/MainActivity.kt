package com.odc.cashpoweredg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.odc.cashpoweredg.navigation.AppNavigation
import com.odc.cashpoweredg.ui.theme.CashPowerEDGTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CashPowerEDGTheme {
                AppNavigation()
            }
        }
    }
}