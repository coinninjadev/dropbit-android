package com.coinninja.coinkeeper.ui.transaction;

import com.coinninja.coinkeeper.ui.transaction.history.DefaultCurrencyChangeObserver;
import com.coinninja.coinkeeper.util.DefaultCurrencies;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DefaultCurrencyChangeViewNotifierTest {

    @Mock
    DefaultCurrencyChangeObserver observer1;

    @Mock
    DefaultCurrencyChangeObserver observer2;

    @Mock
    DefaultCurrencies defaultCurrencies;

    DefaultCurrencyChangeViewNotifier notifier = new DefaultCurrencyChangeViewNotifier();

    @After
    public void tearDown() {
        observer1 = null;
        observer2 = null;
        notifier = null;
    }

    @Test
    public void notifies_observer_of_preference_change() {
        notifier.observeDefaultCurrencyChange(observer1);
        notifier.observeDefaultCurrencyChange(observer2);
        observer2 = null;
        System.gc();

        notifier.onDefaultCurrencyChanged(defaultCurrencies);

        verify(observer1).onDefaultCurrencyChanged(defaultCurrencies);
    }

}