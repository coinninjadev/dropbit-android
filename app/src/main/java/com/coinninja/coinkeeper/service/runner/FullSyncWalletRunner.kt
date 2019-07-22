package com.coinninja.coinkeeper.service.runner

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.cn.account.RemoteAddressCache
import com.coinninja.coinkeeper.cn.dropbit.DropBitMeServiceManager
import com.coinninja.coinkeeper.cn.service.runner.AccountDeverificationServiceRunner
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.receiver.WalletSyncCompletedReceiver
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil
import org.greenrobot.greendao.DaoException
import javax.inject.Inject

@Mockable
class FullSyncWalletRunner @Inject
internal constructor(internal val cnWalletManager: CNWalletManager,
                     internal val accountDeverificationServiceRunner: AccountDeverificationServiceRunner,
                     internal val walletRegistrationRunner: WalletRegistraionRunner,
                     internal val currentBTCStateRunner: CurrentBTCStateRunner,
                     internal val syncRunnable: SyncRunnable,
                     internal val transactionConfirmationUpdateRunner: TransactionConfirmationUpdateRunner,
                     internal val failedBroadcastCleaner: FailedBroadcastCleaner,
                     internal val syncIncomingInvitesRunner: SyncIncomingInvitesRunner,
                     internal val fulfillSentInvitesRunner: FulfillSentInvitesRunner,
                     internal val receivedInvitesStatusRunner: ReceivedInvitesStatusRunner,
                     internal val negativeBalanceRunner: NegativeBalanceRunner,
                     internal val dropbitAccountHelper: DropbitAccountHelper,
                     internal val localBroadCastUtil: LocalBroadCastUtil,
                     internal val remoteAddressCache: RemoteAddressCache,
                     internal val dropBitMeServiceManager: DropBitMeServiceManager
) : Runnable {

    override fun run() {
        if (!cnWalletManager.hasWallet())
            return

        try {
            accountDeverificationServiceRunner.run()
            walletRegistrationRunner.run()
            currentBTCStateRunner.run()
            dropBitMeServiceManager.syncIdentities()

            syncDropbits()

            syncRunnable.run()
            transactionConfirmationUpdateRunner.run()
            failedBroadcastCleaner.run()
            cnWalletManager.updateBalances()
        } catch (e: DaoException) {
            e.printStackTrace()
        }

        localBroadCastUtil.sendGlobalBroadcast(WalletSyncCompletedReceiver::class.java,
                DropbitIntents.ACTION_WALLET_SYNC_COMPLETE)
    }

    private fun syncDropbits() {
        if (!dropbitAccountHelper.hasVerifiedAccount) return

        syncIncomingInvitesRunner.run()
        receivedInvitesStatusRunner.run()
        negativeBalanceRunner.run()
        fulfillSentInvitesRunner.run()
        remoteAddressCache.cacheAddresses()
    }
}
