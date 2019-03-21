package com.coinninja.coinkeeper.view.activity;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.ui.transaction.details.TransactionDetailsActivity;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction;
import com.coinninja.coinkeeper.view.adapter.util.TransactionAdapterUtil;

import org.greenrobot.greendao.query.LazyList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import androidx.recyclerview.widget.RecyclerView;

import static com.coinninja.matchers.ActivityMatchers.activityWithIntentStarted;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class TransactionHistoryActivityTest {

    private TransactionHistoryActivity activity;

    private ActivityController<TransactionHistoryActivity> activityController;
    private BindableTransaction bindableTransaction;

    @Mock
    private TransactionAdapterUtil util;
    @Mock
    private WalletHelper walletHelper;
    @Mock
    private TransactionsInvitesSummary transaction;
    @Mock
    private LazyList<TransactionsInvitesSummary> transactions;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        bindableTransaction = new BindableTransaction();
        when(transactions.size()).thenReturn(12);
        when(transactions.get(anyInt())).thenReturn(transaction);
        when(walletHelper.getTransactionsLazily()).thenReturn(transactions);
        when(transactions.isEmpty()).thenReturn(false);
        when(util.translateTransaction(any(TransactionsInvitesSummary.class))).thenReturn(bindableTransaction);
        activityController = Robolectric.buildActivity(TransactionHistoryActivity.class).create();
        activity = activityController.get();
        activity.transactionAdapterUtil = util;
        activity.walletHelper = walletHelper;

        setupReceive();
    }

    @After
    public void tearDown() throws Exception {
        activity = null;
        transaction = null;
        walletHelper = null;
        activityController = null;
        transactions = null;
        bindableTransaction = null;
        util = null;
    }

    public void setupReceive() {
        bindableTransaction.setSendState(BindableTransaction.SendState.RECEIVE);
        bindableTransaction.setConfirmationState(BindableTransaction.ConfirmationState.CONFIRMED);
        bindableTransaction.setTxTime("April 24, 2018 01:24am");
        bindableTransaction.setValue(340520465L);
        bindableTransaction.setFundingAddress("1PyWmpjXkPftUGSbz9nrHbK4EoCPhKi6pg");
        bindableTransaction.setTargetAddress("3FVXXXLtej9x7kzHgB51WGXpPXkdfEUBjS");
        bindableTransaction.setFee(30465);
    }

    private void startActivity() {
        activityController.start().resume().visible();
    }

    @Test
    public void shows_detail_of_transaction_from_Creation_intent() {
        Intent intent = new Intent();
        intent.putExtra(Intents.EXTRA_TRANSACTION_ID, "--TXID--");
        activity.setIntent(intent);

        startActivity();

        Intent i = new Intent(activity, TransactionDetailsActivity.class);
        i.putExtra(Intents.EXTRA_TRANSACTION_ID, "--TXID--");
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        assertThat(activity, activityWithIntentStarted(i));
        assertFalse(activity.getIntent().hasExtra(Intents.EXTRA_TRANSACTION_ID));
    }

    @Test
    public void shows_transaction_detail_when_history_item_selected() {
        TransactionsInvitesSummary summary = mock(TransactionsInvitesSummary.class);
        when(summary.getId()).thenReturn(4L);
        when(transactions.get(3)).thenReturn(summary);
        startActivity();

        activity.onItemClick(null, 3);

        Intent intent = new Intent(activity, TransactionDetailsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(Intents.EXTRA_TRANSACTION_RECORD_ID, 4L);
        assertThat(activity, activityWithIntentStarted(intent));
    }

    @Test
    public void shows_transactions_when_syncing_no_transactions_to_some_transactions() {
        when(transactions.isEmpty()).thenReturn(true);
        when(transactions.size()).thenReturn(0);
        startActivity();
        RecyclerView list = activity.findViewById(R.id.transaction_history);
        assertThat(list.getAdapter().getItemCount(), equalTo(0));
        assertThat(list.getVisibility(), equalTo(View.GONE));


        LazyList<TransactionsInvitesSummary> updatedTransactions = mock(LazyList.class);
        when(updatedTransactions.size()).thenReturn(1);
        when(updatedTransactions.get(anyInt())).thenReturn(transaction);
        when(walletHelper.getTransactionsLazily()).thenReturn(updatedTransactions);

        activity.onWalletSyncComplete();

        assertThat(list.getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void refreshes_transactions_when_sync_completed() {
        startActivity();
        RecyclerView list = activity.findViewById(R.id.transaction_history);

        verify(walletHelper, times(1)).getTransactionsLazily();
        assertThat(list.getAdapter().getItemCount(), equalTo(12));


        LazyList<TransactionsInvitesSummary> updatedTransactions = mock(LazyList.class);
        when(updatedTransactions.size()).thenReturn(13);
        when(updatedTransactions.get(anyInt())).thenReturn(transaction);
        when(walletHelper.getTransactionsLazily()).thenReturn(updatedTransactions);

        activity.onWalletSyncComplete();

        assertThat(list.getAdapter().getItemCount(), equalTo(13));
        verify(walletHelper, times(2)).getTransactionsLazily();
    }

    @Test
    public void sets_has_fixed_size_for_efficency() {
        startActivity();
        assertTrue(((RecyclerView) activity.findViewById(R.id.transaction_history)).hasFixedSize());
    }

    @Test
    public void setsAdapterForPresentingListingOfInforamtion() {
        startActivity();

        RecyclerView list = activity.findViewById(R.id.transaction_history);

        assertNotNull(list.getAdapter());
    }

    @Test
    public void closes_cursor_when_stopped() {
        startActivity();

        activityController.stop();

        verify(transactions).close();
    }

    @Test
    public void shows_empty_state_view() {
        when(transactions.isEmpty()).thenReturn(true);
        startActivity();

        View empty = activity.findViewById(R.id.empty_transaction_history);
        View list = activity.findViewById(R.id.transaction_history);

        assertNotNull(empty);
        assertThat(empty.getVisibility(), equalTo(View.VISIBLE));
        assertThat(list.getVisibility(), equalTo(View.GONE));
    }


    @Test
    public void has_list_view_for_transactions() {
        startActivity();

        View empty = activity.findViewById(R.id.empty_transaction_history);
        View list = activity.findViewById(R.id.transaction_history);

        assertNotNull(list);
        assertThat(list.getVisibility(), equalTo(View.VISIBLE));
        assertThat(empty.getVisibility(), equalTo(View.GONE));
    }


    //TODO: this is a terrible way to assert we refresh transactions
    @Test
    public void refreshes_transactions_on_transaction_data_change() {
        startActivity();

        RecyclerView list = activity.findViewById(R.id.transaction_history);

        verify(walletHelper, times(1)).getTransactionsLazily();
        assertThat(list.getAdapter().getItemCount(), equalTo(12));

        LazyList<TransactionsInvitesSummary> updatedTransactions = mock(LazyList.class);
        when(updatedTransactions.size()).thenReturn(13);
        when(updatedTransactions.get(anyInt())).thenReturn(transaction);
        when(walletHelper.getTransactionsLazily()).thenReturn(updatedTransactions);

        activity.receiver.onReceive(activity, new Intent(Intents.ACTION_TRANSACTION_DATA_CHANGED));

        assertThat(list.getAdapter().getItemCount(), equalTo(13));
        verify(walletHelper, times(2)).getTransactionsLazily();
    }

    @Test
    public void subscribes_to_transaction_data_change() {
        startActivity();

        ArgumentCaptor<IntentFilter> intentFilterArgumentCaptor = ArgumentCaptor.forClass(IntentFilter.class);
        LocalBroadCastUtil broadCastUtil = mock(LocalBroadCastUtil.class);
        activity.localBroadCastUtil = broadCastUtil;

        activity.onResume();

        verify(broadCastUtil).registerReceiver(any(BroadcastReceiver.class), intentFilterArgumentCaptor.capture());
        assertThat(intentFilterArgumentCaptor.getValue().getAction(0), equalTo(Intents.ACTION_TRANSACTION_DATA_CHANGED));
    }

    @Test
    public void unsubscribes_from_transaction_data_changed_when_paused() {
        startActivity();
        LocalBroadCastUtil broadCastUtil = mock(LocalBroadCastUtil.class);
        activity.localBroadCastUtil = broadCastUtil;

        activity.onPause();

        verify(broadCastUtil).unregisterReceiver(any(BroadcastReceiver.class));
    }
}