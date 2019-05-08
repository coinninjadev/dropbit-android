package com.coinninja.coinkeeper.cn.dropbit;

import android.content.Intent;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.transaction.TransactionNotificationManager;
import com.coinninja.coinkeeper.model.db.TransactionNotification;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class DropBitServiceTest {

    @Mock
    DropBitCancellationManager manager;
    @Mock
    LocalBroadCastUtil localBroadCastUtil;
    @Mock
    WalletHelper walletHelper;
    @Mock
    TransactionNotificationManager transactionNotificationManager;

    private DropBitService dropBitService;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        dropBitService = Robolectric.setupService(DropBitService.class);
        dropBitService.dropBitCancellationManager = manager;
        dropBitService.localBroadCastUtil = localBroadCastUtil;
    }

    @After
    public void tearDown(){
        dropBitService = null;
        manager = null;
        localBroadCastUtil = null;
    }

    @Test
    public void cancels_DropBit_by_id(){
        Intent intent = new Intent();
        String inviteId = "--invite id";
        intent.setAction(DropbitIntents.ACTION_CANCEL_DROPBIT);
        intent.putExtra(DropbitIntents.EXTRA_INVITATION_ID, inviteId);
        dropBitService.onHandleIntent(intent);

        verify(manager).markAsCanceled(inviteId);
    }

    @Test
    public void null_noop_without_invitation_id(){
        Intent intent = new Intent();
        intent.setAction(DropbitIntents.ACTION_CANCEL_DROPBIT);
        dropBitService.onHandleIntent(intent);

        verify(manager, times(0)).markAsCanceled(anyString());
    }

    @Test
    public void notifies_observers_that_invite_canceled() {
        Intent intent = new Intent();
        String inviteId = "--invite id";
        intent.setAction(DropbitIntents.ACTION_CANCEL_DROPBIT);
        intent.putExtra(DropbitIntents.EXTRA_INVITATION_ID, inviteId);
        dropBitService.onHandleIntent(intent);

        verify(localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_TRANSACTION_DATA_CHANGED);
    }

    @Test
    public void null_noop_without_memo_string(){
        Intent intent = new Intent();
        intent.setAction(DropbitIntents.ACTION_CREATE_NOTIFICATION);
        intent.putExtra(DropbitIntents.EXTRA_DROPBIT_TXID, "");
        dropBitService.onHandleIntent(intent);

        verify(walletHelper, times(0)).getTransactionByTxid("");
    }

    @Test
    public void null_noop_without_txid() {
        Intent intent = new Intent();
        intent.setAction(DropbitIntents.ACTION_CREATE_NOTIFICATION);
        intent.putExtra(DropbitIntents.EXTRA_DROPBIT_MEMO, "");
        dropBitService.onHandleIntent(intent);

        verify(walletHelper, times(0)).getTransactionByTxid("");
    }
}