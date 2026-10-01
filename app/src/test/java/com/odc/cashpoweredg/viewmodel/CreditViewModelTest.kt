package com.odc.cashpoweredg.viewmodel

import com.odc.cashpoweredg.data.entity.Parametres
import com.odc.cashpoweredg.model.MessagesErreur
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CreditViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val maintenant = 1_800_000_000_000L

    private val parametres = Parametres(tarifKwhGnf = 2_000.0, seuilAlerteJours = 2.0, nomCompteur = "Compteur Maison")

    private fun creerViewModel(repository: FakeCashPowerRepository = FakeCashPowerRepository(parametres)) =
        CreditViewModel(repository, horloge = { maintenant })

    @Test
    fun `le formulaire sait si le tarif est configure`() {
        assertTrue(creerViewModel().uiState.value.tarifConfigure)
        assertFalse(creerViewModel(FakeCashPowerRepository(parametres = null)).uiState.value.tarifConfigure)
    }

    @Test
    fun `la conversion en kWh s affiche pendant la saisie`() {
        val viewModel = creerViewModel()

        viewModel.onMontantChange("100 000")

        assertEquals(50.0, viewModel.uiState.value.kwhCalcule!!, 0.0001)
    }

    @Test
    fun `un achat valide est enregistre avec les kWh calcules`() {
        val repository = FakeCashPowerRepository(parametres)
        val viewModel = creerViewModel(repository)

        viewModel.onMontantChange("100000")
        viewModel.enregistrer()

        val achat = repository.achats.value.single()
        assertEquals(100_000L, achat.montantGnf)
        assertEquals(50.0, achat.kwh, 0.0001)
        assertEquals(maintenant, achat.date)
        val etat = viewModel.uiState.value
        assertTrue(etat.isSaved)
        assertEquals("", etat.montantGnf)
        assertNull(etat.error)
    }

    @Test
    fun `montant vide ou nul n est pas enregistre`() {
        val repository = FakeCashPowerRepository(parametres)
        val viewModel = creerViewModel(repository)

        viewModel.enregistrer()
        assertEquals(MessagesErreur.CHAMPS_VIDES, viewModel.uiState.value.error)

        viewModel.onMontantChange("0")
        viewModel.enregistrer()
        assertEquals(MessagesErreur.MONTANT_INVALIDE, viewModel.uiState.value.error)

        assertTrue(repository.achats.value.isEmpty())
    }

    @Test
    fun `un achat sans tarif configure est refuse`() {
        val repository = FakeCashPowerRepository(parametres = null)
        val viewModel = creerViewModel(repository)

        viewModel.onMontantChange("100000")
        assertNull(viewModel.uiState.value.kwhCalcule)
        viewModel.enregistrer()

        assertEquals(MessagesErreur.TARIF_NON_CONFIGURE, viewModel.uiState.value.error)
        assertTrue(repository.achats.value.isEmpty())
    }

    @Test
    fun `une date dans le futur est refusee`() {
        val repository = FakeCashPowerRepository(parametres)
        val viewModel = creerViewModel(repository)

        viewModel.onMontantChange("100000")
        viewModel.onDateChange(maintenant + 1)
        viewModel.enregistrer()

        assertEquals(MessagesErreur.DATE_FUTURE, viewModel.uiState.value.error)
        assertTrue(repository.achats.value.isEmpty())
    }

    @Test
    fun `un releve valide est enregistre`() {
        val repository = FakeCashPowerRepository(parametres)
        val viewModel = creerViewModel(repository)

        viewModel.onTypeChange(TypeSaisie.RELEVE)
        viewModel.onKwhRestantsChange("42,5")
        viewModel.enregistrer()

        assertEquals(42.5, repository.releves.value.single().kwhRestants, 0.0001)
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `un releve negatif est refuse`() {
        val repository = FakeCashPowerRepository(parametres)
        val viewModel = creerViewModel(repository)

        viewModel.onTypeChange(TypeSaisie.RELEVE)
        viewModel.onKwhRestantsChange("-3")
        viewModel.enregistrer()

        assertEquals(MessagesErreur.KWH_NEGATIF, viewModel.uiState.value.error)
        assertTrue(repository.releves.value.isEmpty())
    }

    @Test
    fun `une erreur d enregistrement est affichee`() {
        val repository = FakeCashPowerRepository(parametres).apply { echecEcriture = true }
        val viewModel = creerViewModel(repository)

        viewModel.onMontantChange("100000")
        viewModel.enregistrer()

        val etat = viewModel.uiState.value
        assertEquals(MessagesErreur.ENREGISTREMENT, etat.error)
        assertFalse(etat.isSaving)
        assertFalse(etat.isSaved)
        assertEquals("100000", etat.montantGnf)
    }
}
