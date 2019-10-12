package com.coinninja.coinkeeper.di.builder

import com.coinninja.coinkeeper.di.interfaces.ActivityScope
import com.coinninja.coinkeeper.ui.account.verify.UserAccountVerificationActivity
import com.coinninja.coinkeeper.ui.backup.BackupRecoveryWordsStartActivity
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.ui.base.BaseActivityModule
import com.coinninja.coinkeeper.ui.home.HomeActivity
import com.coinninja.coinkeeper.ui.home.HomeModule
import com.coinninja.coinkeeper.ui.lightning.broadcast.BroadcastLightningPaymentActivity
import com.coinninja.coinkeeper.ui.lightning.deposit.LightningDepositActivity
import com.coinninja.coinkeeper.ui.lightning.deposit.LightningDepositActivityModule
import com.coinninja.coinkeeper.ui.lightning.withdrawal.LightningWithdrawalActivity
import com.coinninja.coinkeeper.ui.lightning.withdrawal.LightningWithdrawalActivityModule
import com.coinninja.coinkeeper.ui.lightning.withdrawal.LightningWithdrawalBroadcastActivity
import com.coinninja.coinkeeper.ui.market.MarketChartModule
import com.coinninja.coinkeeper.ui.market.MarketScreenActivity
import com.coinninja.coinkeeper.ui.news.MarketNewsModule
import com.coinninja.coinkeeper.ui.payment.confirm.ConfirmPaymentActivity
import com.coinninja.coinkeeper.ui.payment.create.CreatePaymentActivity
import com.coinninja.coinkeeper.ui.payment.create.CreatePaymentModule
import com.coinninja.coinkeeper.ui.payment.invite.InviteContactActivity
import com.coinninja.coinkeeper.ui.payment.request.LndInvoiceRequestActivity
import com.coinninja.coinkeeper.ui.payment.request.PayRequestActivity
import com.coinninja.coinkeeper.ui.payment.request.PayRequestScreenModule
import com.coinninja.coinkeeper.ui.phone.verification.VerificationActivity
import com.coinninja.coinkeeper.ui.segwit.*
import com.coinninja.coinkeeper.ui.settings.AdjustableFeesActivity
import com.coinninja.coinkeeper.ui.settings.SettingsActivity
import com.coinninja.coinkeeper.ui.spending.BuyBitcoinActivity
import com.coinninja.coinkeeper.ui.spending.SpendBitcoinActivity
import com.coinninja.coinkeeper.ui.transaction.details.TransactionDetailsActivity
import com.coinninja.coinkeeper.view.activity.*
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AndroidActivityBuilder {

    @ActivityScope
    @ContributesAndroidInjector(modules = [HomeModule::class, BaseActivityModule::class])
    internal abstract fun homeActivity(): HomeActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class, MarketChartModule::class, MarketNewsModule::class])
    internal abstract fun marketScreenActivity(): MarketScreenActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun adjustableFeesActivity(): AdjustableFeesActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun signupSelectionActivity(): SignUpSelectionActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun spendBitcoinActivity(): SpendBitcoinActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun buyBitcoinActivity(): BuyBitcoinActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun userAccountVerificationActivity(): UserAccountVerificationActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun baseActivity(): BaseActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun backupActivity(): BackupActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun backupRecoveryWordsStartActivity(): BackupRecoveryWordsStartActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun broadcastActivity(): BroadcastActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun authenticateActivity(): AuthenticateActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun authorizedActionActivity(): AuthorizedActionActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun coinKeeperSupportActivity(): CoinKeeperSupportActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun createPinActivity(): CreatePinActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun inviteSendActivity(): InviteSendActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun licensesActivity(): LicensesActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun pickContactActivity(): PickUserActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun qrScanActivity(): QrScanActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun restoreWalletActivity(): RestoreWalletActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun recoverWalletActivity(): RecoverWalletActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun settingsActivity(): SettingsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun splashActivity(): SplashActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun startActivity(): StartActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun trainingActivity(): TrainingActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun verifyPhoneNumberActivity(): VerificationActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun verifyPhoneVerificationCodeActivity(): VerifyPhoneVerificationCodeActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun verifyRecoverywordsActivity(): VerifyRecoverywordsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun transactionDetailsActivity(): TransactionDetailsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class, LightningDepositActivityModule::class])
    internal abstract fun lightningDepositActivity(): LightningDepositActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class, LightningWithdrawalActivityModule::class])
    internal abstract fun lightningWithdrawalActivity(): LightningWithdrawalActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class, LightningWithdrawalActivityModule::class])
    internal abstract fun lightningWithdrawalCompletedActivity(): LightningWithdrawalBroadcastActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class, UpgradeToSegwitActivityModule::class])
    internal abstract fun upgradeToSegwitActivity(): UpgradeToSegwitActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class, PerformSegwitUpgradeActivityModule::class])
    internal abstract fun performSegwitUpgradeActivity(): PerformSegwitUpgradeActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun upgradeToSegwitCompleteActivity(): UpgradeToSegwitCompleteActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class, PayRequestScreenModule::class])
    internal abstract fun payRequestActivity(): PayRequestActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class, PayRequestScreenModule::class])
    internal abstract fun lndInvoiceRequestActivity(): LndInvoiceRequestActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class, CreatePaymentModule::class])
    internal abstract fun createPaymentActivity(): CreatePaymentActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class, CreatePaymentModule::class])
    internal abstract fun confirmPaymentActivity(): ConfirmPaymentActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class, CreatePaymentModule::class])
    internal abstract fun broadcastLightningPaymentActivity(): BroadcastLightningPaymentActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BaseActivityModule::class, CreatePaymentModule::class])
    internal abstract fun inviteContactActivity(): InviteContactActivity
}
