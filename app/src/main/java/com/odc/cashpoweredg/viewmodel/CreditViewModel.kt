package com.odc.cashpoweredg.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.odc.cashpoweredg.data.local.entity.AchatCredit
import com.odc.cashpoweredg.data.local.entity.Releve
import com.odc.cashpoweredg.data.repository.CashPowerRepository
import com.odc.cashpoweredg.model.COMPTEUR_PAR_DEFAUT_ID
import com.odc.cashpoweredg.model.MessagesErreur
import com.odc.cashpoweredg.model.ValidationResult
import com.odc.cashpoweredg.model.calculateKwh
import com.odc.cashpoweredg.model.validateDate
import com.odc.cashpoweredg.model.validateKwh
import com.odc.cashpoweredg.model.validateMontant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TypeSaisie { ACHAT, RELEVE }

/**
 * État du formulaire achat / relevé.
 *
 * Les champs texte gardent la saisie brute ; la conversion et la validation
 * sont faites par le ViewModel au moment d'enregistrer.
 *
 * États du formulaire : chargement = [isSaving], succès = [isSaved], erreur = [error].
 *
 * @property kwhCalcule aperçu de la conversion GNF → kWh pendant la saisie du montant.
 * @property isSaved passe à `true` après un enregistrement réussi ; l'écran appelle
 * ensuite [CreditViewModel.onSavedHandled].
 */
data class CreditFormUiState(
    val type: TypeSaisie = TypeSaisie.ACHAT,
    val montantGnf: String = "",
    val kwhRestants: String = "",
    val date: Long? = null,
    val tarifKwhGnf: Double? = null,
    val kwhCalcule: Double? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
) {
    /** Faux tant que le tarif n'est pas configuré : l'écran peut inviter à aller dans Paramètres. */
    val tarifConfigure: Boolean get() = tarifKwhGnf != null && tarifKwhGnf > 0
}

class CreditViewModel(
    private val repository: CashPowerRepository,
    private val compteurId: Int = COMPTEUR_PAR_DEFAUT_ID,
    private val horloge: () -> Long = System::currentTimeMillis
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreditFormUiState(date = horloge()))
    val uiState: StateFlow<CreditFormUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeParametres()
                .catch { _uiState.update { it.copy(error = MessagesErreur.CHARGEMENT) } }
                .collect { parametres ->
                    _uiState.update { it.copy(tarifKwhGnf = parametres?.tarifKwhGnf).avecKwhCalcule() }
                }
        }
    }

    fun onTypeChange(type: TypeSaisie) {
        _uiState.update { it.copy(type = type, error = null) }
    }

    fun onMontantChange(montantGnf: String) {
        _uiState.update { it.copy(montantGnf = montantGnf, error = null).avecKwhCalcule() }
    }

    fun onKwhRestantsChange(kwhRestants: String) {
        _uiState.update { it.copy(kwhRestants = kwhRestants, error = null) }
    }

    fun onDateChange(date: Long) {
        _uiState.update { it.copy(date = date, error = null) }
    }

    fun onSavedHandled() {
        _uiState.update { it.copy(isSaved = false) }
    }

    fun enregistrer() {
        val etat = _uiState.value
        if (etat.isSaving) return
        when (etat.type) {
            TypeSaisie.ACHAT -> enregistrerAchat(etat)
            TypeSaisie.RELEVE -> enregistrerReleve(etat)
        }
    }

    private fun enregistrerAchat(etat: CreditFormUiState) {
        val montant = when (val resultat = validateMontant(etat.montantGnf)) {
            is ValidationResult.Invalid -> return afficherErreur(resultat.message)
            is ValidationResult.Valid -> resultat.value
        }
        val tarif = etat.tarifKwhGnf?.takeIf { it > 0 }
            ?: return afficherErreur(MessagesErreur.TARIF_NON_CONFIGURE)
        val date = when (val resultat = validateDate(etat.date, horloge())) {
            is ValidationResult.Invalid -> return afficherErreur(resultat.message)
            is ValidationResult.Valid -> resultat.value
        }

        val achat = AchatCredit(
            compteurId = compteurId,
            montantGnf = montant,
            kwh = calculateKwh(montant, tarif),
            date = date
        )
        sauvegarder { repository.insertAchat(achat) }
    }

    private fun enregistrerReleve(etat: CreditFormUiState) {
        val kwh = when (val resultat = validateKwh(etat.kwhRestants)) {
            is ValidationResult.Invalid -> return afficherErreur(resultat.message)
            is ValidationResult.Valid -> resultat.value
        }
        val date = when (val resultat = validateDate(etat.date, horloge())) {
            is ValidationResult.Invalid -> return afficherErreur(resultat.message)
            is ValidationResult.Valid -> resultat.value
        }

        val releve = Releve(compteurId = compteurId, kwhRestants = kwh, date = date)
        sauvegarder { repository.insertReleve(releve) }
    }

    private fun sauvegarder(operation: suspend () -> Unit) {
        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                operation()
                _uiState.update {
                    it.copy(
                        montantGnf = "",
                        kwhRestants = "",
                        kwhCalcule = null,
                        date = horloge(),
                        isSaving = false,
                        isSaved = true
                    )
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

    private fun CreditFormUiState.avecKwhCalcule(): CreditFormUiState {
        val montant = (validateMontant(montantGnf) as? ValidationResult.Valid)?.value
        val tarif = tarifKwhGnf?.takeIf { it > 0 }
        val kwh = if (montant != null && tarif != null) calculateKwh(montant, tarif) else null
        return copy(kwhCalcule = kwh)
    }

    companion object {
        fun factory(repository: CashPowerRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { CreditViewModel(repository) }
        }
    }
}
