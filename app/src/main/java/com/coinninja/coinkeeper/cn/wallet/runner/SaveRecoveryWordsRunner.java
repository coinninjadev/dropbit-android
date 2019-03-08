package com.coinninja.coinkeeper.cn.wallet.runner;

import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import javax.inject.Inject;

public class SaveRecoveryWordsRunner implements Runnable {
    private final CNWalletManager cnWalletManager;
    private final LocalBroadCastUtil localBroadCastUtil;

    private String[] words;

    @Inject
    SaveRecoveryWordsRunner(CNWalletManager cnWalletManager,
                            LocalBroadCastUtil localBroadCastUtil) {

        this.cnWalletManager = cnWalletManager;
        this.localBroadCastUtil = localBroadCastUtil;
    }

    public void setWords(String[] words) {
        this.words = words;
    }

    @Override
    public void run() {
        if (null == words || !cnWalletManager.userVerifiedWords(words)) {
            communicateFailure();
        } else {
            communicateSuccess();
        }
    }

    private void communicateSuccess() {
        localBroadCastUtil.sendBroadcast(Intents.ACTION_SAVE_RECOVERY_WORDS);
    }

    private void communicateFailure() {
        localBroadCastUtil.sendBroadcast(Intents.ACTION_UNABLE_TO_SAVE_RECOVERY_WORDS);
    }
}
