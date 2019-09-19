package com.coinninja.coinkeeper.ui.segwit

import androidx.lifecycle.ViewModelProviders
import app.dropbit.annotations.Mockable
import app.dropbit.commons.util.DateUtil
import com.coinninja.coinkeeper.bitcoin.TransactionBroadcaster
import com.coinninja.coinkeeper.cn.account.RemoteAddressCache
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager
import com.coinninja.coinkeeper.cn.wallet.HDWalletWrapper
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.util.android.ServiceWorkUtil
import javax.inject.Inject

@Mockable
class WalletUpgradeModelProvider @Inject constructor(
        internal val cnClient: SignedCoinKeeperApiClient,
        internal val cnWalletManager: CNWalletManager,
        internal val syncWalletManager: SyncWalletManager,
        internal val dropbitAccountHelper: DropbitAccountHelper,
        internal val remoteAddressCache: RemoteAddressCache,
        internal val transactionBroadcaster: TransactionBroadcaster,
        internal val hdWalletWrapper: HDWalletWrapper,
        internal val serviceWorkUtil: ServiceWorkUtil
) {

    fun provide(activity: BaseActivity): WalletUpgradeViewModel {
        return bind(ViewModelProviders.of(activity)[WalletUpgradeViewModel::class.java])
    }

    private fun bind(walletUpgradeViewModel: WalletUpgradeViewModel): WalletUpgradeViewModel {
        walletUpgradeViewModel.cnWalletManager = cnWalletManager
        walletUpgradeViewModel.dropbitAccountHelper = dropbitAccountHelper
        walletUpgradeViewModel.syncWalletManager = syncWalletManager
        walletUpgradeViewModel.remoteAddressCache = remoteAddressCache
        walletUpgradeViewModel.transactionBroadcaster = transactionBroadcaster
        walletUpgradeViewModel.hdWalletWrapper = hdWalletWrapper
        walletUpgradeViewModel.cnClient = cnClient
        walletUpgradeViewModel.serviceWorkUtil = serviceWorkUtil
        walletUpgradeViewModel.dateUtil = DateUtil()
        return walletUpgradeViewModel
    }

}