package com.coinninja.coinkeeper.service.runner;

import android.os.AsyncTask;
import android.util.Log;

import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.client.model.InvitedContact;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import javax.inject.Inject;

import retrofit2.Response;

public class InviteContactRunner extends AsyncTask<Contact, Integer, Response> {
    private static final String TAG = InviteContactRunner.class.getSimpleName();

    private final SignedCoinKeeperApiClient client;
    private final CNWalletManager cnWalletManager;
    private OnInviteListener onInviteListener;
    private Long satoshisSending;
    private USDCurrency usdExchangeCurrency;
    private PhoneNumberUtil phoneNumberUtil;

    @Inject
    InviteContactRunner(SignedCoinKeeperApiClient client, CNWalletManager cnWalletManager, PhoneNumberUtil phoneNumberUtil) {
        this.client = client;
        this.cnWalletManager = cnWalletManager;
        this.phoneNumberUtil = phoneNumberUtil;
    }

    private InviteContactRunner(SignedCoinKeeperApiClient client, CNWalletManager cnWalletManager, PhoneNumberUtil phoneNumberUtil, OnInviteListener onInviteListener) {
        this.client = client;
        this.cnWalletManager = cnWalletManager;
        this.onInviteListener = onInviteListener;
        this.phoneNumberUtil = phoneNumberUtil;
    }

    public void setOnInviteListener(OnInviteListener onInviteListener) {
        this.onInviteListener = onInviteListener;
    }

    @Override
    public InviteContactRunner clone() {
        return new InviteContactRunner(client, cnWalletManager, phoneNumberUtil, onInviteListener);
    }

    @Override
    protected Response doInBackground(Contact... contacts) {
        publishProgress(50);

        Contact receiverContact = contacts[0];

        BTCCurrency sendingValue = new BTCCurrency(satoshisSending);
        long centsAmountSendingUSD = sendingValue.toUSD(usdExchangeCurrency).toLong();
        long satoshisAmountSendingBTC = sendingValue.toSatoshis();

        PhoneNumber senderPhoneNumber = cnWalletManager.getContact().getPhoneNumber();

        PhoneNumber receiverPhoneNumber = receiverContact.getPhoneNumber();

        Response response = client.invitePhoneNumber(
                centsAmountSendingUSD,
                satoshisAmountSendingBTC,
                senderPhoneNumber,
                receiverPhoneNumber);


        if (response.isSuccessful()) {
            return response;
        } else {
            Log.d(TAG, "|---- Invite Contact failed");
            Log.d(TAG, "|------ statusCode: " + String.valueOf(response.code()));
            Log.d(TAG, "|--------- message: " + response.body());
        }

        sleep(1000);

        return response;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        onInviteListener.onInviteProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Response response) {
        if (response == null) {
            onInviteListener.onInviteError(Intents.ACTION_DROPBIT__ERROR_UNKNOWN, getErrorMessage(response));
            return;
        }

        if (response.isSuccessful()) {
            InvitedContact inviteContact = (InvitedContact) response.body();
            onInviteListener.onInviteSuccessful(inviteContact);
        } else {
            onResponseError(response);
        }
    }

    private void onResponseError(Response errorResponse) {
        switch (errorResponse.code()) {
            case 429:
                onInviteListener.onInviteError(Intents.ACTION_DROPBIT__ERROR_RATE_LIMIT, getErrorMessage(errorResponse));
                break;
            default:
                onInviteListener.onInviteError(Intents.ACTION_DROPBIT__ERROR_UNKNOWN, getErrorMessage(errorResponse));
        }
    }

    public void setSatoshisSending(Long satoshisSending) {
        this.satoshisSending = satoshisSending;
    }

    public void setUSAExchangeCurrency(USDCurrency usdExchangeCurrency) {
        this.usdExchangeCurrency = usdExchangeCurrency;
    }

    private String getErrorMessage(Response response) {
        if (response == null) return "unknown";


        try {
            return response.errorBody().string();
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown";
        }
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public interface OnInviteListener {
        void onInviteSuccessful(InvitedContact contact);

        void onInviteProgress(int progress);

        void onInviteError(String dropBitActionError, String errorMessage);
    }
}
