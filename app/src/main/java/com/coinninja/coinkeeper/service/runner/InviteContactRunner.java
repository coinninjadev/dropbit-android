package com.coinninja.coinkeeper.service.runner;

import android.os.AsyncTask;
import android.util.Log;

import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.client.model.DropBitInvitation;
import com.coinninja.coinkeeper.service.client.model.InviteMetadata;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.google.gson.Gson;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Response;

public class InviteContactRunner extends AsyncTask<Contact, Integer, InvitationResult> {
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

    public void setSatoshisSending(Long satoshisSending) {
        this.satoshisSending = satoshisSending;
    }

    public void setUSAExchangeCurrency(USDCurrency usdExchangeCurrency) {
        this.usdExchangeCurrency = usdExchangeCurrency;
    }

    @Override
    protected InvitationResult doInBackground(Contact... contacts) {
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

        String errorMessage = getErrorMessage(response);
        if (response == null) {
            return new InvitationResult(InvitationResult.Status.UNKNOWN_ERROR, errorMessage);
        } else if (response.code() == 501) {
            DropBitInvitation dropBitInvitation = new Gson().fromJson(errorMessage, DropBitInvitation.class);
            return new InvitationResult(dropBitInvitation, response.code(), InvitationResult.Status.DEGRADED_SMS);
        } else if (!response.isSuccessful()) {
            Log.d(TAG, "|---- Invite Contact failed");
            Log.d(TAG, "|------ statusCode: " + String.valueOf(response.code()));
            Log.d(TAG, "|--------- errorMessage: " + errorMessage);
            return new InvitationResult(response.code(), InvitationResult.Status.ERROR, errorMessage);
        } else {
            DropBitInvitation inviteContact = (DropBitInvitation) response.body();
            return new InvitationResult(inviteContact, response.code(), InvitationResult.Status.SUCCESS);
        }
    }

    @Override
    protected void onPostExecute(InvitationResult invitationResult) {
        if (invitationResult.getStatus() == InvitationResult.Status.UNKNOWN_ERROR) {
            onInviteListener.onInviteError(Intents.ACTION_DROPBIT__ERROR_UNKNOWN, invitationResult.getErrorMessage());
        } else if (invitationResult.getStatus() == InvitationResult.Status.DEGRADED_SMS) {
            onInviteListener.onInviteSuccessfulDegradedSms(invitationResult.getInvitedResult());
        } else if (invitationResult.getStatus() == InvitationResult.Status.SUCCESS) {
            onInviteListener.onInviteSuccessful(invitationResult.getInvitedResult());
        } else {
            onResponseError(invitationResult.getStatusCode(), invitationResult.getErrorMessage());
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        onInviteListener.onInviteProgress(values[0]);
    }

    private void onResponseError(int code, String errorMessage) {
        switch (code) {
            case 429:
                onInviteListener.onInviteError(Intents.ACTION_DROPBIT__ERROR_RATE_LIMIT, errorMessage);
                break;
            default:
                onInviteListener.onInviteError(Intents.ACTION_DROPBIT__ERROR_UNKNOWN, errorMessage);
        }
    }

    private String getErrorMessage(Response response) {
        try {
            if (response != null) {
                ResponseBody responseBody = response.errorBody();
                if (responseBody != null) {
                    return responseBody.string();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "unknown";
    }

    public interface OnInviteListener {
        void onInviteSuccessful(DropBitInvitation contact);

        void onInviteSuccessfulDegradedSms(DropBitInvitation contact);

        void onInviteProgress(int progress);

        void onInviteError(String dropBitActionError, String errorMessage);
    }
}
