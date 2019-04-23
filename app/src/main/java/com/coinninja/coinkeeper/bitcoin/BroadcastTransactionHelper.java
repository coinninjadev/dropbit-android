package com.coinninja.coinkeeper.bitcoin;

import com.coinninja.bindings.TransactionBroadcastResult;
import com.coinninja.bindings.TransactionBuilder;
import com.coinninja.bindings.TransactionData;
import com.coinninja.bindings.model.Transaction;
import com.coinninja.coinkeeper.interfaces.ErrorLogging;
import com.coinninja.coinkeeper.service.client.BlockchainClient;
import com.coinninja.coinkeeper.service.client.BlockstreamClient;
import com.coinninja.coinkeeper.util.ErrorLoggingUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.crashlytics.android.Crashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Response;

public class BroadcastTransactionHelper {
    private final Analytics analytics;
    private final BlockchainClient blockchainClient;
    private final TransactionBuilder transactionBuilder;
    private final BlockstreamClient blockstreamClient;
    private final ErrorLoggingUtil errorLoggingUtil;

    @Inject
    BroadcastTransactionHelper(TransactionBuilder transactionBuilder, BlockchainClient blockchainClient,
                               BlockstreamClient blockstreamClient, Analytics analytics, ErrorLoggingUtil errorLoggingUtil) {
        this.analytics = analytics;
        this.blockchainClient = blockchainClient;
        this.transactionBuilder = transactionBuilder;
        this.blockstreamClient = blockstreamClient;
        this.errorLoggingUtil = errorLoggingUtil;
    }

    public TransactionBroadcastResult broadcast(TransactionData transactionData) {
        TransactionBroadcastResult blockChainInfoResult = broadcastToBlockChainInfo(transactionData);
        TransactionBroadcastResult blockstreamInfoResult = broadcastToBlockstream(transactionData);

        reportBroadcastResult(blockChainInfoResult, blockstreamInfoResult);
        return blockChainInfoResult.isSuccess() ? blockChainInfoResult : blockstreamInfoResult;
    }

    private TransactionBroadcastResult broadcastToBlockstream(TransactionData transactionData) {
        Transaction transaction = transactionBuilder.build(transactionData);
        return handleBroadcastResult(transaction, blockstreamClient.broadcastTransaction(transaction.rawTx));
    }

    private TransactionBroadcastResult broadcastToBlockChainInfo(TransactionData transactionData) {
        Transaction transaction = transactionBuilder.build(transactionData);
        return handleBroadcastResult(transaction, blockchainClient.broadcastTransaction(transaction.rawTx));
    }

    private TransactionBroadcastResult handleBroadcastResult(Transaction transaction, Response response) {
        if (response.isSuccessful()) {
            String successMessage;
            try {
                successMessage = ((ResponseBody) response.body()).string();
            } catch (Exception e) {
                errorLoggingUtil.logError(e);
                successMessage = response.message();
            }

            return generateSuccessfulBroadcast(transaction, successMessage, response.code());
        } else {
            String failedReason;
            try {
                failedReason = response.errorBody().string();
            } catch (Exception e) {
                errorLoggingUtil.logError(e);
                failedReason = response.message();
            }

            return generateFailedBroadcast(failedReason, transaction.rawTx, transaction.txId, response.code());
        }
    }

    private void reportBroadcastResult(TransactionBroadcastResult blockChainInfoResult, TransactionBroadcastResult blockstreamInfoResult) {
        boolean isSuccess = blockChainInfoResult.isSuccess() ? blockChainInfoResult.isSuccess() : blockstreamInfoResult.isSuccess();
        JSONObject properties = buildBroadcastProp(blockChainInfoResult, blockstreamInfoResult);

        if (isSuccess) {
            analytics.trackEvent(Analytics.EVENT_BROADCAST_COMPLETE, properties);
        } else {
            analytics.trackEvent(Analytics.EVENT_BROADCAST_FAILED, properties);
        }
    }


    private JSONObject buildBroadcastProp(TransactionBroadcastResult blockChainInfoResult, TransactionBroadcastResult blockstreamResponse) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put(Analytics.EVENT_BROADCAST_JSON_KEY_LIB_CODE, blockstreamResponse.getResponseCode());
            jsonObject.put(Analytics.EVENT_BROADCAST_JSON_KEY_LIB_MSG, blockstreamResponse.getMessage());
            jsonObject.put(Analytics.EVENT_BROADCAST_JSON_KEY_BLOCK_CODE, blockChainInfoResult.getResponseCode());
            jsonObject.put(Analytics.EVENT_BROADCAST_JSON_KEY_BLOCK_MSG, blockChainInfoResult.getMessage());
        } catch (JSONException e) {
            Crashlytics.logException(e);
        }

        return jsonObject;
    }


    public TransactionBroadcastResult generateFailedBroadcast(String reason) {
        return generateFailedBroadcast(reason, "", "", -1);
    }

    public TransactionBroadcastResult generateSuccessfulBroadcast(Transaction transaction, String message, int responseCode) {
        return generateBroadcastResponse(true, message, transaction, responseCode);
    }

    public TransactionBroadcastResult generateFailedBroadcast(String reason, String raxTX, String txid, int responseCode) {
        return generateBroadcastResponse(false, reason, new Transaction(raxTX, txid), responseCode);
    }

    public TransactionBroadcastResult generateBroadcastResponse(boolean isSuccessful, String reason, Transaction transaction, int responseCode) {
        return new TransactionBroadcastResult(responseCode, isSuccessful, reason, transaction);
    }
}
