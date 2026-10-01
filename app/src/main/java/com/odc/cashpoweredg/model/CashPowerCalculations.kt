package com.odc.cashpoweredg.model

import com.odc.cashpoweredg.data.entity.AchatCredit
import com.odc.cashpoweredg.data.entity.Releve
import java.util.Calendar
import java.util.TimeZone

/*
 * Calculs métier de Cash Power EDG (R2 à R5, crédit restant et coût mensuel).
 *
 * Fonctions pures, sans dépendance à Compose ni à Room :
 * elles sont appelées par les ViewModels et testées unitairement.
 */

const val MILLIS_PAR_JOUR: Long = 24L * 60 * 60 * 1000

const val SEUIL_ALERTE_PAR_DEFAUT: Double = 2.0

const val NOM_COMPTEUR_PAR_DEFAUT: String = "Compteur Maison"

/** MVP : un seul compteur, créé par les données initiales (tâche I5). */
const val COMPTEUR_PAR_DEFAUT_ID: Int = 1

/** Ordre chronologique ; à date égale, le dernier enregistré est considéré comme le plus récent. */
private val ORDRE_RELEVES = compareBy<Releve>({ it.date }, { it.id })

private fun Releve.estCoherent(): Boolean = kwhRestants.isFinite() && kwhRestants >= 0

/**
 * R2 — Convertit un montant en GNF en kWh : `kWh = montant / tarif`.
 *
 * Exemple : 100 000 GNF à 2 000 GNF/kWh = 50 kWh.
 *
 * @throws IllegalArgumentException si le montant ou le tarif n'est pas strictement positif.
 */
fun calculateKwh(
    montantGnf: Long,
    tarifKwhGnf: Double
): Double {
    require(montantGnf > 0) { MessagesErreur.MONTANT_INVALIDE }
    require(tarifKwhGnf.isFinite() && tarifKwhGnf > 0) { MessagesErreur.TARIF_INVALIDE }
    return montantGnf.toDouble() / tarifKwhGnf
}

/**
 * R3 — Consommation moyenne journalière en kWh/jour.
 *
 * Pour chaque paire de relevés consécutifs :
 * `consommé = kWh précédent + kWh achetés entre les deux − kWh suivant`.
 * La moyenne est le total consommé divisé par le total des jours couverts.
 *
 * Exemple : jour 1 = 50 kWh, jour 3 = 24 kWh → 26 kWh / 2 jours = 13 kWh/jour.
 *
 * Les paires inexploitables sont ignorées :
 * - dates identiques (durée nulle) ;
 * - relevé avec un kWh négatif ;
 * - consommation négative (par exemple une recharge non enregistrée dans l'application).
 *
 * Un achat daté exactement comme un relevé est considéré comme déjà inclus dans ce relevé.
 *
 * @return la moyenne, ou `null` si aucun relevé, un seul relevé ou aucune paire exploitable.
 */
fun calculateAverageDailyConsumption(
    releves: List<Releve>,
    achats: List<AchatCredit> = emptyList()
): Double? {
    val relevesTries = releves.filter { it.estCoherent() }.sortedWith(ORDRE_RELEVES)

    var kwhConsommes = 0.0
    var dureeMillis = 0L
    for ((precedent, suivant) in relevesTries.zipWithNext()) {
        val duree = suivant.date - precedent.date
        if (duree <= 0) continue

        val kwhAchetes = achats
            .filter { it.date > precedent.date && it.date <= suivant.date }
            .sumOf { it.kwh }
        val consommation = precedent.kwhRestants + kwhAchetes - suivant.kwhRestants
        if (consommation < 0) continue

        kwhConsommes += consommation
        dureeMillis += duree
    }

    if (dureeMillis == 0L) return null
    return kwhConsommes / (dureeMillis.toDouble() / MILLIS_PAR_JOUR)
}

