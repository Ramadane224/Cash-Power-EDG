package com.odc.cashpoweredg.data.entity
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "compteurs")
data class Compteur(
     @PrimaryKey(autoGenerate = true) val id: Int = 0,
     val nom: String,
     val numero: String
)
