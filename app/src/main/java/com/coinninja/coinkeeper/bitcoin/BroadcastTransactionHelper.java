package com.coinninja.coinkeeper.bitcoin;

import com.coinninja.bindings.TransactionBroadcastResult;
import com.coinninja.bindings.TransactionBuilder;
import com.coinninja.bindings.TransactionData;
import com.coinninja.bindings.model.Transaction;
import com.coinninja.coinkeeper.service.client.BlockchainClient;
import com.coinninja.coinkeeper.util.analytics.Analytics;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Response;

public class BroadcastTransactionHelper {
    private final Analytics analytics;
    private final BlockchainClient blockchainClient;
    private final TransactionBuilder transactionBuilder;

    @Inject
    BroadcastTransactionHelper(TransactionBuilder transactionBuilder, BlockchainClient blockchainClient, Analytics analytics) {
        this.analytics = analytics;
        this.blockchainClient = blockchainClient;
        this.transactionBuilder = transactionBuilder;
    }

    public TransactionBroadcastResult broadcast(TransactionData transactionData) {

        TransactionBroadcastResult blockChainInfoResult = broadcastToBlockChainInfo(transactionData);
        reportBroadcastResult(blockChainInfoResult);
        return blockChainInfoResult;
    }


    private TransactionBroadcastResult broadcastToBlockChainInfo(TransactionData transactionData) {
        Transaction transaction = transactionBuilder.build(transactionData);

        Response response = blockchainClient.broadcastTransaction(transaction.rawTx);

        if (response.isSuccessful()) {
            String successMessage;
            try {
                successMessage = ((ResponseBody) response.body()).string();
            } catch (Exception e) {
                successMessage = response.message();
            }
            return generateSuccessfulBroadcast(transaction, successMessage, response.code());
        } else {
            String failedReason;
            try {
                failedReason = response.errorBody().string();
            } catch (Exception e) {
                failedReason = response.message();
            }
            return generateFailedBroadcast(failedReason, transaction.rawTx, transaction.txId, response.code());
        }
    }


    private void reportBroadcastResult(TransactionBroadcastResult broadcastResult) {
        JSONObject properties = buildBroadcastProp(broadcastResult);

        if (broadcastResult.isSuccess()) {
            analytics.trackEvent(Analytics.EVENT_BROADCAST_COMPLETE, properties);
        } else {
            analytics.trackEvent(Analytics.EVENT_BROADCAST_FAILED, properties);
        }
    }


    private JSONObject buildBroadcastProp(TransactionBroadcastResult broadcastResult) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put(Analytics.EVENT_BROADCAST_JSON_KEY_BLOCK_CODE, broadcastResult.getResponseCode());
            jsonObject.put(Analytics.EVENT_BROADCAST_JSON_KEY_BLOCK_MSG, broadcastResult.getMessage());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }


    public TransactionBroadcastResult generateSuccessfulBroadcast(Transaction transaction, String message, int responseCode) {
        return generateBroadcastResponse(true, message, transaction, responseCode);
    }

    public TransactionBroadcastResult generateFailedBroadcast(String reason) {
        return generateFailedBroadcast(reason, "", "", -1);
    }

    public TransactionBroadcastResult generateFailedBroadcast(String reason, String raxTX, String txid, int responseCode) {
        return generateBroadcastResponse(false, reason, new Transaction(raxTX, txid), responseCode);
    }

    private TransactionBroadcastResult generateBroadcastResponse(boolean isSuccessful, String reason, Transaction transaction, int responseCode) {
        return new TransactionBroadcastResult(responseCode, isSuccessful, reason, transaction);
    }
}
