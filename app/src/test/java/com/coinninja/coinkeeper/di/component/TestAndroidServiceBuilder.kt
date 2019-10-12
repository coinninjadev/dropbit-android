package com.coinninja.coinkeeper.di.component

import com.coinninja.coinkeeper.cn.dropbit.DropBitService
import com.coinninja.coinkeeper.cn.service.CNFirebaseMessagingService
import com.coinninja.coinkeeper.cn.service.CNFirebaseMessagingServiceTest
import com.coinninja.coinkeeper.cn.service.CNGlobalMessagingService
import com.coinninja.coinkeeper.cn.wallet.service.CNWalletService
import com.coinninja.coinkeeper.cn.wallet.service.CNWalletServiceTest
import com.coinninja.coinkeeper.service.*
import com.coinninja.coinkeeper.service.blockchain.BlockChainService
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class TestAndroidServiceBuilder {

    @ContributesAndroidInjector
    internal abstract fun walletCreationIntentService(): WalletCreationIntentService

    @ContributesAndroidInjector(modules = [DeleteWalletServiceTest.DeleteWalletServiceTestModule::class])
    internal abstract fun deleteWalletService(): DeleteWalletService

    @ContributesAndroidInjector
    internal abstract fun contactLookupService(): ContactLookupService

    @ContributesAndroidInjector(modules = [WalletTransactionRetrieverServiceTest.TestWalletTransactionRetrieverServiceModule::class])
    internal abstract fun walletTransactionRetrieverService(): WalletTransactionRetrieverService

    @ContributesAndroidInjector(modules = [CNFirebaseMessagingServiceTest.CNFirebaseMessagingServiceTestModule::class])
    internal abstract fun cnFirebaseMessagingService(): CNFirebaseMessagingService

    @ContributesAndroidInjector(modules = [PushNotificationEndpointRegistrationServiceTest.PushNotificationEndpointRegistrationServiceTestModule::class])
    internal abstract fun pushNotificationEndpointRegistrationService(): PushNotificationEndpointRegistrationService

    @ContributesAndroidInjector
    internal abstract fun injectInviteService(): SaveInviteService

    @ContributesAndroidInjector(modules = [CNWalletServiceTest.TestCNWalletServiceModule::class])
    internal abstract fun cnWalletService(): CNWalletService

    @ContributesAndroidInjector
    internal abstract fun cnGlobalMessagingService(): CNGlobalMessagingService

    @ContributesAndroidInjector
    internal abstract fun blockChainService(): BlockChainService

    @ContributesAndroidInjector
    internal abstract fun injectTransactionService(): BroadcastTransactionService

    @ContributesAndroidInjector
    internal abstract fun syncDropBitService(): SyncDropBitService

    @ContributesAndroidInjector
    internal abstract fun btcBroadcastNotificationService(): BtcBroadcastNotificationService

    @ContributesAndroidInjector
    internal abstract fun fulfillSentInvitesService(): FulfillSentInvitesService

    @ContributesAndroidInjector
    internal abstract fun dropBitService(): DropBitService

}
