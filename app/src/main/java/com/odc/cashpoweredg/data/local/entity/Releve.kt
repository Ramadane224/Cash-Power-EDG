package com.odc.cashpoweredg.data.local.entity

/**
 * CONTRAT ATTENDU — à remplacer par l'entité Room d'Ismail (branche `feature/room-data`).
 *
 * Les champs reprennent exactement le modèle de données du plan d'équipe ;
 * seules les annotations Room (@Entity, @PrimaryKey) manquent.
 */
data class Releve(
    val id: Int = 0,
    val compteurId: Int,
    val kwhRestants: Double,
    val date: Long
)
