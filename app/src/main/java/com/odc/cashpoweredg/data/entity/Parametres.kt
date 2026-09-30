package com.odc.cashpoweredg.data.entity
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "parametres")
data class Parametres(@PrimaryKey val id: Int = 1, val tarifParKwh: Double = 1500.0, val seuilAlerteJours: Int = 2, val nomCompteur: String = "Mon compteur")
