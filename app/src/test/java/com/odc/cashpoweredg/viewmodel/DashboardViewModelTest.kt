package com.odc.cashpoweredg.viewmodel

import com.odc.cashpoweredg.data.local.entity.AchatCredit
import com.odc.cashpoweredg.data.local.entity.Parametres
import com.odc.cashpoweredg.data.local.entity.Releve
import com.odc.cashpoweredg.data.repository.CashPowerRepository
import com.odc.cashpoweredg.model.CreditState
import com.odc.cashpoweredg.model.MILLIS_PAR_JOUR
import com.odc.cashpoweredg.model.MessagesErreur
import com.odc.cashpoweredg.model.UiStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun jour(n: Int): Long = (20_000L + n) * MILLIS_PAR_JOUR

    private val maintenant = jour(4)

    private val parametres = Parametres(
        tarifKwhGnf = 2_000.0,
        seuilAlerteJours = 2.0,
        nomCompteur = "Compteur Maison"
    )

    // Jour 1 : 50 kWh, jour 3 : 24 kWh → 13 kWh/jour
    private val releves = listOf(
        Releve(compteurId = 1, kwhRestants = 50.0, date = jour(1)),
        Releve(compteurId = 1, kwhRestants = 24.0, date = jour(3))
    )

    @Test
    fun `sans donnees le tableau de bord est vide et l estimation indisponible`() {
        val etat = buildDashboardUiState(emptyList(), emptyList(), null, maintenant)

        assertTrue(etat.aucunReleve)
        assertEquals(UiStatus.EMPTY, etat.statut)
        assertNull(etat.consommationMoyenne)
        assertNull(etat.joursRestants)
        assertEquals(CreditState.UNKNOWN, etat.etatCredit)
        assertFalse(etat.enAlerte)
        assertEquals(2.0, etat.seuilAlerte, 0.0)
    }

    @Test
    fun `un seul releve affiche les kWh sans estimation`() {
        val etat = buildDashboardUiState(releves.take(1), emptyList(), parametres, maintenant)

        assertFalse(etat.aucunReleve)
        assertEquals(50.0, etat.kwhRestants, 0.0001)
        assertNull(etat.joursRestants)
        assertEquals(CreditState.UNKNOWN, etat.etatCredit)
    }

    @Test
    fun `42 kWh a 13 kWh par jour donnent un credit suffisant`() {
        // 24 kWh au dernier relevé + achat de 36 000 GNF (18 kWh) le jour 4 = 42 kWh
        val achats = listOf(
            AchatCredit(compteurId = 1, montantGnf = 36_000, kwh = 18.0, date = jour(4))
        )

        val etat = buildDashboardUiState(releves, achats, parametres, maintenant)

        assertEquals(42.0, etat.kwhRestants, 0.0001)
        assertEquals(13.0, etat.consommationMoyenne!!, 0.0001)
        assertEquals(3.23, etat.joursRestants!!, 0.01)
        assertEquals(CreditState.NORMAL, etat.etatCredit)
        assertFalse(etat.enAlerte)
        assertEquals(UiStatus.SUCCESS, etat.statut)
        assertEquals("Compteur Maison", etat.nomCompteur)
        assertEquals(36_000L, etat.coutMoisEnCoursGnf)
    }

    @Test
    fun `credit sous le seuil passe en alerte`() {
        // 24 kWh / 13 kWh par jour ≈ 1,85 jour <= 2 jours
        val etat = buildDashboardUiState(releves, emptyList(), parametres, maintenant)

        assertEquals(1.85, etat.joursRestants!!, 0.01)
        assertEquals(CreditState.WARNING, etat.etatCredit)
        assertTrue(etat.enAlerte)
    }

    @Test
    fun `credit epuise est critique`() {
        val epuise = releves + Releve(compteurId = 1, kwhRestants = 0.0, date = jour(4))

        val etat = buildDashboardUiState(epuise, emptyList(), parametres, maintenant)

        assertEquals(0.0, etat.joursRestants!!, 0.0)
        assertEquals(CreditState.CRITICAL, etat.etatCredit)
        assertTrue(etat.enAlerte)
    }

    @Test
    fun `le ViewModel suit les donnees du repository`() = runTest {
        val repository = FakeCashPowerRepository(parametres)
        val viewModel = DashboardViewModel(repository, horloge = { maintenant })
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        assertEquals(UiStatus.EMPTY, viewModel.uiState.value.statut)

        repository.releves.value = releves

        val etat = viewModel.uiState.value
        assertEquals(UiStatus.SUCCESS, etat.statut)
        assertEquals(24.0, etat.kwhRestants, 0.0001)
        assertEquals(CreditState.WARNING, etat.etatCredit)
    }

    @Test
    fun `une erreur de chargement est exposee dans l etat`() = runTest {
        val repository = object : CashPowerRepository by FakeCashPowerRepository() {
            override fun observeReleves() =
                flow<List<Releve>> { throw IOException("base indisponible") }
        }
        val viewModel = DashboardViewModel(repository, horloge = { maintenant })
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        assertEquals(MessagesErreur.CHARGEMENT, viewModel.uiState.value.error)
        assertEquals(UiStatus.ERROR, viewModel.uiState.value.statut)
    }

    @Test
    fun `l etat initial est en chargement`() {
        val viewModel = DashboardViewModel(
            FakeCashPowerRepository(parametres),
            horloge = { maintenant }
        )

        assertEquals(UiStatus.LOADING, viewModel.uiState.value.statut)
    }
}