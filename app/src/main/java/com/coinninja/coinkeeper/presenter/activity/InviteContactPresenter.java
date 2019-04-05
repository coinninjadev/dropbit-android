package com.coinninja.coinkeeper.presenter.activity;

import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.client.model.DropBitInvitation;
import com.coinninja.coinkeeper.service.runner.InviteContactRunner;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import org.json.JSONException;
import org.json.JSONObject;

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
    public void onInviteSuccessful(DropBitInvitation inviteContact) {
        view.showInviteSuccessful(inviteContact);
        analytics.setUserProperty(Analytics.PROPERTY_HAS_SENT_DROPBIT, true);
        analytics.trackEvent(Analytics.EVENT_DROPBIT_INITIATED);
    }

    @Override
    public void onInviteSuccessfulDegradedSms(DropBitInvitation inviteContact) {
        view.showInviteSuccessfulDegradedSms(inviteContact);
        analytics.setUserProperty(Analytics.PROPERTY_HAS_SENT_DROPBIT, true);


        JSONObject jsonObject = new JSONObject();
        try {
            if (inviteContact != null && inviteContact.getMetadata() != null && inviteContact.getMetadata().getReceiver() != null) {
                jsonObject.put(Analytics.JSON_KEY_COUNTRY_CODE, inviteContact.getMetadata().getReceiver().getCountry_code());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        analytics.trackEvent(Analytics.EVENT_DROPBIT_INVITATION_SMS_FAILED, jsonObject);
    }

    @Override
    public void onInviteProgress(int progress) {
        view.showProgress(progress);
    }

    @Override
    public void onInviteError(String dropBitActionError, String errorMessage) {
        view.showInviteFail(dropBitActionError, errorMessage);
        analytics.trackEvent(Analytics.EVENT_DROPBIT_INITIATION_FAILED);
    }

    public interface View {
        void showInviteFail(String dropBitActionError, String errorMessage);

        void showInviteSuccessful(DropBitInvitation inviteContact);

        void showInviteSuccessfulDegradedSms(DropBitInvitation inviteContact);

        void showProgress(int progress);
    }

}
