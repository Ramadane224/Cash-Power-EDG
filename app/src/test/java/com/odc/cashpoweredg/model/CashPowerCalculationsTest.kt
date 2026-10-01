package com.odc.cashpoweredg.model

import com.odc.cashpoweredg.data.entity.AchatCredit
import com.odc.cashpoweredg.data.entity.Releve
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class CashPowerCalculationsTest {

    private fun jour(n: Int): Long = (20_000L + n) * MILLIS_PAR_JOUR

    private fun releve(jour: Int, kwh: Double, id: Int = 0) =
        Releve(id = id, compteurId = 1, kwhRestants = kwh, date = jour(jour))

    private fun achat(jour: Int, kwh: Double, montant: Long = (kwh * 2_000).toLong()) =
        AchatCredit(compteurId = 1, montantGnf = montant, kwh = kwh, date = jour(jour))

    // --- R2 : conversion GNF → kWh ---

    @Test
    fun `100 000 GNF a 2 000 GNF par kWh donnent 50 kWh`() {
        assertEquals(50.0, calculateKwh(100_000, 2_000.0), 0.0001)
    }

    @Test
    fun `montant nul ou negatif est refuse`() {
        val nul = assertThrows(IllegalArgumentException::class.java) { calculateKwh(0, 2_000.0) }
        assertEquals(MessagesErreur.MONTANT_INVALIDE, nul.message)
        assertThrows(IllegalArgumentException::class.java) { calculateKwh(-5_000, 2_000.0) }
    }

    @Test
    fun `tarif nul ou negatif est refuse`() {
        val nul = assertThrows(IllegalArgumentException::class.java) { calculateKwh(100_000, 0.0) }
        assertEquals(MessagesErreur.TARIF_INVALIDE, nul.message)
        assertThrows(IllegalArgumentException::class.java) { calculateKwh(100_000, -1.0) }
        assertThrows(IllegalArgumentException::class.java) { calculateKwh(100_000, Double.NaN) }
    }

    // --- R3 : consommation moyenne ---

    @Test
    fun `aucun releve donne une moyenne indisponible`() {
        assertNull(calculateAverageDailyConsumption(emptyList()))
    }

    @Test
    fun `un seul releve donne une moyenne indisponible`() {
        assertNull(calculateAverageDailyConsumption(listOf(releve(1, 50.0))))
    }

    @Test
    fun `50 kWh puis 24 kWh deux jours plus tard donnent 13 kWh par jour`() {
        val moyenne = calculateAverageDailyConsumption(listOf(releve(1, 50.0), releve(3, 24.0)))
        assertEquals(13.0, moyenne!!, 0.0001)
    }

    @Test
    fun `l ordre des releves n a pas d importance`() {
        val moyenne = calculateAverageDailyConsumption(listOf(releve(3, 24.0), releve(1, 50.0)))
        assertEquals(13.0, moyenne!!, 0.0001)
    }

    @Test
    fun `releves a la meme date donnent une moyenne indisponible`() {
        assertNull(calculateAverageDailyConsumption(listOf(releve(1, 50.0), releve(1, 40.0))))
    }

    @Test
    fun `un achat entre deux releves est pris en compte`() {
        // 50 kWh + 25 kWh achetés − 49 kWh = 26 kWh consommés en 2 jours
        val moyenne = calculateAverageDailyConsumption(
            releves = listOf(releve(1, 50.0), releve(3, 49.0)),
            achats = listOf(achat(2, 25.0))
        )
        assertEquals(13.0, moyenne!!, 0.0001)
    }

    @Test
    fun `une hausse sans achat enregistre est ignoree`() {
        assertNull(calculateAverageDailyConsumption(listOf(releve(1, 20.0), releve(3, 60.0))))
    }

    @Test
    fun `les paires incoherentes sont ignorees et les autres conservees`() {
        val releves = listOf(
            releve(1, 50.0),
            releve(3, 24.0), // 26 kWh en 2 jours
            releve(4, 80.0), // recharge non enregistrée : paire ignorée
            releve(6, 54.0)  // 26 kWh en 2 jours
        )
        assertEquals(13.0, calculateAverageDailyConsumption(releves)!!, 0.0001)
    }

    @Test
    fun `un releve avec des kWh negatifs est ignore`() {
        val releves = listOf(releve(1, 50.0), releve(2, -10.0), releve(3, 24.0))
        assertEquals(13.0, calculateAverageDailyConsumption(releves)!!, 0.0001)
    }

    @Test
    fun `aucune consommation donne une moyenne de zero`() {
        assertEquals(0.0, calculateAverageDailyConsumption(listOf(releve(1, 30.0), releve(3, 30.0)))!!, 0.0001)
    }

    // --- Crédit restant ---

    @Test
    fun `sans releve le credit restant est inconnu`() {
        assertNull(calculateRemainingKwh(emptyList(), listOf(achat(1, 50.0))))
    }

    @Test
    fun `le credit restant ajoute les achats posterieurs au dernier releve`() {
        val kwh = calculateRemainingKwh(
            releves = listOf(releve(1, 50.0), releve(3, 24.0)),
            achats = listOf(achat(2, 10.0), achat(4, 18.0))
        )
        assertEquals(42.0, kwh!!, 0.0001)
    }

    @Test
    fun `a date egale le dernier releve enregistre est retenu`() {
        val kwh = calculateRemainingKwh(listOf(releve(3, 30.0, id = 2), releve(3, 24.0, id = 1)), emptyList())
        assertEquals(30.0, kwh!!, 0.0001)
    }

    // --- R4 : jours restants ---

    @Test
    fun `42 kWh a 13 kWh par jour donnent environ 3,23 jours`() {
        assertEquals(3.23, estimateRemainingDays(42.0, 13.0)!!, 0.01)
    }

    @Test
    fun `consommation nulle ou inconnue donne une estimation indisponible`() {
        assertNull(estimateRemainingDays(42.0, 0.0))
        assertNull(estimateRemainingDays(42.0, null))
        assertNull(estimateRemainingDays(42.0, -3.0))
    }

    @Test
    fun `credit epuise donne zero jour`() {
        assertEquals(0.0, estimateRemainingDays(0.0, 13.0)!!, 0.0)
        assertEquals(0.0, estimateRemainingDays(0.0, null)!!, 0.0)
    }

    @Test
    fun `kWh negatifs donnent une estimation indisponible`() {
        assertNull(estimateRemainingDays(-1.0, 13.0))
    }

    // --- R5 : état du crédit ---

    @Test
    fun `etat du credit selon le seuil`() {
        assertEquals(CreditState.UNKNOWN, determineCreditState(null, 2.0))
        assertEquals(CreditState.NORMAL, determineCreditState(3.23, 2.0))
        assertEquals(CreditState.WARNING, determineCreditState(2.0, 2.0))
        assertEquals(CreditState.WARNING, determineCreditState(1.5, 2.0))
        assertEquals(CreditState.CRITICAL, determineCreditState(0.0, 2.0))
        assertEquals(CreditState.CRITICAL, determineCreditState(-1.0, 2.0))
    }

    // --- Coût mensuel ---

    private val utc = TimeZone.getTimeZone("UTC")

    private fun date(annee: Int, mois: Int, jour: Int): Long = Calendar.getInstance(utc).apply {
        clear()
        set(annee, mois, jour, 12, 0)
    }.timeInMillis

    private val achatsSurTroisMois = listOf(
        AchatCredit(compteurId = 1, montantGnf = 50_000, kwh = 25.0, date = date(2026, Calendar.AUGUST, 31)),
        AchatCredit(compteurId = 1, montantGnf = 100_000, kwh = 50.0, date = date(2026, Calendar.SEPTEMBER, 1)),
        AchatCredit(compteurId = 1, montantGnf = 20_000, kwh = 10.0, date = date(2026, Calendar.SEPTEMBER, 28)),
        AchatCredit(compteurId = 1, montantGnf = 70_000, kwh = 35.0, date = date(2026, Calendar.OCTOBER, 1))
    )

    @Test
    fun `le cout du mois en cours ne compte que les achats de ce mois`() {
        assertEquals(120_000L, calculateMonthlyCost(achatsSurTroisMois, date(2026, Calendar.SEPTEMBER, 15), utc))
    }

    @Test
    fun `le cout d un mois sans achat est zero`() {
        assertEquals(0L, calculateMonthlyCost(achatsSurTroisMois, date(2026, Calendar.DECEMBER, 15), utc))
        assertEquals(0L, calculateMonthlyCost(emptyList(), date(2026, Calendar.SEPTEMBER, 15), utc))
    }

    @Test
    fun `les couts mensuels sont regroupes par mois du plus recent au plus ancien`() {
        assertEquals(
            listOf(
                CoutMensuel(annee = 2026, mois = 10, totalGnf = 70_000),
                CoutMensuel(annee = 2026, mois = 9, totalGnf = 120_000),
                CoutMensuel(annee = 2026, mois = 8, totalGnf = 50_000)
            ),
            calculateMonthlyCosts(achatsSurTroisMois, utc)
        )
    }

    @Test
    fun `les couts mensuels distinguent les annees`() {
        val achats = listOf(
            AchatCredit(compteurId = 1, montantGnf = 10_000, kwh = 5.0, date = date(2025, Calendar.DECEMBER, 20)),
            AchatCredit(compteurId = 1, montantGnf = 30_000, kwh = 15.0, date = date(2026, Calendar.JANUARY, 5))
        )

        assertEquals(
            listOf(
                CoutMensuel(annee = 2026, mois = 1, totalGnf = 30_000),
                CoutMensuel(annee = 2025, mois = 12, totalGnf = 10_000)
            ),
            calculateMonthlyCosts(achats, utc)
        )
    }

    @Test
    fun `aucun achat donne aucun cout mensuel`() {
        assertEquals(emptyList<CoutMensuel>(), calculateMonthlyCosts(emptyList(), utc))
    }
}
