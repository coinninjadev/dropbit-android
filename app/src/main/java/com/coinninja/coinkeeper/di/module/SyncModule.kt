package com.coinninja.coinkeeper.di.module

import app.coinninja.cn.thunderdome.repository.ThunderDomeRepository
import com.coinninja.coinkeeper.cn.account.RemoteAddressCache
import com.coinninja.coinkeeper.cn.dropbit.DropBitMeServiceManager
import com.coinninja.coinkeeper.cn.service.runner.AccountDeverificationServiceRunner
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.service.runner.*
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil
import dagger.Module
import dagger.Provides

@Module
class SyncModule {

    @Provides
    fun provideFullSyncRunner(
            cnWalletManager: CNWalletManager,
            accountDeverificationServiceRunner: AccountDeverificationServiceRunner,
            walletRegistrationRunner: WalletRegistrationRunner,
            currentBTCStateRunner: CurrentBTCStateRunner,
            syncRunnable: SyncRunnable,
            transactionConfirmationUpdateRunner: TransactionConfirmationUpdateRunner,
            failedBroadcastCleaner: FailedBroadcastCleaner,
            syncIncomingInvitesRunner: SyncIncomingInvitesRunner,
            fulfillSentInvitesRunner: FulfillSentInvitesRunner,
            receivedInvitesStatusRunner: ReceivedInvitesStatusRunner,
            negativeBalanceRunner: NegativeBalanceRunner,
            dropbitAccountHelper: DropbitAccountHelper,
            localBroadCastUtil: LocalBroadCastUtil,
            remoteAddressCache: RemoteAddressCache,
            dropBitMeServiceManager: DropBitMeServiceManager,
            thunderDomeRepository: ThunderDomeRepository
    ): FullSyncWalletRunner = FullSyncWalletRunner(
            cnWalletManager, accountDeverificationServiceRunner, walletRegistrationRunner,
            currentBTCStateRunner, syncRunnable, transactionConfirmationUpdateRunner,
            failedBroadcastCleaner, syncIncomingInvitesRunner, fulfillSentInvitesRunner,
            receivedInvitesStatusRunner, negativeBalanceRunner, dropbitAccountHelper,
            localBroadCastUtil, remoteAddressCache, dropBitMeServiceManager, thunderDomeRepository
    )
}
