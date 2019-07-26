package com.coinninja.coinkeeper.di.module

import com.coinninja.coinkeeper.receiver.*
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class TestAndroidReceiverBuilder {
    @ContributesAndroidInjector
    internal abstract fun applicationStartedReceiver(): ApplicationStartedReceiver

    @ContributesAndroidInjector
    internal abstract fun startupCompleteReceiver(): StartupCompleteReceiver

    @ContributesAndroidInjector()
    internal abstract fun walletCreatedBroadCastReceiver(): WalletCreatedBroadCastReceiver

    @ContributesAndroidInjector()
    internal abstract fun walletRegistrationCompleteReceiver(): WalletRegistrationCompleteReceiver

    @ContributesAndroidInjector
    internal abstract fun walletSyncCompletedReceiver(): WalletSyncCompletedReceiver

    @ContributesAndroidInjector
    internal abstract fun deviceRebootBootCompletedReceiver(): DeviceRebootBootCompletedReceiver

    @ContributesAndroidInjector
    internal abstract fun authenticationCompleteReceiver(): AuthenticationCompleteReceiver
}
