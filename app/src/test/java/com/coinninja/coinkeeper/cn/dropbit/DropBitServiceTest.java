package com.coinninja.coinkeeper.cn.dropbit;

import android.content.Intent;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.transaction.TransactionNotificationManager;
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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class DropBitServiceTest {

    @Mock
    DropBitMeServiceManager dropBitMeServiceManager;
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
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        dropBitService = Robolectric.setupService(DropBitService.class);
        dropBitService.dropBitCancellationManager = manager;
        dropBitService.localBroadCastUtil = localBroadCastUtil;
        dropBitService.dropBitMeServiceManager = dropBitMeServiceManager;
    }

    @After
    public void tearDown() {
        dropBitService = null;
        manager = null;
        localBroadCastUtil = null;
    }

    @Test
    public void cancels_DropBit_by_id() {
        Intent intent = new Intent(DropbitIntents.ACTION_CANCEL_DROPBIT);
        String inviteId = "--invite id";
        intent.setAction(DropbitIntents.ACTION_CANCEL_DROPBIT);
        intent.putExtra(DropbitIntents.EXTRA_INVITATION_ID, inviteId);
        dropBitService.onHandleIntent(intent);

        verify(manager).markAsCanceled(inviteId);
        verify(localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_TRANSACTION_DATA_CHANGED);
    }

    @Test
    public void null_noop_without_invitation_id() {
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
    }

    @Test
    public void disables_dropbit_me() {
        Intent intent = new Intent(DropbitIntents.ACTION_DROPBIT_ME_DISABLE_ACCOUNT);
        dropBitService.onHandleIntent(intent);

        verify(dropBitMeServiceManager).disableAccount();
    }

    @Test
    public void enables_dropbit_me() {
        Intent intent = new Intent(DropbitIntents.ACTION_DROPBIT_ME_ENABLE_ACCOUNT);

        dropBitService.onHandleIntent(intent);

        verify(dropBitMeServiceManager).enableAccount();
    }

    @Test
    public void null_noop_without_memo_string() {
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