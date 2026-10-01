package com.odc.cashpoweredg.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parametres")
data class Parametres(
    @PrimaryKey
    val id: Int = 1,
    val tarifKwhGnf: Double = 1500.0,
    val seuilAlerteJours: Double = 2.0,
    val nomCompteur: String = "Compteur Maison"
)
