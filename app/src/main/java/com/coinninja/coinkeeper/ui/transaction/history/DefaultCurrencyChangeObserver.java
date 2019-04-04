package com.coinninja.coinkeeper.ui.transaction.history;

import com.coinninja.coinkeeper.util.DefaultCurrencies;

public interface DefaultCurrencyChangeObserver {
    void onDefaultCurrencyChanged(DefaultCurrencies defaultCurrencies);
}
