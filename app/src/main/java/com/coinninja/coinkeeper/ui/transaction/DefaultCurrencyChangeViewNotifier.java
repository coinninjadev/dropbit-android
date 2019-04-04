package com.coinninja.coinkeeper.ui.transaction;

import com.coinninja.coinkeeper.ui.transaction.history.DefaultCurrencyChangeObserver;
import com.coinninja.coinkeeper.util.DefaultCurrencies;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class DefaultCurrencyChangeViewNotifier implements DefaultCurrencyChangeObserver {
    private List<WeakReference<DefaultCurrencyChangeObserver>> observers;

    @Inject
    DefaultCurrencyChangeViewNotifier() {
        observers = new ArrayList<>();
    }

    public void observeDefaultCurrencyChange(DefaultCurrencyChangeObserver observer) {
        observers.add(new WeakReference<>(observer));
    }

    public void onDefaultCurrencyChanged(DefaultCurrencies defaultCurrencies) {

        for (WeakReference<DefaultCurrencyChangeObserver> observerWeakReference : observers) {
            if (observerWeakReference.get() != null) {
                observerWeakReference.get().onDefaultCurrencyChanged(defaultCurrencies);
            }
        }
    }
}
