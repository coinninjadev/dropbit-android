package com.coinninja.coinkeeper.di.component;

import com.coinninja.coinkeeper.cn.wallet.runner.SaveRecoveryWordsRunner;
import com.coinninja.coinkeeper.service.runner.FullSyncWalletRunner;
import com.coinninja.coinkeeper.util.analytics.Analytics;

import java.util.Locale;

public interface CoinKeeperComponent {
    FullSyncWalletRunner getFullSyncRunner();

    SaveRecoveryWordsRunner getSaveRecoveryWordsRunner();

    Analytics getAnalytics();

    Locale getLocale();
}
