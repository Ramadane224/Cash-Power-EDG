package com.odc.cashpoweredg.data.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.odc.cashpoweredg.data.entity.Parametres
import kotlinx.coroutines.flow.Flow
@Dao
interface ParametresDao {
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun save(parametres: Parametres)
@Query("SELECT * FROM parametres WHERE id = 1")
fun get(): Flow<Parametres?>
@Query("SELECT * FROM parametres WHERE id = 1")
suspend fun getOnce(): Parametres?
}
