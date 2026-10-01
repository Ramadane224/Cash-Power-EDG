package com.odc.cashpoweredg.data.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
@Entity(tableName = "releves", foreignKeys = [ForeignKey(entity = Compteur::class, parentColumns = ["id"], childColumns = ["compteurId"], onDelete = ForeignKey.CASCADE)], indices = [Index("compteurId")])
data class Releve(@PrimaryKey(autoGenerate = true) val id: Int = 0, val compteurId: Int, val kwhRestants: Double, val date: Long = System.currentTimeMillis())
