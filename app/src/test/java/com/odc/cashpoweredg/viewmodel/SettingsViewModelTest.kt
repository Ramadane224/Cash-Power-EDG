package com.odc.cashpoweredg.viewmodel

import com.odc.cashpoweredg.data.entity.Parametres
import com.odc.cashpoweredg.model.MessagesErreur
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `les parametres existants remplissent le formulaire`() {
        val repository = FakeCashPowerRepository(
            Parametres(tarifKwhGnf = 2_000.0, seuilAlerteJours = 2.0, nomCompteur = "Boutique")
        )

        val etat = SettingsViewModel(repository).uiState.value

        assertEquals(UiStatus.SUCCESS, etat.statut)
        assertEquals("Boutique", etat.nomCompteur)
        assertEquals("2000", etat.tarifKwhGnf)
        assertEquals("2", etat.seuilAlerteJours)
    }

    @Test
    fun `sans parametres les valeurs par defaut sont proposees`() {
        val etat = SettingsViewModel(FakeCashPowerRepository(parametres = null)).uiState.value

        assertEquals(UiStatus.EMPTY, etat.statut)
        assertEquals("Compteur Maison", etat.nomCompteur)
        assertEquals("", etat.tarifKwhGnf)
        assertEquals("2", etat.seuilAlerteJours)
    }

    @Test
    fun `des parametres valides sont enregistres`() {
        val repository = FakeCashPowerRepository(parametres = null)
        val viewModel = SettingsViewModel(repository)

        viewModel.onTarifChange("2 000")
        viewModel.onSeuilChange("1,5")
        viewModel.enregistrer()

        assertEquals(
            Parametres(tarifKwhGnf = 2_000.0, seuilAlerteJours = 1.5, nomCompteur = "Compteur Maison"),
            repository.parametres.value
        )
        assertTrue(viewModel.uiState.value.isSaved)
        assertEquals(UiStatus.SUCCESS, viewModel.uiState.value.statut)
    }

    @Test
    fun `un tarif nul est refuse`() {
        val repository = FakeCashPowerRepository(parametres = null)
        val viewModel = SettingsViewModel(repository)

        viewModel.onTarifChange("0")
        viewModel.enregistrer()

        assertEquals(MessagesErreur.TARIF_INVALIDE, viewModel.uiState.value.error)
        assertEquals(null, repository.parametres.value)
    }

    @Test
    fun `un nom de compteur vide est refuse`() {
        val viewModel = SettingsViewModel(FakeCashPowerRepository(parametres = null))

        viewModel.onNomCompteurChange("  ")
        viewModel.onTarifChange("2000")
        viewModel.enregistrer()

        assertEquals(MessagesErreur.CHAMPS_VIDES, viewModel.uiState.value.error)
    }

    @Test
    fun `une erreur d enregistrement est affichee`() {
        val repository = FakeCashPowerRepository(parametres = null).apply { echecEcriture = true }
        val viewModel = SettingsViewModel(repository)

        viewModel.onTarifChange("2000")
        viewModel.enregistrer()

        assertEquals(MessagesErreur.ENREGISTREMENT, viewModel.uiState.value.error)
        assertEquals(UiStatus.ERROR, viewModel.uiState.value.statut)
        assertFalse(viewModel.uiState.value.isSaved)
    }
}