/**
 * Crédit restant en kWh : dernier relevé + achats enregistrés après ce relevé.
 *
 * @return `null` tant qu'aucun relevé n'a été saisi.
 */
fun calculateRemainingKwh(
    releves: List<Releve>,
    achats: List<AchatCredit>
): Double? {
    val dernierReleve = releves.filter { it.estCoherent() }.maxWithOrNull(ORDRE_RELEVES) ?: return null
    val kwhAchetesDepuis = achats
        .filter { it.date > dernierReleve.date }
        .sumOf { it.kwh }
    return dernierReleve.kwhRestants + kwhAchetesDepuis
}

/**
 * R4 — Nombre de jours de crédit restants : `kWh restants / consommation moyenne`.
 *
 * Exemple : 42 kWh / 13 kWh par jour ≈ 3,23 jours.
 *
 * @return `0.0` si le crédit est épuisé, `null` si la consommation est nulle,
 * inconnue ou si les kWh restants sont incohérents.
 */
fun estimateRemainingDays(
    kwhRestants: Double,
    averageDailyConsumption: Double?
): Double? {
    if (!kwhRestants.isFinite() || kwhRestants < 0) return null
    if (kwhRestants == 0.0) return 0.0
    if (averageDailyConsumption == null ||
        !averageDailyConsumption.isFinite() ||
        averageDailyConsumption <= 0
    ) return null
    return kwhRestants / averageDailyConsumption
}

/**
 * R5 — État du crédit à partir des jours restants et du seuil d'alerte.
 *
 * - jours inconnus → [CreditState.UNKNOWN]
 * - jours <= 0 → [CreditState.CRITICAL]
 * - jours <= seuil → [CreditState.WARNING]
 * - jours > seuil → [CreditState.NORMAL]
 */
fun determineCreditState(
    joursRestants: Double?,
    seuilAlerteJours: Double
): CreditState = when {
    joursRestants == null || joursRestants.isNaN() -> CreditState.UNKNOWN
    joursRestants <= 0 -> CreditState.CRITICAL
    joursRestants <= seuilAlerteJours -> CreditState.WARNING
    else -> CreditState.NORMAL
}

/** Total des achats d'un mois civil ([mois] de 1 à 12). */
data class CoutMensuel(
    val annee: Int,
    val mois: Int,
    val totalGnf: Long
)

/**
 * Coût de l'électricité mois par mois, du mois le plus récent au plus ancien.
 * Seuls les mois qui contiennent au moins un achat apparaissent.
 */
fun calculateMonthlyCosts(
    achats: List<AchatCredit>,
    fuseauHoraire: TimeZone = TimeZone.getDefault()
): List<CoutMensuel> {
    val calendrier = Calendar.getInstance(fuseauHoraire)
    return achats
        .groupBy { achat ->
            calendrier.timeInMillis = achat.date
            calendrier.get(Calendar.YEAR) to calendrier.get(Calendar.MONTH) + 1
        }
        .map { (anneeMois, achatsDuMois) ->
            CoutMensuel(
                annee = anneeMois.first,
                mois = anneeMois.second,
                totalGnf = achatsDuMois.sumOf { it.montantGnf }
            )
        }
        .sortedWith(compareByDescending<CoutMensuel> { it.annee }.thenByDescending { it.mois })
}

/** Coût total en GNF des achats du mois civil qui contient [maintenant]. */
fun calculateMonthlyCost(
    achats: List<AchatCredit>,
    maintenant: Long,
    fuseauHoraire: TimeZone = TimeZone.getDefault()
): Long {
    val calendrier = Calendar.getInstance(fuseauHoraire).apply { timeInMillis = maintenant }
    val annee = calendrier.get(Calendar.YEAR)
    val mois = calendrier.get(Calendar.MONTH) + 1
    return calculateMonthlyCosts(achats, fuseauHoraire)
        .firstOrNull { it.annee == annee && it.mois == mois }
        ?.totalGnf
        ?: 0L
}
