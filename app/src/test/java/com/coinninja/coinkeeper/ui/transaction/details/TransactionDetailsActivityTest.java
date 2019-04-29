package com.coinninja.coinkeeper.ui.transaction.details;

import android.content.Intent;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.transaction.DefaultCurrencyChangeViewNotifier;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import androidx.viewpager.widget.ViewPager;

import static com.coinninja.android.helpers.Views.withId;
import static com.coinninja.matchers.ActivityMatchers.hasViewWithId;
import static com.coinninja.matchers.IntentFilterMatchers.containsAction;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

@RunWith(RobolectricTestRunner.class)
public class TransactionDetailsActivityTest {

    @Mock
    private TransactionDetailDialogController transactionDetailDialogController;

    @Mock
    private TransactionDetailPageAdapter pageAdapter;

    @Mock
    DefaultCurrencyChangeViewNotifier defaultCurrencyChangeViewNotifier;

    private ActivityController<TransactionDetailsActivity> activityController;
    private TransactionDetailsActivity activity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        activityController = Robolectric.buildActivity(TransactionDetailsActivity.class).create();
        activity = activityController.get();
        activity.pageAdapter = pageAdapter;
        activity.defaultCurrencyChangeViewNotifier = defaultCurrencyChangeViewNotifier;
        activity.transactionDetailDialogController = transactionDetailDialogController;
    }

    private void startUp() {
        activityController.start().resume().visible();
    }

    @After
    public void tearDown() {
        activityController.destroy();
        activity = null;
        activityController = null;
        transactionDetailDialogController = null;
    }

    @Test
    public void instructs_adapter_to_teardown_when_stopped() {
        startUp();

        activityController.pause().stop();

        verify(pageAdapter).tearDown();
    }

    @Test
    public void requests_adapter_to_refresh_data_when_resumed() {
        startUp();
        verify(pageAdapter).refreshData();

        activityController.pause().stop().restart().resume();

        verify(pageAdapter, times(2)).refreshData();
    }

    @Test
    public void refreshes_when_wallet_sync_complete() {
        startUp();

        verify(pageAdapter).refreshData();

        activity.receiver.onReceive(activity, new Intent(DropbitIntents.ACTION_WALLET_SYNC_COMPLETE));

        verify(pageAdapter, times(2)).refreshData();
    }

    @Test
    public void refreshes_when_dropbit_canceled() {
        activity.pager = mock(ViewPager.class);

        activity.receiver.onReceive(activity, new Intent(DropbitIntents.ACTION_TRANSACTION_DATA_CHANGED));

        verify(pageAdapter, atLeast(1)).refreshData();
        verify(activity.pager).setAdapter(pageAdapter);
    }

    @Test
    public void maintains_current_position_when_data_changes() {
        startUp();
        activity.pager = mock(ViewPager.class);
        when(activity.pager.getCurrentItem()).thenReturn(2);
        when(pageAdapter.getTransactionIdForIndex(2)).thenReturn(3L);
        when(pageAdapter.lookupTransactionById(3L)).thenReturn(3);

        activity.receiver.onReceive(activity, new Intent(DropbitIntents.ACTION_WALLET_SYNC_COMPLETE));

        verify(activity.pager).setCurrentItem(3);
    }

    @Test
    public void observes_local_events() {
        startUp();

        assertThat(activity.intentFilter, containsAction(DropbitIntents.ACTION_TRANSACTION_DATA_CHANGED));
        assertThat(activity.intentFilter, containsAction(DropbitIntents.ACTION_WALLET_SYNC_COMPLETE));
        assertThat(activity.intentFilter, containsAction(DropbitIntents.ACTION_CURRENCY_PREFERENCE_CHANGED));
        verify(activity.localBroadCastUtil).registerReceiver(activity.receiver, activity.intentFilter);
    }

    @Test
    public void stops_observing_dropbit_canceled_event_when_stopped() {
        startUp();

        activityController.pause().stop();

        verify(activity.localBroadCastUtil).unregisterReceiver(activity.receiver);
    }

    @Test
    public void refreshes_data_when_dropbit_canceled_event_observed() {
        startUp();

        activity.receiver.onReceive(activity, new Intent(DropbitIntents.ACTION_TRANSACTION_DATA_CHANGED));

        verify(pageAdapter, times(2)).refreshData();
    }

    @Test
    public void configures_pager_with_adapter() {
        startUp();

        assertThat(activity, hasViewWithId(R.id.pager_transaction_details));
        ViewPager pager = withId(activity, R.id.pager_transaction_details);
        assertThat(pager.getAdapter(), equalTo(pageAdapter));
    }

    @Test
    public void can_show_transaction_record_on_launch() {
        when(pageAdapter.lookupTransactionById(4L)).thenReturn(10);
        Intent newIntent = new Intent();
        newIntent.putExtra(DropbitIntents.EXTRA_TRANSACTION_RECORD_ID, 4L);
        activity.setIntent(newIntent);
        activity.pager = mock(ViewPager.class);

        startUp();

        verify(activity.pager).setCurrentItem(10, true);
        assertFalse(activity.getIntent().hasExtra(DropbitIntents.EXTRA_TRANSACTION_RECORD_ID));
    }

    @Test
    public void can_show_txid_on_launch() {
        when(pageAdapter.lookupTransactionBy("-- txid --")).thenReturn(10);
        Intent newIntent = new Intent();
        newIntent.putExtra(DropbitIntents.EXTRA_TRANSACTION_ID, "-- txid --");
        activity.setIntent(newIntent);
        activity.pager = mock(ViewPager.class);

        startUp();

        verify(activity.pager).setCurrentItem(10, true);
        assertFalse(activity.getIntent().hasExtra(DropbitIntents.EXTRA_TRANSACTION_ID));
    }

    @Test
    public void observes_transaction_detail_selection_requests() {
        startUp();

        verify(activity.pageAdapter).setShowTransactionDetailRequestObserver(activity.transactionDetailObserver);
    }

    @Test
    public void stop_removes_transaction_observer() {
        startUp();

        activityController.stop();

        verify(activity.pageAdapter).setShowTransactionDetailRequestObserver(null);
    }

    @Test
    public void registers_callback_for_showing_dialog_when_transaction_details_selected() {
        startUp();

        activityController.stop();

        verify(activity.pageAdapter).setShowTransactionDetailRequestObserver(null);
    }

    @Test
    public void shows_details_of_transaction_when_selected() {
        startUp();
        BindableTransaction transaction = mock(BindableTransaction.class);

        activity.transactionDetailObserver.onTransactionDetailsRequested(transaction);

        verify(transactionDetailDialogController).showTransaction(activity, transaction);
    }

    @Test
    public void sets_default_currency_notifier_on_page_adapter() {
        startUp();

        verify(pageAdapter).setDefaultCurrencyChangeViewNotifier(defaultCurrencyChangeViewNotifier);
    }

    @Test
    public void observes_currency_preference_change() {
        DefaultCurrencies defaultCurrencies = new DefaultCurrencies(new BTCCurrency(), new USDCurrency());
        Intent intent = new Intent(DropbitIntents.ACTION_CURRENCY_PREFERENCE_CHANGED);
        intent.putExtra(DropbitIntents.EXTRA_PREFERENCE, defaultCurrencies);
        startUp();

        activity.receiver.onReceive(activity, intent);

        verify(defaultCurrencyChangeViewNotifier).onDefaultCurrencyChanged(defaultCurrencies);
    }
}