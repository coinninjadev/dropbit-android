package com.coinninja.coinkeeper.presenter.activity;

import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.client.model.InvitedContact;
import com.coinninja.coinkeeper.service.runner.InviteContactRunner;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import javax.inject.Inject;

public class InviteContactPresenter implements InviteContactRunner.OnInviteListener {

    private final Analytics analytics;
    private InviteContactRunner inviteRunner;
    private View view;

    @Inject
    public InviteContactPresenter(InviteContactRunner inviteRunner, Analytics analytics) {
        this.inviteRunner = inviteRunner;
        this.analytics = analytics;
    }

    public void attachView(View view) {
        this.view = view;
    }

    public void requestInvite(Contact contact, Long satoshisSending, Long bitcoinUSDPrice) {
        inviteRunner.setOnInviteListener(this);
        inviteRunner = inviteRunner.clone();
        inviteRunner.setSatoshisSending(satoshisSending);
        inviteRunner.setUSAExchangeCurrency(new USDCurrency(bitcoinUSDPrice));
        inviteRunner.execute(contact);
    }

    @Override
    public void onInviteSuccessful(InvitedContact inviteContact) {
        view.showInviteSuccessful(inviteContact);
        analytics.setUserProperty(Analytics.PROPERTY_HAS_SENT_DROPBIT, true);
    }

    @Override
    public void onInviteProgress(int progress) {
        view.showProgress(progress);
    }

    @Override
    public void onInviteError(String dropBitActionError, String errorMessage) {
        view.showInviteFail(dropBitActionError, errorMessage);
    }

    public interface View {
        void showInviteFail(String dropBitActionError, String errorMessage);

        void showInviteSuccessful(InvitedContact inviteContact);

        void showProgress(int progress);
    }
}
