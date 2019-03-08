package com.coinninja.coinkeeper.service.runner;

import android.content.Intent;

import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.client.CurrentState;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.util.CNLogger;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import javax.inject.Inject;

import retrofit2.Response;

public class CurrentBTCStateRunner implements Runnable {

    public static final String ERROR_MESSAGE = "|---- Failed to fetch current BTC Price data";
    public static final String TAG = CurrentBTCStateRunner.class.getCanonicalName();

    private final SignedCoinKeeperApiClient client;
    private final LocalBroadCastUtil localBroadCastUtil;
    private final WalletHelper walletHelper;
    private final CNLogger cnLogger;

    @Inject
    public CurrentBTCStateRunner(SignedCoinKeeperApiClient client, LocalBroadCastUtil localBroadCastUtil,
                                 WalletHelper walletHelper, CNLogger cnLogger) {
        this.client = client;
        this.localBroadCastUtil = localBroadCastUtil;
        this.walletHelper = walletHelper;
        this.cnLogger = cnLogger;
    }

    @Override
    public void run() {
        Response response = client.getCurrentState();

        if (response.code() == 200) {
            notifyStateChange((CurrentState) response.body());
        } else {
            cnLogger.logError(TAG, ERROR_MESSAGE, response);
        }
    }

    private void notifyStateChange(CurrentState currentState) {
        notifyOfPrice(currentState);
        notifyOfFees(currentState);
        notifyOfBlockHeight(currentState);
    }

    private void notifyOfBlockHeight(CurrentState currentState) {
        walletHelper.updateBlockHeight(currentState.getBlockheight());
    }

    private void notifyOfFees(CurrentState currentState) {
        Intent intent = new Intent(Intents.ACTION_TRANSACTION_FEE_UPDATE);
        intent.putExtra(Intents.EXTRA_TRANSACTION_FEE, currentState.getFees());
        walletHelper.setLatestFee(currentState.getFees());
        localBroadCastUtil.sendBroadcast(intent);
    }

    private void notifyOfPrice(CurrentState currentState) {
        Intent intent = new Intent(Intents.ACTION_BTC_PRICE_UPDATE);
        intent.putExtra(Intents.EXTRA_BITCOIN_PRICE,
                currentState.getLatestPrice().toLong());
        walletHelper.setLatestPrice(currentState.getLatestPrice());
        localBroadCastUtil.sendBroadcast(intent);
    }

}
