package com.odc.cashpoweredg.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.odc.cashpoweredg.data.local.entity.AchatCredit
import com.odc.cashpoweredg.data.local.entity.Parametres
import com.odc.cashpoweredg.data.local.entity.Releve
import com.odc.cashpoweredg.data.repository.CashPowerRepository
import com.odc.cashpoweredg.model.CreditState
import com.odc.cashpoweredg.model.MessagesErreur
import com.odc.cashpoweredg.model.SEUIL_ALERTE_PAR_DEFAUT
import com.odc.cashpoweredg.model.calculateAverageDailyConsumption
import com.odc.cashpoweredg.model.calculateMonthlyCost
import com.odc.cashpoweredg.model.calculateRemainingKwh
import com.odc.cashpoweredg.model.determineCreditState
import com.odc.cashpoweredg.model.estimateRemainingDays
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * R1 — État du tableau de bord, prêt à être affiché par DashboardScreen.
 *
 * @property kwhRestants crédit restant : dernier relevé + achats enregistrés depuis (0.0 si aucun relevé).
 * @property consommationMoyenne kWh/jour, `null` s'il n'y a pas assez de relevés.
 * @property joursRestants `null` si l'estimation est indisponible.
 * @property etatCredit à afficher tel quel : la règle est dans [determineCreditState].
 * @property coutMoisEnCoursGnf total des achats du mois en cours.
 * @property aucunReleve `true` tant qu'aucun relevé n'a été saisi (état vide).
 */
data class DashboardUiState(
    val kwhRestants: Double = 0.0,
    val consommationMoyenne: Double? = null,
    val joursRestants: Double? = null,
    val seuilAlerte: Double = SEUIL_ALERTE_PAR_DEFAUT,
    val etatCredit: CreditState = CreditState.UNKNOWN,
    val nomCompteur: String = "",
    val coutMoisEnCoursGnf: Long = 0,
    val aucunReleve: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val statut: UiStatus
        get() = when {
            isLoading -> UiStatus.LOADING
            error != null -> UiStatus.ERROR
            aucunReleve -> UiStatus.EMPTY
            else -> UiStatus.SUCCESS
        }

    /** Vrai quand il faut afficher l'avertissement « Pensez à recharger ». */
    val enAlerte: Boolean
        get() = etatCredit == CreditState.WARNING || etatCredit == CreditState.CRITICAL
}

class DashboardViewModel(
    repository: CashPowerRepository,
    horloge: () -> Long = System::currentTimeMillis
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.observeReleves(),
        repository.observeAchats(),
        repository.observeParametres()
    ) { releves, achats, parametres ->
        buildDashboardUiState(releves, achats, parametres, horloge())
    }
        .catch { emit(DashboardUiState(error = MessagesErreur.CHARGEMENT)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState(isLoading = true)
        )

    companion object {
        fun factory(repository: CashPowerRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { DashboardViewModel(repository) }
        }
    }
}

/** Assemble l'état du tableau de bord à partir des données du Repository. */
internal fun buildDashboardUiState(
    releves: List<Releve>,
    achats: List<AchatCredit>,
    parametres: Parametres?,
    maintenant: Long
): DashboardUiState {
    val seuilAlerte = parametres?.seuilAlerteJours ?: SEUIL_ALERTE_PAR_DEFAUT
    val kwhRestants = calculateRemainingKwh(releves, achats)
    val consommationMoyenne = calculateAverageDailyConsumption(releves, achats)
    val joursRestants = kwhRestants?.let { estimateRemainingDays(it, consommationMoyenne) }

    return DashboardUiState(
        kwhRestants = kwhRestants ?: 0.0,
        consommationMoyenne = consommationMoyenne,
        joursRestants = joursRestants,
        seuilAlerte = seuilAlerte,
        etatCredit = determineCreditState(joursRestants, seuilAlerte),
        nomCompteur = parametres?.nomCompteur.orEmpty(),
        coutMoisEnCoursGnf = calculateMonthlyCost(achats, maintenant),
        aucunReleve = kwhRestants == null
    )
}
