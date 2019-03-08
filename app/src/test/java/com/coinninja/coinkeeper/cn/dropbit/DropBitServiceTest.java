package com.coinninja.coinkeeper.cn.dropbit;

import android.content.Intent;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.util.Intents;
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
    DropBitCancellationManager manager;
    @Mock
    LocalBroadCastUtil localBroadCastUtil;

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
        intent.putExtra(Intents.EXTRA_INVITATION_ID, inviteId);
        dropBitService.onHandleIntent(intent);

        verify(manager).markAsCanceled(inviteId);
    }

    @Test
    public void null_noop(){
        Intent intent = new Intent();
        dropBitService.onHandleIntent(intent);

        verify(manager, times(0)).markAsCanceled(anyString());
    }

    @Test
    public void notifies_observers_that_invite_canceled() {
        Intent intent = new Intent();
        String inviteId = "--invite id";
        intent.putExtra(Intents.EXTRA_INVITATION_ID, inviteId);
        dropBitService.onHandleIntent(intent);

        verify(localBroadCastUtil).sendBroadcast(Intents.ACTION_TRANSACTION_DATA_CHANGED);
    }

}