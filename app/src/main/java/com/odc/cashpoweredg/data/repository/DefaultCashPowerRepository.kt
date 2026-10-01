package com.odc.cashpoweredg.data.repository

import com.odc.cashpoweredg.data.local.entity.AchatCredit
import com.odc.cashpoweredg.data.local.entity.Parametres
import com.odc.cashpoweredg.data.local.entity.Releve
import com.odc.cashpoweredg.model.COMPTEUR_PAR_DEFAUT_ID
import com.odc.cashpoweredg.model.NOM_COMPTEUR_PAR_DEFAUT
import com.odc.cashpoweredg.model.SEUIL_ALERTE_PAR_DEFAUT
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Implémentation en mémoire du Repository pour le socle UI.
 *
 * Utilisé par défaut dans MainActivity et les previews Compose
 * avant la fusion finale avec Room (`feature/room-data`).
 */
class DefaultCashPowerRepository : CashPowerRepository {

    private val now = System.currentTimeMillis()
    private val uneJourneeMs = 24L * 60 * 60 * 1000

    private val _parametres = MutableStateFlow<Parametres?>(
        Parametres(
            id = COMPTEUR_PAR_DEFAUT_ID,
            tarifKwhGnf = 390.0,
            seuilAlerteJours = SEUIL_ALERTE_PAR_DEFAUT,
            nomCompteur = NOM_COMPTEUR_PAR_DEFAUT
        )
    )

    private val _achats = MutableStateFlow<List<AchatCredit>>(
        listOf(
            AchatCredit(
                id = 1,
                compteurId = COMPTEUR_PAR_DEFAUT_ID,
                montantGnf = 100_000,
                kwh = 256.4,
                date = now - (7 * uneJourneeMs)
            ),
            AchatCredit(
                id = 2,
                compteurId = COMPTEUR_PAR_DEFAUT_ID,
                montantGnf = 50_000,
                kwh = 128.2,
                date = now - (2 * uneJourneeMs)
            )
        )
    )

    private val _releves = MutableStateFlow<List<Releve>>(
        listOf(
            Releve(
                id = 1,
                compteurId = COMPTEUR_PAR_DEFAUT_ID,
                kwhRestants = 200.0,
                date = now - (7 * uneJourneeMs)
            ),
            Releve(
                id = 2,
                compteurId = COMPTEUR_PAR_DEFAUT_ID,
                kwhRestants = 85.0,
                date = now - (1 * uneJourneeMs)
            )
        )
    )

    override fun observeAchats(): Flow<List<AchatCredit>> = _achats

    override fun observeReleves(): Flow<List<Releve>> = _releves

    override fun observeParametres(): Flow<Parametres?> = _parametres

    override suspend fun insertAchat(achat: AchatCredit) {
        _achats.update { current ->
            val nextId = (current.maxOfOrNull { it.id } ?: 0) + 1
            current + achat.copy(id = nextId)
        }
    }

    override suspend fun insertReleve(releve: Releve) {
        _releves.update { current ->
            val nextId = (current.maxOfOrNull { it.id } ?: 0) + 1
            current + releve.copy(id = nextId)
        }
    }

    override suspend fun saveParametres(parametres: Parametres) {
        _parametres.value = parametres
    }

    companion object {
        val instance by lazy { DefaultCashPowerRepository() }
    }
}
