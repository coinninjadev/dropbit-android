package com.coinninja.coinkeeper.service.runner;

import com.coinninja.bindings.TransactionData;
import com.coinninja.coinkeeper.cn.transaction.TransactionNotificationManager;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.db.TransactionNotification;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.dto.BroadcastTransactionDTO;
import com.coinninja.coinkeeper.model.dto.CompletedBroadcastDTO;
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.util.analytics.Analytics;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SaveTransactionRunnerTest {
    @Mock
    TransactionData transactionData;
    @Mock
    private TransactionNotificationManager transactionNotificationManager;
    private String sampleTransactionID;
    private CompletedBroadcastDTO completedBroadcastActivityDTO;
    @Mock
    private TransactionHelper transactionHelper;
    @Mock
    private DaoSessionManager daoManager;
    @Mock
    private Analytics analytics;
    @Mock
    private CNWalletManager cnWalletManager;
    @Mock
    private SyncWalletManager syncWalletManager;
    @InjectMocks
    private SaveTransactionRunner runner;

    @Mock
    private TransactionSummary transactionSummary;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        sampleTransactionID = "---TXID---";

        BroadcastTransactionDTO broadcastActivityDTO = new BroadcastTransactionDTO(transactionData, null, false, null, null);
        completedBroadcastActivityDTO = new CompletedBroadcastDTO(broadcastActivityDTO, sampleTransactionID);
        runner.setCompletedBroadcastActivityDTO(completedBroadcastActivityDTO);

        when(transactionHelper.createInitialTransactionForCompletedBroadcast(completedBroadcastActivityDTO)).thenReturn(transactionSummary);
    }

    @Test
    public void when_runner_runs_track_mixpanel_event_test() throws JSONException {
        runner.run();
        ArgumentCaptor<JSONObject> captor = ArgumentCaptor.forClass(JSONObject.class);

        verify(analytics).trackEvent(eq(Analytics.EVENT_SENT_SHARED_PAYLOAD), captor.capture());

        JSONObject relatedValues = captor.getValue();
        assertThat(relatedValues.get(Analytics.EVENT_MEMO_JSON_KEY_DID_SHARE), equalTo("false"));
    }

    @Test
    public void when_runner_runs_updates_spent_transactions_test() {
        runner.run();

        verify(transactionHelper).createInitialTransactionForCompletedBroadcast(completedBroadcastActivityDTO);
        verify(syncWalletManager).syncNow();
    }

    @Test
    public void when_runner_runs_update_balances_test() {
        runner.run();

        verify(cnWalletManager).updateBalances();
    }

    @Test
    public void sends_transaction_notification_when_memo_is_shared() {
        TransactionNotification transactionNotification = mock(TransactionNotification.class);
        when(daoManager.newTransactionNotification()).thenReturn(transactionNotification);
        completedBroadcastActivityDTO.setMemo("--memo--");
        completedBroadcastActivityDTO.setContact(new Contact(new PhoneNumber("+12223334444"), "Joe", true));
        completedBroadcastActivityDTO.setPublicKey("--pubkey--");
        completedBroadcastActivityDTO.setMemoShared(true);

        runner.run();

        verify(transactionNotificationManager).sendTransactionNotificationToReceiver(completedBroadcastActivityDTO);
    }

    @Test
    public void does_not_send_transaction_notification_when_memo_is_not_shared() {
        TransactionNotification transactionNotification = mock(TransactionNotification.class);
        when(daoManager.newTransactionNotification()).thenReturn(transactionNotification);
        completedBroadcastActivityDTO.setMemo("--memo--");
        completedBroadcastActivityDTO.setPublicKey("--pubkey--");
        completedBroadcastActivityDTO.setMemoShared(false);

        runner.run();

        completedBroadcastActivityDTO.setMemo("");
        runner.run();

        completedBroadcastActivityDTO.setPublicKey("");
        runner.run();

        verify(transactionNotificationManager, times(2))
                .notifyOfPayment(completedBroadcastActivityDTO);
    }

    @Test
    public void does_not_save_transaction_notification_when_memo_does_not_exist() {
        TransactionNotification transactionNotification = mock(TransactionNotification.class);
        when(daoManager.newTransactionNotification()).thenReturn(transactionNotification);
        completedBroadcastActivityDTO.setMemo("");
        completedBroadcastActivityDTO.setPublicKey("");

        runner.run();

        verify(transactionNotificationManager, times(0)).saveTransactionNotificationLocally(any(TransactionSummary.class), any());
    }

    @Test
    public void saves_transaction_notification_when_memo_exists() {
        TransactionNotification transactionNotification = mock(TransactionNotification.class);
        when(daoManager.newTransactionNotification()).thenReturn(transactionNotification);
        completedBroadcastActivityDTO.setMemo("--memo--");
        completedBroadcastActivityDTO.setPublicKey("--pubkey--");

        runner.run();

        verify(transactionNotificationManager).saveTransactionNotificationLocally(transactionSummary, completedBroadcastActivityDTO);
    }

}