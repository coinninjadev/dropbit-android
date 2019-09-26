package com.coinninja.coinkeeper.di.builder

import com.coinninja.coinkeeper.di.interfaces.ActivityScope
import com.coinninja.coinkeeper.ui.account.UserServerAddressesFragment
import com.coinninja.coinkeeper.ui.account.verify.PhoneIdentityFragment
import com.coinninja.coinkeeper.ui.account.verify.TwitterIdentityFragment
import com.coinninja.coinkeeper.ui.base.BaseDialogFragment
import com.coinninja.coinkeeper.ui.base.BaseFragment
import com.coinninja.coinkeeper.ui.base.DropbitMeFragment
import com.coinninja.coinkeeper.ui.dropbit.me.DropBitMeDialog
import com.coinninja.coinkeeper.ui.dropbit.me.verified.DisabledDropbitMeDialog
import com.coinninja.coinkeeper.ui.dropbit.me.verified.NewlyVerifiedDropbitMeDialog
import com.coinninja.coinkeeper.ui.dropbit.me.verified.VerifiedDropbitMeDialog
import com.coinninja.coinkeeper.ui.dropbit.me.verify.VerifyDropBitMeDialog
import com.coinninja.coinkeeper.ui.lightning.history.LightningHistoryFragment
import com.coinninja.coinkeeper.ui.lightning.history.LightningHistoryFragmentModule
import com.coinninja.coinkeeper.ui.lightning.loading.LightningLoadingOptionsDialog
import com.coinninja.coinkeeper.ui.lightning.locked.LightningLockedFragment
import com.coinninja.coinkeeper.ui.payment.PaymentBarFragment
import com.coinninja.coinkeeper.ui.transaction.history.TransactionHistoryFragment
import com.coinninja.coinkeeper.ui.twitter.ShareTransactionDialog
import com.coinninja.coinkeeper.ui.twitter.TransactionTweetDialog
import com.coinninja.coinkeeper.ui.twitter.TwitterTweetModule
import com.coinninja.coinkeeper.view.fragment.*
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class AndroidFragmentBuilder {

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun transactionHistoryFragment(): TransactionHistoryFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [TwitterTweetModule::class])
    internal abstract fun transactionTweetDialog(): TransactionTweetDialog

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun baseFragment(): BaseFragment

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun disabledDropbitMeDialog(): DisabledDropbitMeDialog

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun verifiedDropbitMeDialog(): VerifiedDropbitMeDialog

    @ActivityScope
    @ContributesAndroidInjector
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
    @ContributesAndroidInjector
    internal abstract fun confirmPayDialogFragment(): ConfirmPayDialogFragment

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun fingerprintAuthDialog(): FingerprintAuthDialog

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun inviteHelpDialogFragment(): InviteHelpDialogFragment

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun payDialogFragment(): PayDialogFragment

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
    @ContributesAndroidInjector
    internal abstract fun paymentBarFragment(): PaymentBarFragment

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun phoneIdentityFragment(): PhoneIdentityFragment

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun twitterIdentityFragment(): TwitterIdentityFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [LightningHistoryFragmentModule::class])
    internal abstract fun lightningHistoryFragment(): LightningHistoryFragment

    @ActivityScope
    @ContributesAndroidInjector()
    internal abstract fun lightningLoadingOptionsDialog(): LightningLoadingOptionsDialog

    @ActivityScope
    @ContributesAndroidInjector()
    internal abstract fun lightningLockedFragment(): LightningLockedFragment
}
