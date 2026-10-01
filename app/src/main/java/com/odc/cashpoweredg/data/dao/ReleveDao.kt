package com.odc.cashpoweredg.data.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.odc.cashpoweredg.data.entity.Releve
import kotlinx.coroutines.flow.Flow
@Dao
interface ReleveDao {
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insert(releve: Releve)
@Delete
suspend fun delete(releve: Releve)
@Query("SELECT * FROM releves WHERE compteurId = :compteurId ORDER BY date DESC")
fun getHistorique(compteurId: Int): Flow<List<Releve>>
@Query("SELECT * FROM releves WHERE compteurId = :compteurId ORDER BY date DESC LIMIT 1")
suspend fun getDernier(compteurId: Int): Releve?
@Query("SELECT * FROM releves WHERE compteurId = :compteurId ORDER BY date DESC LIMIT 2")
suspend fun getDeuxDerniers(compteurId: Int): List<Releve>
}
