package com.coinninja.coinkeeper.service.runner;

import com.coinninja.bindings.TransactionBroadcastResult;
import com.coinninja.bindings.TransactionData;
import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.bitcoin.BroadcastTransactionHelper;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.util.analytics.Analytics;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

import retrofit2.Response;

public class BroadcastTransactionRunner extends SteppedAsyncTask<TransactionData, Integer, TransactionBroadcastResult> {
    private static final int NUMBER_OF_STEPS = 2;

    private final CoinKeeperApplication application;
    private final BroadcastTransactionHelper broadcastTransactionHelper;
    private final SignedCoinKeeperApiClient apiClient;
    private final Analytics analytics;
    BroadcastListener broadcastListener;

    @Inject
    public BroadcastTransactionRunner(CoinKeeperApplication application, BroadcastTransactionHelper broadcastTransactionHelper,
                                      SignedCoinKeeperApiClient signedCoinKeeperApiClient, Analytics analytics) {
        this.application = application;
        this.broadcastTransactionHelper = broadcastTransactionHelper;
        apiClient = signedCoinKeeperApiClient;
        this.analytics = analytics;
    }

    public BroadcastTransactionRunner(CoinKeeperApplication application, BroadcastTransactionHelper broadcastTransactionHelper,
                                      SignedCoinKeeperApiClient signedCoinKeeperApiClient, Analytics analytics, BroadcastListener broadcastListener) {
        this.application = application;
        this.broadcastTransactionHelper = broadcastTransactionHelper;
        apiClient = signedCoinKeeperApiClient;
        this.analytics = analytics;
        this.broadcastListener = broadcastListener;
    }

    @Override
    public BroadcastTransactionRunner clone() {
        return new BroadcastTransactionRunner(application, broadcastTransactionHelper,
                apiClient, analytics, broadcastListener);
    }

    public void setBroadcastListener(BroadcastListener broadcastListener) {
        this.broadcastListener = broadcastListener;
    }

    @Override
    int getNumberOfPrimarySteps() {
        return NUMBER_OF_STEPS;
    }

    @Override
    public void reportPrimaryStepCompleted() {
        super.reportPrimaryStepCompleted();
        publishProgress(Math.round(getProgress()) * 100);
    }

    @Override
    public void reportSubStepCompleted(int numSubSteps) {
        super.reportSubStepCompleted(numSubSteps);
        publishProgress(Math.round(getProgress()) * 100);
    }

    @Override
    protected TransactionBroadcastResult doInBackground(TransactionData... transactions) {
        analytics.trackEvent(Analytics.Companion.EVENT_BROADCAST_STARTED);
        if (transactions.length < 1) return null;
        TransactionData transactionData = transactions[0];
        TransactionBroadcastResult transactionBroadcastResult;

        if (checkIn()) {
            transactionBroadcastResult = broadcastTransaction(transactionData);
        } else {
            transactionBroadcastResult = broadcastTransactionHelper.
                    generateFailedBroadcast(application.getString(R.string.transaction_checksum_error));
            reportCheckingFailed();
        }

        return transactionBroadcastResult;
    }

    @Override
    protected void onPostExecute(TransactionBroadcastResult transactionBroadcastResult) {
        if (transactionBroadcastResult.isSuccess()) {
            broadcastListener.onBroadcastSuccessful(transactionBroadcastResult);
            analytics.setUserProperty(Analytics.Companion.PROPERTY_HAS_SENT_ADDRESS, true);
        } else {
            broadcastListener.onBroadcastError(transactionBroadcastResult);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        broadcastListener.onBroadcastProgress(values[0]);
    }

    private boolean checkIn() {
        reportSubStepCompleted(3);
        Response currentState = apiClient.getCurrentState();
        reportSubStepCompleted(3);
        boolean isSuccess = currentState.isSuccessful();
        if (isSuccess) reportPrimaryStepCompleted();
        return isSuccess;
    }

    private TransactionBroadcastResult broadcastTransaction(TransactionData transactionData) {
        TransactionBroadcastResult result;
        reportSubStepCompleted(2);

        if (checksumPass(transactionData)) {
            result = broadcastTransactionHelper.broadcast(transactionData);
        } else {
            result = broadcastTransactionHelper.generateFailedBroadcast(application.getString(R.string.transaction_checksum_error));
        }

        reportPrimaryStepCompleted();
        return result;
    }

    private boolean checksumPass(TransactionData transactionData) {
        return transactionData != null && transactionData.getPaymentAddress() != null &&
                !transactionData.getPaymentAddress().isEmpty();
    }

    private void reportCheckingFailed() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Analytics.Companion.EVENT_BROADCAST_JSON_KEY_CHECK_IN_FAIL, Analytics.Companion.EVENT_BROADCAST_CHECK_IN_FAIL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        analytics.trackEvent(Analytics.Companion.EVENT_BROADCAST_FAILED, jsonObject);
    }

    public interface BroadcastListener {

        void onBroadcastSuccessful(TransactionBroadcastResult transactionBroadcastResult);

        void onBroadcastProgress(int progress);

        void onBroadcastError(TransactionBroadcastResult transactionBroadcastResult);

    }
}
