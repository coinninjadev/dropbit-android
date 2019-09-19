package com.coinninja.coinkeeper.model.query

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.db.Wallet
import com.coinninja.coinkeeper.model.db.WalletDao
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager
import javax.inject.Inject

@Mockable
class WalletQueryManager @Inject internal constructor(private val daoSessionManager: DaoSessionManager) {

    val primaryWallet: Wallet? get() = daoSessionManager.queryForWallet().orderAsc(WalletDao.Properties.Id).limit(1).unique()
    val legacyWallet: Wallet?
        get() = daoSessionManager.queryForWallet().where(
                WalletDao.Properties.Purpose.eq(49)
        ).orderDesc(WalletDao.Properties.Id).limit(1).unique()
    val segwitWallet: Wallet?
        get() = daoSessionManager.queryForWallet().where(
                WalletDao.Properties.Purpose.eq(84)
        ).orderAsc().limit(1).unique()
}
