package com.coinninja.coinkeeper.service.runner;

import android.os.AsyncTask;
import android.util.Log;

import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.dto.PendingInviteDTO;
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.client.model.InvitedContact;
import com.coinninja.coinkeeper.util.DropbitIntents;
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
    private PhoneNumberUtil phoneNumberUtil;
    private PendingInviteDTO pendingInviteDTO;
    private InviteTransactionSummaryHelper inviteTransactionSummaryHelper;

    @Inject
    InviteContactRunner(SignedCoinKeeperApiClient client, CNWalletManager cnWalletManager, PhoneNumberUtil phoneNumberUtil, InviteTransactionSummaryHelper inviteTransactionSummaryHelper) {
        this.client = client;
        this.cnWalletManager = cnWalletManager;
        this.phoneNumberUtil = phoneNumberUtil;
        this.inviteTransactionSummaryHelper = inviteTransactionSummaryHelper;
    }

    private InviteContactRunner(SignedCoinKeeperApiClient client, CNWalletManager cnWalletManager, PhoneNumberUtil phoneNumberUtil, PendingInviteDTO pendingInviteDTO, OnInviteListener onInviteListener, InviteTransactionSummaryHelper inviteTransactionSummaryHelper) {
        this.client = client;
        this.cnWalletManager = cnWalletManager;
        this.onInviteListener = onInviteListener;
        this.phoneNumberUtil = phoneNumberUtil;
        this.pendingInviteDTO = pendingInviteDTO;
        this.inviteTransactionSummaryHelper = inviteTransactionSummaryHelper;
    }

    public void setPendingInviteDTO(PendingInviteDTO pendingInviteDTO) {
        this.pendingInviteDTO = pendingInviteDTO;
    }

    public void setOnInviteListener(OnInviteListener onInviteListener) {
        this.onInviteListener = onInviteListener;
    }

    @Override
    public InviteContactRunner clone() {
        return new InviteContactRunner(client, cnWalletManager, phoneNumberUtil, pendingInviteDTO, onInviteListener, inviteTransactionSummaryHelper);
    }

    @Override
    protected Response doInBackground(Contact... contacts) {
        publishProgress(50);

        inviteTransactionSummaryHelper.saveTemporaryInvite(pendingInviteDTO);

        Contact receiverContact = contacts[0];

        BTCCurrency sendingValue = new BTCCurrency(pendingInviteDTO.getInviteAmount());
        long centsAmountSendingUSD = sendingValue.toUSD(new USDCurrency(pendingInviteDTO.getBitcoinPrice())).toLong();
        long satoshisAmountSendingBTC = sendingValue.toSatoshis();

        PhoneNumber senderPhoneNumber = cnWalletManager.getContact().getPhoneNumber();

        PhoneNumber receiverPhoneNumber = receiverContact.getPhoneNumber();

        Response response = client.invitePhoneNumber(
                centsAmountSendingUSD,
                satoshisAmountSendingBTC,
                senderPhoneNumber,
                receiverPhoneNumber,
                pendingInviteDTO.getRequestId());

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
            onInviteListener.onInviteError(DropbitIntents.ACTION_DROPBIT__ERROR_UNKNOWN, getErrorMessage(response));
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
                onInviteListener.onInviteError(DropbitIntents.ACTION_DROPBIT__ERROR_RATE_LIMIT, getErrorMessage(errorResponse));
                break;
            default:
                onInviteListener.onInviteError(DropbitIntents.ACTION_DROPBIT__ERROR_UNKNOWN, getErrorMessage(errorResponse));
        }
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
