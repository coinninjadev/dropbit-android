package com.coinninja.coinkeeper.di.component

import com.coinninja.coinkeeper.di.interfaces.ActivityScope
import com.coinninja.coinkeeper.di.module.TestPicassoModule
import com.coinninja.coinkeeper.ui.account.UserServerAddressesFragment
import com.coinninja.coinkeeper.ui.account.verify.PhoneIdentityFragment
import com.coinninja.coinkeeper.ui.account.verify.TwitterIdentityFragment
import com.coinninja.coinkeeper.ui.base.BaseDialogFragment
import com.coinninja.coinkeeper.ui.base.BaseFragment
import com.coinninja.coinkeeper.ui.base.DropbitMeFragment
import com.coinninja.coinkeeper.ui.base.TransactionTweetDialogTest
import com.coinninja.coinkeeper.ui.dropbit.me.DropBitMeDialog
import com.coinninja.coinkeeper.ui.dropbit.me.verified.DisabledDropbitMeDialog
import com.coinninja.coinkeeper.ui.dropbit.me.verified.NewlyVerifiedDropbitMeDialog
import com.coinninja.coinkeeper.ui.dropbit.me.verified.VerifiedDropbitMeDialog
import com.coinninja.coinkeeper.ui.dropbit.me.verify.VerifyDropBitMeDialog
import com.coinninja.coinkeeper.ui.lightning.history.LightningHistoryFragment
import com.coinninja.coinkeeper.ui.lightning.history.LightningHistoryFragmentTest
import com.coinninja.coinkeeper.ui.lightning.loading.LightningLoadingOptionsDialog
import com.coinninja.coinkeeper.ui.lightning.locked.LightningLockedFragment
import com.coinninja.coinkeeper.ui.payment.PaymentBarFragment
import com.coinninja.coinkeeper.ui.payment.PaymentBarFragmentTest
import com.coinninja.coinkeeper.ui.transaction.history.TransactionHistoryFragment
import com.coinninja.coinkeeper.ui.twitter.ShareTransactionDialog
import com.coinninja.coinkeeper.ui.twitter.TransactionTweetDialog
import com.coinninja.coinkeeper.view.fragment.*
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class TestAndroidFragmentBuilder {

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun transactionHistoryFragment(): TransactionHistoryFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [TransactionTweetDialogTest.TestTransactionTweetDialogModule::class, TestPicassoModule::class])
    internal abstract fun transactionTweetDialog(): TransactionTweetDialog

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun baseFragment(): BaseFragment

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun disabledDropbitMeDialog(): DisabledDropbitMeDialog

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestPicassoModule::class])
    internal abstract fun verifiedDropbitMeDialog(): VerifiedDropbitMeDialog

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestPicassoModule::class])
    internal abstract fun newlyVerifiedDropbitMeDialog(): NewlyVerifiedDropbitMeDialog

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun verifyDropBitMeDialog(): VerifyDropBitMeDialog

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun dropbitMeFragment(): DropbitMeFragment

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun dropBitMeDialog(): DropBitMeDialog

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun shareTransactionDialog(): ShareTransactionDialog

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun authenticateFragment(): AuthenticateFragment

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun userServerAddressesFragment(): UserServerAddressesFragment

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun baseDialogFragment(): BaseDialogFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [ConfirmPayDialogFragmentTest.TestConfirmPayDialogModule::class])
    internal abstract fun confirmPayDialogFragment(): ConfirmPayDialogFragment

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun fingerprintAuthDialog(): FingerprintAuthDialog

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun inviteHelpDialogFragment(): InviteHelpDialogFragment

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun verifyRecoverywordsFragment(): VerifyRecoverywordsFragment

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun pinConfirmFragment(): PinConfirmFragment

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun pinCreateFragment(): PinCreateFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [PaymentBarFragmentTest.TestPaymentBarModule::class])
    internal abstract fun paymentBarFragment(): PaymentBarFragment

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun phoneIdentityFragment(): PhoneIdentityFragment

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun twitterIdentityFragment(): TwitterIdentityFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [LightningHistoryFragmentTest.FragmentModule::class])
    abstract fun LightningHistoryFragment(): LightningHistoryFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun lightningLoadingOptionsDialog(): LightningLoadingOptionsDialog

    @ActivityScope
    @ContributesAndroidInjector()
    internal abstract fun lightningLockedFragment(): LightningLockedFragment
}
