package com.odc.cashpoweredg.data.repository
import com.odc.cashpoweredg.data.database.AppDatabase
import com.odc.cashpoweredg.data.entity.AchatCredit
import com.odc.cashpoweredg.data.entity.Compteur
import com.odc.cashpoweredg.data.entity.Parametres
import com.odc.cashpoweredg.data.entity.Releve
import kotlinx.coroutines.flow.Flow
class CashPowerRepository(private val db: AppDatabase) {
suspend fun sauvegarderCompteur(compteur: Compteur) = db.compteurDao().insert(compteur)
fun getCompteur(): Flow<Compteur?> = db.compteurDao().get()
suspend fun ajouterAchat(achat: AchatCredit) = db.achatCreditDao().insert(achat)
suspend fun modifierAchat(achat: AchatCredit) = db.achatCreditDao().update(achat)
suspend fun supprimerAchat(achat: AchatCredit) = db.achatCreditDao().delete(achat)
fun getHistoriqueAchats(compteurId: Int): Flow<List<AchatCredit>> = db.achatCreditDao().getHistorique(compteurId)
suspend fun getTotalKwh(compteurId: Int): Double = db.achatCreditDao().getTotalKwh(compteurId) ?: 0.0
suspend fun getCoutMensuel(compteurId: Int, debutMois: Long): Double = db.achatCreditDao().getCoutMensuel(compteurId, debutMois) ?: 0.0
suspend fun ajouterReleve(releve: Releve) = db.releveDao().insert(releve)
suspend fun supprimerReleve(releve: Releve) = db.releveDao().delete(releve)
fun getHistoriqueReleves(compteurId: Int): Flow<List<Releve>> = db.releveDao().getHistorique(compteurId)
suspend fun getDernierReleve(compteurId: Int): Releve? = db.releveDao().getDernier(compteurId)
suspend fun sauvegarderParametres(p: Parametres) = db.parametresDao().save(p)
fun getParametres(): Flow<Parametres?> = db.parametresDao().get()
suspend fun getParametresOnce(): Parametres? = db.parametresDao().getOnce()
}
