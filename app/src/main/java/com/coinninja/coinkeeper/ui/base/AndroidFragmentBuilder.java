package com.coinninja.coinkeeper.ui.base;

import com.coinninja.coinkeeper.ui.account.UserServerAddressesFragment;
import com.coinninja.coinkeeper.ui.payment.PaymentBarFragment;
import com.coinninja.coinkeeper.ui.twitter.ShareTransactionDialog;
import com.coinninja.coinkeeper.view.fragment.AuthenticateFragment;
import com.coinninja.coinkeeper.view.fragment.ConfirmPayDialogFragment;
import com.coinninja.coinkeeper.view.fragment.FingerprintAuthDialog;
import com.coinninja.coinkeeper.view.fragment.InviteHelpDialogFragment;
import com.coinninja.coinkeeper.view.fragment.PayDialogFragment;
import com.coinninja.coinkeeper.view.fragment.PinConfirmFragment;
import com.coinninja.coinkeeper.view.fragment.PinCreateFragment;
import com.coinninja.coinkeeper.view.fragment.RequestDialogFragment;
import com.coinninja.coinkeeper.view.fragment.VerifyRecoverywordsFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;


@Module()
public abstract class AndroidFragmentBuilder {

    @ActivityScope
    @ContributesAndroidInjector
    abstract BaseFragment baseFragment();

    @ActivityScope
    @ContributesAndroidInjector
    abstract ShareTransactionDialog shareTransactionDialog();

    @ActivityScope
    @ContributesAndroidInjector
    abstract AuthenticateFragment authenticateFragment();

    @ActivityScope
    @ContributesAndroidInjector
    abstract UserServerAddressesFragment userServerAddressesFragment();

    @ActivityScope
    @ContributesAndroidInjector
    abstract BaseDialogFragment baseDialogFragment();

    @ActivityScope
    @ContributesAndroidInjector
    abstract ConfirmPayDialogFragment confirmPayDialogFragment();

    @ActivityScope
    @ContributesAndroidInjector
    abstract FingerprintAuthDialog fingerprintAuthDialog();

    @ActivityScope
    @ContributesAndroidInjector
    abstract InviteHelpDialogFragment inviteHelpDialogFragment();

    @ActivityScope
    @ContributesAndroidInjector
    abstract PayDialogFragment payDialogFragment();

    @ActivityScope
    @ContributesAndroidInjector
    abstract RequestDialogFragment requestDialogFragment();

    @ActivityScope
    @ContributesAndroidInjector
    abstract VerifyRecoverywordsFragment verifyRecoverywordsFragment();

    @ActivityScope
    @ContributesAndroidInjector
    abstract PinConfirmFragment pinConfirmFragment();

    @ActivityScope
    @ContributesAndroidInjector
    abstract PinCreateFragment pinCreateFragment();

    @ActivityScope
    @ContributesAndroidInjector
    abstract PaymentBarFragment paymentBarFragment();

}
