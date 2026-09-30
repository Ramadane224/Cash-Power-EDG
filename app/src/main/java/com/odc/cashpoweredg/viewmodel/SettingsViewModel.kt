package com.odc.cashpoweredg.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.odc.cashpoweredg.data.local.entity.Parametres
import com.odc.cashpoweredg.data.repository.CashPowerRepository
import com.odc.cashpoweredg.model.MessagesErreur
import com.odc.cashpoweredg.model.NOM_COMPTEUR_PAR_DEFAUT
import com.odc.cashpoweredg.model.SEUIL_ALERTE_PAR_DEFAUT
import com.odc.cashpoweredg.model.ValidationResult
import com.odc.cashpoweredg.model.formatPourSaisie
import com.odc.cashpoweredg.model.validateNomCompteur
import com.odc.cashpoweredg.model.validateSeuil
import com.odc.cashpoweredg.model.validateTarif
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * État de l'écran Paramètres. Les champs gardent la saisie brute ("2 000", "2,5") ;
 * la validation est faite au moment d'enregistrer.
 *
 * @property parametresEnregistres `false` à la première utilisation : le tarif reste à configurer.
 */
data class SettingsUiState(
    val nomCompteur: String = "",
    val tarifKwhGnf: String = "",
    val seuilAlerteJours: String = "",
    val parametresEnregistres: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
) {
    val statut: UiStatus
        get() = when {
            isLoading -> UiStatus.LOADING
            error != null -> UiStatus.ERROR
            !parametresEnregistres -> UiStatus.EMPTY
            else -> UiStatus.SUCCESS
        }
}

class SettingsViewModel(private val repository: CashPowerRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val parametres = repository.observeParametres().first()
                _uiState.update {
                    it.copy(
                        nomCompteur = parametres?.nomCompteur ?: NOM_COMPTEUR_PAR_DEFAUT,
                        tarifKwhGnf = parametres?.tarifKwhGnf?.let(::formatPourSaisie).orEmpty(),
                        seuilAlerteJours = formatPourSaisie(parametres?.seuilAlerteJours ?: SEUIL_ALERTE_PAR_DEFAUT),
                        parametresEnregistres = parametres != null,
                        isLoading = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = MessagesErreur.CHARGEMENT) }
            }
        }
    }

    fun onNomCompteurChange(nomCompteur: String) {
        _uiState.update { it.copy(nomCompteur = nomCompteur, error = null) }
    }

    fun onTarifChange(tarifKwhGnf: String) {
        _uiState.update { it.copy(tarifKwhGnf = tarifKwhGnf, error = null) }
    }

    fun onSeuilChange(seuilAlerteJours: String) {
        _uiState.update { it.copy(seuilAlerteJours = seuilAlerteJours, error = null) }
    }

    fun onSavedHandled() {
        _uiState.update { it.copy(isSaved = false) }
    }

    fun enregistrer() {
        val etat = _uiState.value
        if (etat.isLoading || etat.isSaving) return

        val nomCompteur = when (val resultat = validateNomCompteur(etat.nomCompteur)) {
            is ValidationResult.Invalid -> return afficherErreur(resultat.message)
            is ValidationResult.Valid -> resultat.value
        }
        val tarif = when (val resultat = validateTarif(etat.tarifKwhGnf)) {
            is ValidationResult.Invalid -> return afficherErreur(resultat.message)
            is ValidationResult.Valid -> resultat.value
        }
        val seuil = when (val resultat = validateSeuil(etat.seuilAlerteJours)) {
            is ValidationResult.Invalid -> return afficherErreur(resultat.message)
            is ValidationResult.Valid -> resultat.value
        }

        val parametres = Parametres(
            tarifKwhGnf = tarif,
            seuilAlerteJours = seuil,
            nomCompteur = nomCompteur
        )
        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                repository.saveParametres(parametres)
                _uiState.update {
                    it.copy(nomCompteur = nomCompteur, parametresEnregistres = true, isSaving = false, isSaved = true)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = MessagesErreur.ENREGISTREMENT) }
            }
        }
    }

    private fun afficherErreur(message: String) {
        _uiState.update { it.copy(error = message) }
    }

    companion object {
        fun factory(repository: CashPowerRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { SettingsViewModel(repository) }
        }
    }
}
