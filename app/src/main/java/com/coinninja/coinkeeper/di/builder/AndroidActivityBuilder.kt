package com.coinninja.coinkeeper.di.builder

import com.coinninja.coinkeeper.di.interfaces.ActivityScope
import com.coinninja.coinkeeper.service.tasks.CoinNinjaUserViewModel
import com.coinninja.coinkeeper.ui.account.verify.UserAccountVerificationActivity
import com.coinninja.coinkeeper.ui.backup.BackupRecoveryWordsStartActivity
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.ui.home.HomeActivity
import com.coinninja.coinkeeper.ui.home.HomeModule
import com.coinninja.coinkeeper.ui.market.MarketScreenActivity
import com.coinninja.coinkeeper.ui.phone.verification.VerificationActivity
import com.coinninja.coinkeeper.ui.settings.AdjustableFeesActivity
import com.coinninja.coinkeeper.ui.settings.SettingsActivity
import com.coinninja.coinkeeper.ui.spending.BuyBitcoinActivity
import com.coinninja.coinkeeper.ui.spending.SpendBitcoinActivity
import com.coinninja.coinkeeper.ui.transaction.details.TransactionDetailsActivity
import com.coinninja.coinkeeper.util.TwitterUtil
import com.coinninja.coinkeeper.view.activity.*
import com.coinninja.coinkeeper.view.activity.base.BalanceBarActivity
import com.coinninja.coinkeeper.view.activity.base.MessengerActivity
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AndroidActivityBuilder {

    @ActivityScope
    @ContributesAndroidInjector(modules = [HomeModule::class])
    internal abstract fun homeActivity(): HomeActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    internal abstract fun marketScreenActivity(): MarketScreenActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun adjustableFeesActivity(): AdjustableFeesActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun coinNinjaUserViewModel(): CoinNinjaUserViewModel

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun signupSelectionActivity(): SignUpSelectionActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun twitterUtil(): TwitterUtil

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun spendBitcoinActivity(): SpendBitcoinActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun buyBitcoinActivity(): BuyBitcoinActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun userAccountVerificationActivity(): UserAccountVerificationActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun baseActivity(): BaseActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun backupActivity(): BackupActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun backupRecoveryWordsStartActivity(): BackupRecoveryWordsStartActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun balanceBarActivity(): BalanceBarActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun broadcastActivity(): BroadcastActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun authenticateActivity(): AuthenticateActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun authorizedActionActivity(): AuthorizedActionActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun coinKeeperSupportActivity(): CoinKeeperSupportActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun createPinActivity(): CreatePinActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun inviteSendActivity(): InviteSendActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun licensesActivity(): LicensesActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun pickContactActivity(): PickUserActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun messagegerActivity(): MessengerActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun qrScanActivity(): QrScanActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun restoreWalletActivity(): RestoreWalletActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun recoverWalletActivity(): RecoverWalletActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun securedActivity(): SecuredActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun settingsActivity(): SettingsActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun splashActivity(): SplashActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun startActivity(): StartActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun trainingActivity(): TrainingActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun verifyPhoneNumberActivity(): VerificationActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun verifyPhoneVerificationCodeActivity(): VerifyPhoneVerificationCodeActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun verifyRecoverywordsActivity(): VerifyRecoverywordsActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun transactionDetailsActivity(): TransactionDetailsActivity

}
