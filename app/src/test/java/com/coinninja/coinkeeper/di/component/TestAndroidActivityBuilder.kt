package com.coinninja.coinkeeper.di.component

import com.coinninja.coinkeeper.di.interfaces.ActivityScope
import com.coinninja.coinkeeper.di.module.TestHomeModule
import com.coinninja.coinkeeper.service.tasks.CoinNinjaUserViewModel
import com.coinninja.coinkeeper.ui.account.verify.UserAccountVerificationActivity
import com.coinninja.coinkeeper.ui.backup.BackupRecoveryWordsStartActivity
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.ui.base.TestBaseActivityModule
import com.coinninja.coinkeeper.ui.home.HomeActivity
import com.coinninja.coinkeeper.ui.lightning.deposit.LightningDepositActivity
import com.coinninja.coinkeeper.ui.lightning.deposit.LightningDepositActivityTest
import com.coinninja.coinkeeper.ui.lightning.withdrawal.LightningWithdrawalActivity
import com.coinninja.coinkeeper.ui.lightning.withdrawal.LightningWithdrawalActivityTest
import com.coinninja.coinkeeper.ui.lightning.withdrawal.LightningWithdrawalBroadcastActivity
import com.coinninja.coinkeeper.ui.lightning.withdrawal.LightningWithdrawalBroadcastActivityTest
import com.coinninja.coinkeeper.ui.market.MarketScreenActivity
import com.coinninja.coinkeeper.ui.market.TestMarketChartModule
import com.coinninja.coinkeeper.ui.news.TestMarketNewsModule
import com.coinninja.coinkeeper.ui.payment.confirm.ConfirmPaymentActivity
import com.coinninja.coinkeeper.ui.payment.confirm.ConfirmPaymentActivityTest
import com.coinninja.coinkeeper.ui.payment.create.CreatePaymentActivity
import com.coinninja.coinkeeper.ui.payment.create.CreatePaymentActivityTest
import com.coinninja.coinkeeper.ui.payment.request.LndInvoiceRequestActivity
import com.coinninja.coinkeeper.ui.payment.request.LndInvoiceRequestActivityTest
import com.coinninja.coinkeeper.ui.payment.request.PayRequestActivity
import com.coinninja.coinkeeper.ui.payment.request.PayRequestActivityTest
import com.coinninja.coinkeeper.ui.phone.verification.VerificationActivity
import com.coinninja.coinkeeper.ui.segwit.*
import com.coinninja.coinkeeper.ui.settings.AdjustableFeesActivity
import com.coinninja.coinkeeper.ui.settings.AdjustableFeesActivityTest
import com.coinninja.coinkeeper.ui.settings.SettingsActivity
import com.coinninja.coinkeeper.ui.spending.BuyBitcoinActivity
import com.coinninja.coinkeeper.ui.spending.SpendBitcoinActivity
import com.coinninja.coinkeeper.ui.transaction.details.TransactionDetailsActivity
import com.coinninja.coinkeeper.view.activity.*
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class TestAndroidActivityBuilder {

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestHomeModule::class, TestBaseActivityModule::class])
    internal abstract fun homeActivity(): HomeActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [AdjustableFeesActivityTest.TestAdjustableFeesActivityModule::class, TestBaseActivityModule::class])
    internal abstract fun adjustableFeesActivity(): AdjustableFeesActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class])
    internal abstract fun coinNinjaUserViewModel(): CoinNinjaUserViewModel

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class])
    internal abstract fun signupSelectionActivity(): SignUpSelectionActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class])
    internal abstract fun spendBitcoinActivity(): SpendBitcoinActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class])
    internal abstract fun buyBitcoinActivity(): BuyBitcoinActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class])
    internal abstract fun userAccountVerificationActivity(): UserAccountVerificationActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class])
    internal abstract fun baseActivity(): BaseActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class])
    internal abstract fun backupActivity(): BackupActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class])
    internal abstract fun backupRecoveryWordsStartActivity(): BackupRecoveryWordsStartActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class, BroadcastActivityTest.BroadcastActivityTestModule::class])
    internal abstract fun broadcastActivity(): BroadcastActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class])
    internal abstract fun authenticateActivity(): AuthenticateActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class])
    internal abstract fun authorizedActionActivity(): AuthorizedActionActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class])
    internal abstract fun coinKeeperSupportActivity(): CoinKeeperSupportActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class])
    internal abstract fun createPinActivity(): CreatePinActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class])
    internal abstract fun inviteSendActivity(): InviteSendActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class])
    internal abstract fun licensesActivity(): LicensesActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class])
    internal abstract fun pickContactActivity(): PickUserActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class])
    internal abstract fun qrScanActivity(): QrScanActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class])
    internal abstract fun restoreWalletActivity(): RestoreWalletActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class])
    internal abstract fun recoverWalletActivity(): RecoverWalletActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class])
    internal abstract fun settingsActivity(): SettingsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class])
    internal abstract fun splashActivity(): SplashActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class])
    internal abstract fun startActivity(): StartActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class])
    internal abstract fun trainingActivity(): TrainingActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class])
    internal abstract fun verifyPhoneNumberActivity(): VerificationActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class])
    internal abstract fun verifyPhoneVerificationCodeActivity(): VerifyPhoneVerificationCodeActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class])
    internal abstract fun verifyRecoverywordsActivity(): VerifyRecoverywordsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class])
    internal abstract fun transactionDetailsActivity(): TransactionDetailsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class, TestMarketChartModule::class, TestMarketNewsModule::class])
    internal abstract fun marketScreenActivity(): MarketScreenActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class, LightningDepositActivityTest.LightningDepositActivityTestModule::class])
    internal abstract fun lightningDepositActivity(): LightningDepositActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class, LightningWithdrawalActivityTest.LightningWithdrawalActivityTestModule::class])
    internal abstract fun lightningWithdrawalActivity(): LightningWithdrawalActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class, LightningWithdrawalBroadcastActivityTest.LightningWithdrawalBroadcastActivityTestModule::class])
    internal abstract fun lightningWithdrawalCompletedActivity(): LightningWithdrawalBroadcastActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class, UpgradeToSegwitActivityTest.UpgradeToSegwitActivityTestModule::class])
    internal abstract fun upgradeToSegwitActivity(): UpgradeToSegwitActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class, PerformSegwitUpgradeActivityTest.PerformSegwitUpgradeActivityTestModule::class])
    internal abstract fun performSegwitUpgradeActivity(): PerformSegwitUpgradeActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class])
    internal abstract fun upgradeToSegwitCompleteActivity(): UpgradeToSegwitCompleteActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class, PayRequestActivityTest.PayRequestActivityTestModule::class])
    internal abstract fun payRequestActivity(): PayRequestActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class, LndInvoiceRequestActivityTest.LndInvoiceRequestActivityTestModule::class])
    internal abstract fun lndInvoiceRequestActivity(): LndInvoiceRequestActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class, CreatePaymentActivityTest.CreatePaymentActivityTestModule::class])
    internal abstract fun createPaymentActivity(): CreatePaymentActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBaseActivityModule::class, ConfirmPaymentActivityTest.ConfirmPaymentActivityTestModule::class])
    internal abstract fun confirmPaymentActivity(): ConfirmPaymentActivity
}

