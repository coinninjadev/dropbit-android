package com.coinninja.coinkeeper.model.query

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.db.Wallet
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager
import javax.inject.Inject

@Mockable
class WalletQueryManager @Inject internal constructor(private val daoSessionManager: DaoSessionManager) {

    val wallet: Wallet? get() = daoSessionManager.qeuryForWallet().orderAsc().limit(1).unique()
}
