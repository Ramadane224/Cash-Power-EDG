package com.odc.cashpoweredg.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.odc.cashpoweredg.data.local.entity.AchatCredit
import com.odc.cashpoweredg.data.local.entity.Releve
import com.odc.cashpoweredg.data.repository.CashPowerRepository
import com.odc.cashpoweredg.model.CoutMensuel
import com.odc.cashpoweredg.model.MessagesErreur
import com.odc.cashpoweredg.model.calculateMonthlyCosts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class FiltreHistorique { TOUS, ACHATS, RELEVES }

/**
 * État de l'historique : listes triées du plus récent au plus ancien.
 *
 * @property coutsMensuels coût de l'électricité par mois (tous les achats, quel que soit le filtre).
 */
data class HistoryUiState(
    val achats: List<AchatCredit> = emptyList(),
    val releves: List<Releve> = emptyList(),
    val coutsMensuels: List<CoutMensuel> = emptyList(),
    val filtre: FiltreHistorique = FiltreHistorique.TOUS,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    /** Vrai quand il faut afficher « Aucune donnée pour le moment. » */
    val estVide: Boolean get() = achats.isEmpty() && releves.isEmpty()

    val statut: UiStatus
        get() = when {
            isLoading -> UiStatus.LOADING
            error != null -> UiStatus.ERROR
            estVide -> UiStatus.EMPTY
            else -> UiStatus.SUCCESS
        }
}

class HistoryViewModel(repository: CashPowerRepository) : ViewModel() {

    private val filtre = MutableStateFlow(FiltreHistorique.TOUS)

    val uiState: StateFlow<HistoryUiState> = combine(
        repository.observeAchats(),
        repository.observeReleves(),
        filtre
    ) { achats, releves, filtreActif ->
        HistoryUiState(
            achats = if (filtreActif == FiltreHistorique.RELEVES) emptyList() else achats.sortedByDescending { it.date },
            releves = if (filtreActif == FiltreHistorique.ACHATS) emptyList() else releves.sortedByDescending { it.date },
            coutsMensuels = calculateMonthlyCosts(achats),
            filtre = filtreActif
        )
    }
        .catch { emit(HistoryUiState(error = MessagesErreur.CHARGEMENT)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState(isLoading = true)
        )

    fun onFiltreChange(nouveauFiltre: FiltreHistorique) {
        filtre.value = nouveauFiltre
    }

    companion object {
        fun factory(repository: CashPowerRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { HistoryViewModel(repository) }
        }
    }
}
