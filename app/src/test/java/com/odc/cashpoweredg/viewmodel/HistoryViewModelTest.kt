package com.odc.cashpoweredg.viewmodel

import com.odc.cashpoweredg.data.entity.AchatCredit
import com.odc.cashpoweredg.data.entity.Releve
import com.odc.cashpoweredg.data.repository.CashPowerRepository
import com.odc.cashpoweredg.model.MessagesErreur
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `historique trie du plus recent au plus ancien et filtrable`() = runTest {
        val repository = FakeCashPowerRepository().apply {
            achats.value = listOf(
                AchatCredit(compteurId = 1, montantGnf = 50_000, kwh = 25.0, date = 1_000),
                AchatCredit(compteurId = 1, montantGnf = 100_000, kwh = 50.0, date = 3_000)
            )
            releves.value = listOf(
                Releve(compteurId = 1, kwhRestants = 42.5, date = 2_000),
                Releve(compteurId = 1, kwhRestants = 35.0, date = 4_000)
            )
        }
        val viewModel = HistoryViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        assertEquals(UiStatus.SUCCESS, viewModel.uiState.value.statut)
        assertEquals(listOf(100_000L, 50_000L), viewModel.uiState.value.achats.map { it.montantGnf })
        assertEquals(listOf(35.0, 42.5), viewModel.uiState.value.releves.map { it.kwhRestants })

        viewModel.onFiltreChange(FiltreHistorique.ACHATS)
        assertEquals(2, viewModel.uiState.value.achats.size)
        assertTrue(viewModel.uiState.value.releves.isEmpty())

        viewModel.onFiltreChange(FiltreHistorique.RELEVES)
        assertTrue(viewModel.uiState.value.achats.isEmpty())
        assertEquals(2, viewModel.uiState.value.releves.size)
    }

    @Test
    fun `l historique expose le cout mensuel meme avec le filtre releves`() = runTest {
        val repository = FakeCashPowerRepository().apply {
            achats.value = listOf(
                AchatCredit(compteurId = 1, montantGnf = 50_000, kwh = 25.0, date = 1_000),
                AchatCredit(compteurId = 1, montantGnf = 100_000, kwh = 50.0, date = 3_000)
            )
        }
        val viewModel = HistoryViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.onFiltreChange(FiltreHistorique.RELEVES)

        assertEquals(150_000L, viewModel.uiState.value.coutsMensuels.sumOf { it.totalGnf })
    }

    @Test
    fun `historique vide`() = runTest {
        val viewModel = HistoryViewModel(FakeCashPowerRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        assertTrue(viewModel.uiState.value.estVide)
        assertEquals(UiStatus.EMPTY, viewModel.uiState.value.statut)
    }

    @Test
    fun `une erreur de chargement est exposee dans l etat`() = runTest {
        val repository = object : CashPowerRepository by FakeCashPowerRepository() {
            override fun observeAchats() = flow<List<AchatCredit>> { throw IOException("base indisponible") }
        }
        val viewModel = HistoryViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        assertEquals(MessagesErreur.CHARGEMENT, viewModel.uiState.value.error)
        assertEquals(UiStatus.ERROR, viewModel.uiState.value.statut)
    }
}
