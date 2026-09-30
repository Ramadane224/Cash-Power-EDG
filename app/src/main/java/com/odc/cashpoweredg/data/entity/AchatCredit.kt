package com.odc.cashpoweredg.data.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
@Entity(tableName = "achats_credit", foreignKeys = [ForeignKey(entity = Compteur::class, parentColumns = ["id"], childColumns = ["compteurId"], onDelete = ForeignKey.CASCADE)], indices = [Index("compteurId")])
data class AchatCredit(@PrimaryKey(autoGenerate = true) val id: Int = 0, val compteurId: Int, val montantGnf: Double, val kwh: Double, val date: Long = System.currentTimeMillis())
