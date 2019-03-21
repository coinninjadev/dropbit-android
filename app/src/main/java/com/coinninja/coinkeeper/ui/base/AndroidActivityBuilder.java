package com.coinninja.coinkeeper.ui.base;

import com.coinninja.coinkeeper.ui.backup.BackupRecoveryWordsStartActivity;
import com.coinninja.coinkeeper.ui.phone.verification.VerifyPhoneNumberActivity;
import com.coinninja.coinkeeper.ui.settings.SettingsActivity;
import com.coinninja.coinkeeper.ui.transaction.details.TransactionDetailModule;
import com.coinninja.coinkeeper.ui.transaction.details.TransactionDetailsActivity;
import com.coinninja.coinkeeper.view.activity.AuthenticateActivity;
import com.coinninja.coinkeeper.view.activity.AuthorizedActionActivity;
import com.coinninja.coinkeeper.view.activity.BackupActivity;
import com.coinninja.coinkeeper.view.activity.BroadcastActivity;
import com.coinninja.coinkeeper.view.activity.CoinKeeperSupportActivity;
import com.coinninja.coinkeeper.view.activity.CreatePinActivity;
import com.coinninja.coinkeeper.view.activity.InviteSendActivity;
import com.coinninja.coinkeeper.view.activity.LicensesActivity;
import com.coinninja.coinkeeper.view.activity.PickContactActivity;
import com.coinninja.coinkeeper.view.activity.QrScanActivity;
import com.coinninja.coinkeeper.view.activity.RecoverWalletActivity;
import com.coinninja.coinkeeper.view.activity.RestoreWalletActivity;
import com.coinninja.coinkeeper.view.activity.SplashActivity;
import com.coinninja.coinkeeper.view.activity.StartActivity;
import com.coinninja.coinkeeper.view.activity.TrainingActivity;
import com.coinninja.coinkeeper.view.activity.TransactionHistoryActivity;
import com.coinninja.coinkeeper.view.activity.VerifyPhoneVerificationCodeActivity;
import com.coinninja.coinkeeper.view.activity.VerifyRecoverywordsActivity;
import com.coinninja.coinkeeper.view.activity.base.BalanceBarActivity;
import com.coinninja.coinkeeper.view.activity.base.MessengerActivity;
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module()
public abstract class AndroidActivityBuilder {

    @ActivityScope
    @ContributesAndroidInjector
    abstract BaseActivity baseActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract BackupActivity backupActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract BackupRecoveryWordsStartActivity backupRecoveryWordsStartActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract BalanceBarActivity balanceBarActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract BroadcastActivity broadcastActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract AuthenticateActivity authenticateActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract AuthorizedActionActivity authorizedActionActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract CoinKeeperSupportActivity coinKeeperSupportActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract CreatePinActivity createPinActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract InviteSendActivity inviteSendActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract LicensesActivity licensesActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract PickContactActivity pickContactActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract MessengerActivity messagegerActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract QrScanActivity qrScanActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract RestoreWalletActivity restoreWalletActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract RecoverWalletActivity recoverWalletActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract SecuredActivity securedActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract SettingsActivity settingsActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract SplashActivity splashActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract StartActivity startActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract TransactionHistoryActivity transactionHistoryActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract TrainingActivity trainingActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract VerifyPhoneNumberActivity verifyPhoneNumberActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract VerifyPhoneVerificationCodeActivity verifyPhoneVerificationCodeActivity();

    @ActivityScope
    @ContributesAndroidInjector
    abstract VerifyRecoverywordsActivity verifyRecoverywordsActivity();

    @ActivityScope
    @ContributesAndroidInjector(modules = TransactionDetailModule.class)
    abstract TransactionDetailsActivity transactionDetailsActivity();

}
