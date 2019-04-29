package com.coinninja.coinkeeper.receiver;

import android.content.Intent;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.service.CNGlobalMessagingService;
import com.coinninja.coinkeeper.util.android.app.JobIntentService.JobServiceScheduler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static com.coinninja.matchers.IntentMatcher.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class AuthenticationCompleteReceiverTest {

    private TestCoinKeeperApplication application;
    private AuthenticationCompleteReceiver receiver;
    private JobServiceScheduler jobServiceScheduler = mock(JobServiceScheduler.class);

    @Before
    public void setUp() throws Exception {
        application = (TestCoinKeeperApplication) RuntimeEnvironment.application;
        application.jobServiceScheduler = jobServiceScheduler;
        receiver = new AuthenticationCompleteReceiver();
    }

    @Test
    public void on_receive_start_cn_messaging_services_test() {
        ArgumentCaptor<Intent> argumentCaptor = ArgumentCaptor.forClass(Intent.class);
        receiver.onReceive(application, null);

        verify(jobServiceScheduler).enqueueWork(eq(application),
                eq(CNGlobalMessagingService.class),
                eq(JobServiceScheduler.GLOBAL_MESSAGING_SERVICE_JOB_ID),
                argumentCaptor.capture());

        Intent intent = new Intent(application, CNGlobalMessagingService.class);
        assertThat(argumentCaptor.getValue(), equalTo(intent));
    }
}