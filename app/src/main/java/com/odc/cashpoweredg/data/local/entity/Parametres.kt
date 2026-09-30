package com.odc.cashpoweredg.data.local.entity

/**
 * CONTRAT ATTENDU — à remplacer par l'entité Room d'Ismail (branche `feature/room-data`).
 *
 * Les champs reprennent exactement le modèle de données du plan d'équipe ;
 * seules les annotations Room (@Entity, @PrimaryKey) manquent.
 */
data class Parametres(
    val id: Int = 1,
    val tarifKwhGnf: Double,
    val seuilAlerteJours: Double,
    val nomCompteur: String
)
