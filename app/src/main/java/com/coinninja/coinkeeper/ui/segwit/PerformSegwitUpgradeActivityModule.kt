package com.coinninja.coinkeeper.ui.segwit

import com.coinninja.coinkeeper.bitcoin.TransactionBroadcaster
import com.coinninja.coinkeeper.cn.account.RemoteAddressCache
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager
import com.coinninja.coinkeeper.cn.wallet.HDWalletWrapper
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.android.ServiceWorkUtil
import dagger.Module
import dagger.Provides

@Module
class PerformSegwitUpgradeActivityModule {
    @Provides
    fun walletUpgradeModelProvider(
            cnClient: SignedCoinKeeperApiClient,
            cnWalletManager: CNWalletManager,
            syncWalletManager: SyncWalletManager,
            dropbitAccountHelper: DropbitAccountHelper,
            remoteAddressCache: RemoteAddressCache,
            transactionBroadcaster: TransactionBroadcaster,
            hdWalletWrapper: HDWalletWrapper,
            serviceWorkUtil: ServiceWorkUtil,
            analytics: Analytics
    )
            : WalletUpgradeModelProvider = WalletUpgradeModelProvider(
            cnClient,
            cnWalletManager,
            syncWalletManager,
            dropbitAccountHelper,
            remoteAddressCache,
            transactionBroadcaster,
            hdWalletWrapper,
            serviceWorkUtil,
            analytics
    )

}
