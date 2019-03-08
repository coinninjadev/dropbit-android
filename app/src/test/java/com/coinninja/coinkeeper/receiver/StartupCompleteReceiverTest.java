package com.coinninja.coinkeeper.receiver;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.util.analytics.Analytics;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class StartupCompleteReceiverTest {

    @Test
    public void reports_application_foregrounded() {
        StartupCompleteReceiver receiver = new StartupCompleteReceiver();

        receiver.onReceive(RuntimeEnvironment.application, null);

        verify(receiver.analytics).trackEvent(Analytics.EVENT_APP_OPEN);
        verify(receiver.analytics).flush();
    }

}