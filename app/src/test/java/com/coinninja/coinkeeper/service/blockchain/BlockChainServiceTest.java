package com.coinninja.coinkeeper.service.blockchain;

import android.app.Application;
import android.os.Handler;

import com.coinninja.coinkeeper.service.runner.CurrentBTCStateRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;


@RunWith(RobolectricTestRunner.class)
public class BlockChainServiceTest {

    @Mock
    CurrentBTCStateRunner currentBTCStateRunner;
    @Mock
    Handler threadHandler;

    private BlockChainService service;
    private Application application;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        application = RuntimeEnvironment.application;
        service = Robolectric.setupService(BlockChainService.class);
        service.currentBTCStateRunner = currentBTCStateRunner;
        service.threadHandler = threadHandler;
    }

    @Test
    public void on_bind_reutrns_block_chain_binder() {
        assertNotNull(service.onBind(null));
        assertThat(service.onBind(null), equalTo(service.binder));
    }

    @Test
    public void runs_current_state_runner_in_background() {
        ArgumentCaptor<Runnable> argumentCaptor = ArgumentCaptor.forClass(Runnable.class);

        service.fetchCurrentState();

        verify(threadHandler).post(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue(), equalTo(currentBTCStateRunner));
    }

}