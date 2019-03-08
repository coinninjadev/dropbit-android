package com.coinninja.coinkeeper;

import com.coinninja.coinkeeper.receiver.ApplicationStartedReceiver;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.view.activity.SplashActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricTestRunner.class)
public class CoinKeeperApplicationTest {

    @Mock
    LocalBroadCastUtil localBroadCastUtil;

    CoinKeeperApplication coinKeeperApplication;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        coinKeeperApplication = new TestableCoinkeeperApplication();
        coinKeeperApplication.localBroadCastUtil = localBroadCastUtil;
    }

    @Test
    public void register_coinKeeperLifecycleListener_for_ActivityLifecycleCallbacks() {
        coinKeeperApplication = (CoinKeeperApplication) RuntimeEnvironment.application;
        CoinKeeperLifecycleListener mockCoinKeeperLifecycleListener = coinKeeperApplication.coinKeeperLifecycleListener;

        coinKeeperApplication.onCreate();
        Robolectric.setupActivity(SplashActivity.class);

        verify(mockCoinKeeperLifecycleListener, times(2)).onActivityCreated(any(), any());
    }

    @Test
    public void broadcasts_on_application_start() {
        coinKeeperApplication.onCreate();

        verify(localBroadCastUtil).sendGlobalBroadcast(ApplicationStartedReceiver.class, Intents.ACTION_ON_APPLICATION_START);
    }

    class TestableCoinkeeperApplication extends CoinKeeperApplication {

        @Override
        protected void createComponent() {
        }

        @Override
        protected void registerNotificationChannels() {
        }

    }

}