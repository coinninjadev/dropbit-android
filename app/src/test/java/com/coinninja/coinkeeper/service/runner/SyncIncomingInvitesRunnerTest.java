package com.coinninja.coinkeeper.service.runner;

import android.content.Intent;

import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SyncIncomingInvitesRunnerTest {

    @Mock
    private GetIncomingInviteRunner getIncomingInviteRunner;
    @Mock
    private IncomingInviteResponder incomingInviteResponder;
    @Mock
    private LocalBroadCastUtil localBroadCastUtil;

    @InjectMocks
    private SyncIncomingInvitesRunner runner;

    @After
    public void tearDown() {
        runner = null;
        localBroadCastUtil = null;
        incomingInviteResponder = null;
        getIncomingInviteRunner = null;
    }

    @Test
    public void handle_intent_start_runners_test() {
        runner.run();

        verify(getIncomingInviteRunner).run();
        verify(incomingInviteResponder).run();
        verify(localBroadCastUtil).sendBroadcast(any(Intent.class));
    }

}