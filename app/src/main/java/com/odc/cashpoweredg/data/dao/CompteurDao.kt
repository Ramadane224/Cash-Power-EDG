package com.odc.cashpoweredg.data.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.odc.cashpoweredg.data.entity.Compteur
import kotlinx.coroutines.flow.Flow
@Dao
interface CompteurDao {
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insert(compteur: Compteur)
@Query("SELECT * FROM compteurs LIMIT 1")
fun get(): Flow<Compteur?>
@Query("SELECT * FROM compteurs WHERE id = :id")
suspend fun getById(id: Int): Compteur?
}
