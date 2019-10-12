package com.coinninja.coinkeeper.di.builder

import com.coinninja.coinkeeper.cn.dropbit.DropBitService
import com.coinninja.coinkeeper.cn.service.CNFirebaseMessagingService
import com.coinninja.coinkeeper.cn.service.CNGlobalMessagingService
import com.coinninja.coinkeeper.cn.wallet.service.CNWalletService
import com.coinninja.coinkeeper.cn.wallet.service.CnWalletServiceModule
import com.coinninja.coinkeeper.service.*
import com.coinninja.coinkeeper.service.blockchain.BlockChainService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AndroidServiceBuilder {

    @ContributesAndroidInjector
    internal abstract fun walletCreationIntentService(): WalletCreationIntentService

    @ContributesAndroidInjector
    internal abstract fun deleteWalletService(): DeleteWalletService

    @ContributesAndroidInjector
    internal abstract fun contactLookupService(): ContactLookupService

    @ContributesAndroidInjector
    internal abstract fun walletTransactionRetrieverService(): WalletTransactionRetrieverService

    @ContributesAndroidInjector
    internal abstract fun cnFirebaseMessagingService(): CNFirebaseMessagingService

    @ContributesAndroidInjector
    internal abstract fun pushNotificationEndpointRegistrationService(): PushNotificationEndpointRegistrationService

    @ContributesAndroidInjector
    internal abstract fun injectInviteService(): SaveInviteService

    @ContributesAndroidInjector(modules = [CnWalletServiceModule::class])
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
