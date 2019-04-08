package com.coinninja.coinkeeper.di.builder;

import com.coinninja.coinkeeper.receiver.ApplicationStartedReceiver;
import com.coinninja.coinkeeper.receiver.AuthenticationCompleteReceiver;
import com.coinninja.coinkeeper.receiver.DeviceRebootBootCompletedReceiver;
import com.coinninja.coinkeeper.receiver.StartupCompleteReceiver;
import com.coinninja.coinkeeper.receiver.WalletCreatedBroadCastReceiver;
import com.coinninja.coinkeeper.receiver.WalletRegistrationCompleteReceiver;
import com.coinninja.coinkeeper.receiver.WalletSyncCompletedReceiver;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class AndroidBroadcastReceiverBuilder {

    @ContributesAndroidInjector
    abstract ApplicationStartedReceiver applicationStartedReceiver();

    @ContributesAndroidInjector
    abstract StartupCompleteReceiver startupCompleteReceiver();

    @ContributesAndroidInjector
    abstract WalletCreatedBroadCastReceiver walletCreatedBroadCastReceiver();

    @ContributesAndroidInjector
    abstract WalletRegistrationCompleteReceiver walletRegistrationCompleteReceiver();

    @ContributesAndroidInjector
    abstract WalletSyncCompletedReceiver walletSyncCompletedReceiver();

    @ContributesAndroidInjector
    abstract DeviceRebootBootCompletedReceiver deviceRebootBootCompletedReceiver();

    @ContributesAndroidInjector
    abstract AuthenticationCompleteReceiver authenticationCompleteReceiver();
}
