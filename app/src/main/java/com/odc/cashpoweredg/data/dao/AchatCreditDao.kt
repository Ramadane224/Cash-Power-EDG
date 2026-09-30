package com.odc.cashpoweredg.data.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.odc.cashpoweredg.data.entity.AchatCredit
import kotlinx.coroutines.flow.Flow
@Dao
interface AchatCreditDao {
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insert(achat: AchatCredit)
@Update
suspend fun update(achat: AchatCredit)
@Delete
suspend fun delete(achat: AchatCredit)
@Query("SELECT * FROM achats_credit WHERE compteurId = :compteurId ORDER BY date DESC")
fun getHistorique(compteurId: Int): Flow<List<AchatCredit>>
@Query("SELECT SUM(kwh) FROM achats_credit WHERE compteurId = :compteurId")
suspend fun getTotalKwh(compteurId: Int): Double?
@Query("SELECT SUM(montantGnf) FROM achats_credit WHERE compteurId = :compteurId AND date >= :debutMois")
suspend fun getCoutMensuel(compteurId: Int, debutMois: Long): Double?
}
