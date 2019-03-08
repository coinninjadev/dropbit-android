package com.coinninja.coinkeeper.service;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.service.runner.FullSyncWalletRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.verify;


@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class WalletTransactionRetrieverServiceTest {

    @Mock
    FullSyncWalletRunner fullSyncWalletRunner;

    private WalletTransactionRetrieverService service;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        service = Robolectric.setupService(WalletTransactionRetrieverService.class);
        service.fullSyncWalletRunner = fullSyncWalletRunner;
    }

    @After
    public void tearDown() {
        fullSyncWalletRunner = null;
        service = null;
    }

    @Test
    public void executes_full_sync_on_handle_work() {
        service.onHandleWork(null);

        verify(fullSyncWalletRunner).run();
    }
}