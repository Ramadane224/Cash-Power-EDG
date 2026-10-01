package com.odc.cashpoweredg.di

import android.content.Context
import com.odc.cashpoweredg.data.database.AppDatabase
import com.odc.cashpoweredg.data.repository.CashPowerRepository
import com.odc.cashpoweredg.data.repository.CashPowerRepositoryImpl

class AppContainer(context: Context) {

    private val database = AppDatabase.getInstance(context)

    val repository: CashPowerRepository by lazy {
        CashPowerRepositoryImpl(
            achatCreditDao = database.achatCreditDao(),
            releveDao = database.releveDao(),
            parametresDao = database.parametresDao()
        )
    }
}
