package com.odc.cashpoweredg.data.repository

import com.odc.cashpoweredg.data.local.entity.AchatCredit
import com.odc.cashpoweredg.data.local.entity.Parametres
import com.odc.cashpoweredg.data.local.entity.Releve
import kotlinx.coroutines.flow.Flow

/**
 * CONTRAT ATTENDU — interface dont les ViewModels ont besoin.
 *
 * Ismail en reste propriétaire (branche `feature/room-data`) : son
 * `CashPowerRepositoryImpl` l'implémente avec les DAO Room. Il peut ajouter
 * d'autres méthodes ; si l'une de celles-ci est renommée, il suffit
 * d'adapter les appels dans `viewmodel/`.
 */
interface CashPowerRepository {

    /** Tous les achats, mis à jour automatiquement (Room → Flow). */
    fun observeAchats(): Flow<List<AchatCredit>>

    /** Tous les relevés, mis à jour automatiquement. */
    fun observeReleves(): Flow<List<Releve>>

    /** Paramètres (ligne id = 1), ou `null` s'ils n'ont jamais été enregistrés. */
    fun observeParametres(): Flow<Parametres?>

    suspend fun insertAchat(achat: AchatCredit)

    suspend fun insertReleve(releve: Releve)

    /** Crée ou remplace les paramètres (ligne id = 1). */
    suspend fun saveParametres(parametres: Parametres)
}
