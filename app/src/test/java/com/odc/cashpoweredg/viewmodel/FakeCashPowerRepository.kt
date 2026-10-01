package com.odc.cashpoweredg.viewmodel

import com.odc.cashpoweredg.data.entity.AchatCredit
import com.odc.cashpoweredg.data.entity.Parametres
import com.odc.cashpoweredg.data.entity.Releve
import com.odc.cashpoweredg.data.repository.CashPowerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.io.IOException

/** Repository en mémoire pour tester les ViewModels sans Room. */
class FakeCashPowerRepository(parametres: Parametres? = null) : CashPowerRepository {

    val achats = MutableStateFlow<List<AchatCredit>>(emptyList())
    val releves = MutableStateFlow<List<Releve>>(emptyList())
    val parametres = MutableStateFlow(parametres)

    /** Simule une erreur de la base de données lors des écritures. */
    var echecEcriture = false

    override fun observeAchats(): Flow<List<AchatCredit>> = achats

    override fun observeReleves(): Flow<List<Releve>> = releves

    override fun observeParametres(): Flow<Parametres?> = parametres

    override suspend fun insertAchat(achat: AchatCredit) {
        if (echecEcriture) throw IOException("échec simulé")
        achats.update { it + achat }
    }

    override suspend fun insertReleve(releve: Releve) {
        if (echecEcriture) throw IOException("échec simulé")
        releves.update { it + releve }
    }

    override suspend fun saveParametres(parametres: Parametres) {
        if (echecEcriture) throw IOException("échec simulé")
        this.parametres.value = parametres
    }
}
