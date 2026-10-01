package com.odc.cashpoweredg.data.database
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.odc.cashpoweredg.data.dao.AchatCreditDao
import com.odc.cashpoweredg.data.dao.CompteurDao
import com.odc.cashpoweredg.data.dao.ParametresDao
import com.odc.cashpoweredg.data.dao.ReleveDao
import com.odc.cashpoweredg.data.entity.AchatCredit
import com.odc.cashpoweredg.data.entity.Compteur
import com.odc.cashpoweredg.data.entity.Parametres
import com.odc.cashpoweredg.data.entity.Releve
@Database(entities = [Compteur::class, AchatCredit::class, Releve::class, Parametres::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
abstract fun compteurDao(): CompteurDao
abstract fun achatCreditDao(): AchatCreditDao
abstract fun releveDao(): ReleveDao
abstract fun parametresDao(): ParametresDao
companion object {
@Volatile private var INSTANCE: AppDatabase? = null
fun getInstance(context: Context): AppDatabase {
return INSTANCE ?: synchronized(this) { Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "cashpower_db").build().also { INSTANCE = it } }
}
}
}
