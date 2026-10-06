package com.odc.cashpoweredg.di

import android.content.Context
import com.odc.cashpoweredg.data.database.AppDatabase
import com.odc.cashpoweredg.data.entity.Compteur
import com.odc.cashpoweredg.data.repository.CashPowerRepository
import com.odc.cashpoweredg.data.repository.CashPowerRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppContainer(context: Context) {

    private val database = AppDatabase.getInstance(context)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val compteur = database.compteurDao().getById(COMPTEUR_PAR_DEFAUT_ID)

            if (compteur == null) {
                database.compteurDao().insert(
                    Compteur(
                        id = COMPTEUR_PAR_DEFAUT_ID,
                        nom = "Maison",
                        numero = ""
                    )
                )
            }
        }
    }

    val repository: CashPowerRepository by lazy {
        CashPowerRepositoryImpl(
            achatCreditDao = database.achatCreditDao(),
            releveDao = database.releveDao(),
            parametresDao = database.parametresDao()
        )
    }

    companion object {
        private const val COMPTEUR_PAR_DEFAUT_ID = 1
    }
}