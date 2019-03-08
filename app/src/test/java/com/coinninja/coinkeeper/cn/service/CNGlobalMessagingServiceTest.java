package com.coinninja.coinkeeper.cn.service;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.service.runner.CNGlobalMessagesRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class CNGlobalMessagingServiceTest {

    private CNGlobalMessagingService service;
    private CNGlobalMessagesRunner cnGlobalMessagesRunner;


    @Before
    public void setUp() throws Exception {

        cnGlobalMessagesRunner = mock(CNGlobalMessagesRunner.class);
        service = Robolectric.setupService(CNGlobalMessagingService.class);
        service.cnGlobalMessagesRunner = cnGlobalMessagesRunner;
    }

    @After
    public void tearDown() throws Exception {
        service = null;
        cnGlobalMessagesRunner = null;
    }

    @Test
    public void onHandleIntent_call_run_on_runnable_test() {

        service.onHandleWork(null);

        verify(cnGlobalMessagesRunner).run();
    }
}