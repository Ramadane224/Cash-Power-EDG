package com.odc.cashpoweredg.data.repository

import com.odc.cashpoweredg.data.dao.AchatCreditDao
import com.odc.cashpoweredg.data.dao.ParametresDao
import com.odc.cashpoweredg.data.dao.ReleveDao
import com.odc.cashpoweredg.data.entity.AchatCredit
import com.odc.cashpoweredg.data.entity.Parametres
import com.odc.cashpoweredg.data.entity.Releve
import kotlinx.coroutines.flow.Flow

class CashPowerRepositoryImpl(
    private val achatCreditDao: AchatCreditDao,
    private val releveDao: ReleveDao,
    private val parametresDao: ParametresDao
) : CashPowerRepository {

    override fun observeAchats(): Flow<List<AchatCredit>> =
        achatCreditDao.getHistorique(COMPTEUR_PAR_DEFAUT_ID)

    override fun observeReleves(): Flow<List<Releve>> =
        releveDao.getHistorique(COMPTEUR_PAR_DEFAUT_ID)

    override fun observeParametres(): Flow<Parametres?> =
        parametresDao.get()

    override suspend fun insertAchat(achat: AchatCredit) {
        achatCreditDao.insert(achat)
    }

    override suspend fun insertReleve(releve: Releve) {
        releveDao.insert(releve)
    }

    override suspend fun saveParametres(parametres: Parametres) {
        parametresDao.save(parametres)
    }

    companion object {
        private const val COMPTEUR_PAR_DEFAUT_ID = 1
    }
}
