package com.odc.cashpoweredg.data.local.entity

/**
 * CONTRAT ATTENDU — à remplacer par l'entité Room d'Ismail (branche `feature/room-data`).
 *
 * Les champs reprennent exactement le modèle de données du plan d'équipe ;
 * seules les annotations Room (@Entity, @PrimaryKey) manquent.
 */
data class AchatCredit(
    val id: Int = 0,
    val compteurId: Int,
    val montantGnf: Long,
    val kwh: Double,
    val date: Long
)
