package com.odc.cashpoweredg.data.repository

import com.odc.cashpoweredg.data.entity.AchatCredit
import com.odc.cashpoweredg.data.entity.Parametres
import com.odc.cashpoweredg.data.entity.Releve
import kotlinx.coroutines.flow.Flow

interface CashPowerRepository {

    fun observeAchats(): Flow<List<AchatCredit>>

    fun observeReleves(): Flow<List<Releve>>

    fun observeParametres(): Flow<Parametres?>

    suspend fun insertAchat(achat: AchatCredit)

    suspend fun insertReleve(releve: Releve)

    suspend fun saveParametres(parametres: Parametres)
}
