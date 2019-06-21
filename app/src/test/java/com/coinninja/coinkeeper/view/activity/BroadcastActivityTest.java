package com.coinninja.coinkeeper.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.coinninja.bindings.DerivationPath;
import com.coinninja.bindings.TransactionBroadcastResult;
import com.coinninja.bindings.TransactionData;
import com.coinninja.bindings.UnspentTransactionOutput;
import com.coinninja.bindings.model.Transaction;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager;
import com.coinninja.coinkeeper.model.Identity;
import com.coinninja.coinkeeper.model.db.enums.IdentityType;
import com.coinninja.coinkeeper.model.dto.BroadcastTransactionDTO;
import com.coinninja.coinkeeper.model.dto.CompletedBroadcastDTO;
import com.coinninja.coinkeeper.presenter.activity.BroadcastTransactionPresenter;
import com.coinninja.coinkeeper.service.BroadcastTransactionService;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.view.activity.BroadcastActivity.SendState;
import com.coinninja.coinkeeper.view.progress.SendingProgressView;
import com.coinninja.matchers.ActivityMatchers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static junit.framework.Assert.assertNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;


@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class BroadcastActivityTest {
    @Mock
    private BroadcastTransactionPresenter broadcastPresenter;
    @Mock
    private SendingProgressView sendingProgressView;
    private BroadcastTransactionDTO broadcastActivityDTO;
    @Mock
    private ActivityNavigationUtil navigationUtil;
    @Mock
    private SyncWalletManager syncWalletManager;

    private ActivityController<BroadcastActivity> activityController;
    private BroadcastActivity activity;
    private ShadowActivity shadowActivity;
    private TransactionData transactionData;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Identity identity = new Identity(IdentityType.PHONE, "+13305551111", "--hash--", "Joe", "", false, null);
        UnspentTransactionOutput[] outputs = new UnspentTransactionOutput[2];
        transactionData = new TransactionData(
                outputs,
                1000100L,
                100L, 50000L,
                new DerivationPath("m49/0/0/0/0/1"),
                "--payment-address");
        broadcastActivityDTO = new BroadcastTransactionDTO(transactionData, true, "memo", identity, "--pub-key--");
        broadcastActivityDTO.setMemoShared(true);
        broadcastActivityDTO.setMemo("memo");

        Intent intent = new Intent();
        intent.putExtra(DropbitIntents.EXTRA_BROADCAST_DTO, broadcastActivityDTO);
        activityController = Robolectric.buildActivity(BroadcastActivity.class, intent);
        activityController.create();
        activity = activityController.get();
        shadowActivity = shadowOf(activity);

        activity.broadcastPresenter = broadcastPresenter;
        activity.sendingProgressView = sendingProgressView;
        activity.activityNavigationUtil = navigationUtil;
        activity.syncWalletManager = syncWalletManager;
    }

    @After
    public void tearDown() {
        broadcastPresenter = null;
        broadcastActivityDTO = null;
        sendingProgressView = null;
        navigationUtil = null;
        activityController = null;
        activity = null;
        shadowActivity = null;
        transactionData = null;
        syncWalletManager = null;
    }

    @Test
    public void broadcastTransaction() {
        activityController.start().resume().visible();

        verify(broadcastPresenter).broadcastTransaction(transactionData);
        assertThat(activity.sendState, equalTo(SendState.STARTED));
    }

    @Test
    public void stops_sync_when_broadcasting() {
        activityController.start().resume().visible();

        verify(syncWalletManager).cancel30SecondSync();
    }

    @Test
    public void starts_sync_when_leaving_screen() {
        activityController.start().resume().visible();

        activityController.pause().stop().destroy();

        verify(syncWalletManager).schedule30SecondSync();
    }

    @Test
    public void show_ui_elements_on_successful_broadcast() {
        activityController.start().resume().visible();
        TransactionBroadcastResult transactionBroadcastResult = mock(TransactionBroadcastResult.class);
        when(transactionBroadcastResult.getTxId()).thenReturn("--txid--");

        activity.showBroadcastSuccessful(transactionBroadcastResult);

        verify(sendingProgressView).setProgress(100);
        verify(sendingProgressView).completeSuccess();

        TextView sendLabel = activity.findViewById(R.id.broadcast_sending_progress_label);
        assertThat(sendLabel.getVisibility(), equalTo(View.VISIBLE));
        assertThat(sendLabel.getText().toString(), equalTo(activity.getString(R.string.broadcast_sent_label)));
        assertThat(activity.sendState, equalTo(SendState.COMPLETED_SUCCESS));
    }

    @Test
    public void start_save_transaction_service_on_successful_broadcast() {
        activityController.start().resume().visible();
        TransactionBroadcastResult mockTransactionBroadcastResult = mock(TransactionBroadcastResult.class);
        when(mockTransactionBroadcastResult.getTxId()).thenReturn("--txid--");

        Intent intent = new Intent(activity, BroadcastTransactionService.class);
        CompletedBroadcastDTO completedDto = new CompletedBroadcastDTO(broadcastActivityDTO, "--txid--");
        intent.putExtra(DropbitIntents.EXTRA_COMPLETED_BROADCAST_DTO, completedDto);

        activity.showBroadcastSuccessful(mockTransactionBroadcastResult);

        assertThat(activity, ActivityMatchers.serviceWithIntentStarted(intent));
    }

    @Test
    public void broadcastPresenter_attach() {
        activityController.start().resume().visible();

        verify(broadcastPresenter).attachView(activity);
    }

    @Test
    public void showInitTransaction() {
        activityController.start().resume();

        activity.showInitTransaction();

        verify(sendingProgressView, times(2)).setProgress(0);
        verify(sendingProgressView, times(2)).resetView();
    }

    @Test
    public void navigates_home_clearing_task_when_acknowleging_broadcast_state() {
        Bundle inState = new Bundle();
        inState.putInt(BroadcastActivity.RESTORE_STATE, SendState.COMPLETED_SUCCESS.getValue());
        inState.putString(BroadcastActivity.TRANSACTION_ID, "__txid__");
        activityController.start().restoreInstanceState(inState).resume().visible();

        View transactionCompleteButton = activity.findViewById(R.id.transaction_complete_action_button);
        transactionCompleteButton.callOnClick();

        verify(navigationUtil).navigateToHome(activity);
    }

    @Test
    public void showProgress() {

        activity.showProgress(22);

        verify(sendingProgressView).setProgress(22);
    }

    @Test
    public void handles_failed_sends() {
        activityController.start().resume().visible();
        TransactionBroadcastResult mockTransactionBroadcastResult = mock(TransactionBroadcastResult.class);
        activity.showBroadcastFail(mockTransactionBroadcastResult);

        TextView sendLabel = activity.findViewById(R.id.broadcast_sending_progress_label);
        assertThat(sendLabel.getVisibility(), equalTo(View.VISIBLE));
        assertThat(sendLabel.getText().toString(), equalTo(activity.getString(R.string.broadcast_sent_failed)));
        assertThat(activity.sendState, equalTo(SendState.COMPLETED_FAILED));

        assertNull(shadowActivity.getNextStartedService());
    }

    @Test
    public void transactionSuccessful() {
        activityController.start().resume().visible();
        TransactionBroadcastResult transactionBroadcastResult = mock(TransactionBroadcastResult.class);
        when(transactionBroadcastResult.getTxId()).thenReturn("--txid--");

        activity.showBroadcastSuccessful(transactionBroadcastResult);

        verify(sendingProgressView).setProgress(100);
        verify(sendingProgressView).completeSuccess();

        TextView sendLabel = activity.findViewById(R.id.broadcast_sending_progress_label);
        assertThat(sendLabel.getVisibility(), equalTo(View.VISIBLE));
        assertThat(sendLabel.getText().toString(), equalTo(activity.getString(R.string.broadcast_sent_label)));
        assertThat(activity.sendState, equalTo(SendState.COMPLETED_SUCCESS));

        Intent intent = shadowActivity.getNextStartedService();
        assertThat(intent.getComponent().getClassName(), equalTo(BroadcastTransactionService.class.getName()));
    }

    @Test
    public void restores_from_state__fail() {
        Bundle inState = new Bundle();
        inState.putInt(BroadcastActivity.RESTORE_STATE, SendState.COMPLETED_FAILED.getValue());
        activityController.start().restoreInstanceState(inState).resume();

        verify(broadcastPresenter, times(0)).broadcastTransaction(transactionData);
        TextView sendLabel = activity.findViewById(R.id.broadcast_sending_progress_label);
        assertThat(sendLabel.getVisibility(), equalTo(View.VISIBLE));
        assertThat(sendLabel.getText().toString(), equalTo(activity.getString(R.string.broadcast_sent_failed)));
        assertThat(activity.sendState, equalTo(SendState.COMPLETED_FAILED));
    }

    @Test
    public void restores_from_state__success() {
        Bundle inState = new Bundle();
        inState.putInt(BroadcastActivity.RESTORE_STATE, SendState.COMPLETED_SUCCESS.getValue());
        inState.putString(BroadcastActivity.TRANSACTION_ID, "__txid__");
        activityController.start().restoreInstanceState(inState).resume().visible();

        verify(broadcastPresenter, times(0)).broadcastTransaction(transactionData);
        TextView sendLabel = activity.findViewById(R.id.broadcast_sending_progress_label);
        assertThat(sendLabel.getVisibility(), equalTo(View.VISIBLE));
        assertThat(sendLabel.getText().toString(), equalTo(activity.getString(R.string.broadcast_sent_label)));
        assertThat(((TextView) activity.findViewById(R.id.transaction_id_link)).getText().toString(),
                equalTo("__txid__"));
        assertThat(activity.sendState, equalTo(SendState.COMPLETED_SUCCESS));
    }

    @Test
    public void saves_send_state_on_save() {
        TransactionBroadcastResult transactionResult = new TransactionBroadcastResult(200, true, "foo", new Transaction("", "__txid__"));
        activityController.start().resume();
        activity.showBroadcastSuccessful(transactionResult);

        Bundle outState = new Bundle();
        activity.onSaveInstanceState(outState, null);

        assertThat(SendState.valueOf(outState.getInt(BroadcastActivity.RESTORE_STATE)), equalTo(SendState.COMPLETED_SUCCESS));
        assertThat(outState.getString(BroadcastActivity.TRANSACTION_ID), equalTo("__txid__"));
    }

}