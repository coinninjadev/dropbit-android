package com.coinninja.coinkeeper.di.builder;

import com.coinninja.coinkeeper.cn.dropbit.DropBitService;
import com.coinninja.coinkeeper.cn.service.CNFirebaseMessagingService;
import com.coinninja.coinkeeper.cn.service.CNGlobalMessagingService;
import com.coinninja.coinkeeper.cn.wallet.service.CNWalletAddressRequestService;
import com.coinninja.coinkeeper.cn.wallet.service.CNWalletService;
import com.coinninja.coinkeeper.service.BroadcastTransactionService;
import com.coinninja.coinkeeper.service.BtcBroadcastNotificationService;
import com.coinninja.coinkeeper.service.ContactLookupService;
import com.coinninja.coinkeeper.service.DeleteWalletService;
import com.coinninja.coinkeeper.service.DeverifyAccountService;
import com.coinninja.coinkeeper.service.DeviceRegistrationService;
import com.coinninja.coinkeeper.service.DropbitServicePatchService;
import com.coinninja.coinkeeper.service.FulfillSentInvitesService;
import com.coinninja.coinkeeper.service.PushNotificationEndpointRegistrationService;
import com.coinninja.coinkeeper.service.RegisterUsersPhoneService;
import com.coinninja.coinkeeper.service.ResendPhoneVerificationService;
import com.coinninja.coinkeeper.service.SaveInviteService;
import com.coinninja.coinkeeper.service.SyncDropBitService;
import com.coinninja.coinkeeper.service.UserPhoneConfirmationService;
import com.coinninja.coinkeeper.service.WalletCreationIntentService;
import com.coinninja.coinkeeper.service.WalletTransactionRetrieverService;
import com.coinninja.coinkeeper.service.blockchain.BlockChainService;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class AndroidServiceBuilder {
    @ContributesAndroidInjector
    abstract ResendPhoneVerificationService resendPhoneVerificationService();

    @ContributesAndroidInjector
    abstract RegisterUsersPhoneService registerUsersPhoneService();

    @ContributesAndroidInjector
    abstract WalletCreationIntentService walletCreationIntentService();

    @ContributesAndroidInjector
    abstract DeleteWalletService deleteWalletService();

    @ContributesAndroidInjector
    abstract ContactLookupService contactLookupService();

    @ContributesAndroidInjector
    abstract WalletTransactionRetrieverService walletTransactionRetrieverService();

    @ContributesAndroidInjector()
    abstract DeviceRegistrationService deviceRegistrationService();

    @ContributesAndroidInjector()
    abstract CNFirebaseMessagingService cnFirebaseMessagingService();

    @ContributesAndroidInjector()
    abstract PushNotificationEndpointRegistrationService pushNotificationEndpointRegistrationService();

    @ContributesAndroidInjector()
    abstract SaveInviteService injectInviteService();

    @ContributesAndroidInjector()
    abstract CNWalletService cnWalletService();

    @ContributesAndroidInjector()
    abstract CNGlobalMessagingService cnGlobalMessagingService();

    @ContributesAndroidInjector()
    abstract DropbitServicePatchService dropbitServicePatchService();

    @ContributesAndroidInjector()
    abstract UserPhoneConfirmationService userPhoneConfirmationService();

    @ContributesAndroidInjector()
    abstract BlockChainService blockChainService();

    @ContributesAndroidInjector()
    abstract BroadcastTransactionService injectTransactionService();

    @ContributesAndroidInjector()
    abstract SyncDropBitService syncDropBitService();

    @ContributesAndroidInjector()
    abstract BtcBroadcastNotificationService btcBroadcastNotificationService();

    @ContributesAndroidInjector()
    abstract FulfillSentInvitesService fulfillSentInvitesService();

    @ContributesAndroidInjector()
    abstract DeverifyAccountService deverifyAccountService();

    @ContributesAndroidInjector()
    abstract DropBitService dropBitService();

    @ContributesAndroidInjector
    abstract CNWalletAddressRequestService walletAddressRequestService();

}
