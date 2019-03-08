package com.coinninja.coinkeeper;


import com.coinninja.coinkeeper.view.activity.SplashActivity;
import com.coinninja.coinkeeper.view.activity.StartActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class CoinKeeperLifecycleListenerTest {

    CoinKeeperLifecycleListener coinKeeperLifecycleListener;
    private final ForegroundStatusChangeReceiver receiver1 = mock(ForegroundStatusChangeReceiver.class);
    private final ForegroundStatusChangeReceiver receiver2 = mock(ForegroundStatusChangeReceiver.class);

    @Before
    public void setUp() throws Exception {
        coinKeeperLifecycleListener = new CoinKeeperLifecycleListener();
        TestCoinKeeperApplication application = (TestCoinKeeperApplication) RuntimeEnvironment.application;
        application.registerActivityLifecycleCallbacks(coinKeeperLifecycleListener);
    }

    @After
    public void tearDown() throws Exception {
        coinKeeperLifecycleListener = null;
    }

    @Test
    public void if_no_activity_has_started_isAppInForeground_should_be_false() {
        assertFalse(coinKeeperLifecycleListener.isAppInForeground());
    }

    @Test
    public void when_an_activity_has_started_isAppInForeground_should_be_true() {
        ActivityController<SplashActivity> activityController = Robolectric.buildActivity(SplashActivity.class);

        activityController.create().start().resume().visible();

        assertTrue(coinKeeperLifecycleListener.isAppInForeground());
    }

    @Test
    public void when_an_activity_has_started_then_stopped_isAppInForeground_should_be_false() {
        ActivityController<SplashActivity> activityController = Robolectric.buildActivity(SplashActivity.class);
        activityController.create().start().resume().visible();

        assertTrue(coinKeeperLifecycleListener.isAppInForeground());
        activityController.pause().stop().destroy();


        assertFalse(coinKeeperLifecycleListener.isAppInForeground());
    }

    @Test
    public void when_2_activity_have_started_but_only_1_stopped_isAppInForeground_should_still_be_true() {
        ActivityController<SplashActivity> activityController1 = Robolectric.buildActivity(SplashActivity.class);
        ActivityController<StartActivity> activityController2 = Robolectric.buildActivity(StartActivity.class);
        activityController1.create().start().resume().visible();
        activityController2.create().start().resume().visible();


        assertTrue(coinKeeperLifecycleListener.isAppInForeground());
        activityController1.pause().stop().destroy();


        assertTrue(coinKeeperLifecycleListener.isAppInForeground());
    }

    @Test
    public void when_2_activity_have_started_and_both_stopped_isAppInForeground_should_still_be_true() {
        ActivityController<SplashActivity> activityController1 = Robolectric.buildActivity(SplashActivity.class);
        ActivityController<StartActivity> activityController2 = Robolectric.buildActivity(StartActivity.class);
        activityController1.create().start().resume().visible();
        activityController2.create().start().resume().visible();


        assertTrue(coinKeeperLifecycleListener.isAppInForeground());
        activityController1.pause().stop().destroy();
        assertTrue(coinKeeperLifecycleListener.isAppInForeground());
        activityController2.pause().stop().destroy();


        assertFalse(coinKeeperLifecycleListener.isAppInForeground());
    }

    @Test
    public void registerReceiver_brought_to_foreground(){

        coinKeeperLifecycleListener.registerReceiver(receiver1);
        coinKeeperLifecycleListener.registerReceiver(receiver2);
        coinKeeperLifecycleListener.registerReceiver(null);

        coinKeeperLifecycleListener.onActivityStarted(null);

        verify(receiver1).onBroughtToForeground();
        verify(receiver2).onBroughtToForeground();
    }

    @Test
    public void registerReceiver_not_foregrounded(){

        coinKeeperLifecycleListener.registerReceiver(receiver1);
        coinKeeperLifecycleListener.registerReceiver(receiver2);
        coinKeeperLifecycleListener.registerReceiver(null);

        coinKeeperLifecycleListener.onActivityStarted(null);

        verify(receiver1).onBroughtToForeground();
        verify(receiver2).onBroughtToForeground();
    }

    @Test
    public void registerReceiver_already_in_foreground(){

        coinKeeperLifecycleListener.onActivityStarted(null);

        coinKeeperLifecycleListener.registerReceiver(receiver1);
        coinKeeperLifecycleListener.registerReceiver(receiver2);

        coinKeeperLifecycleListener.onActivityStarted(null);

        verifyZeroInteractions(receiver1);
        verifyZeroInteractions(receiver2);
    }

    @Test
    public void registerReceiver_sent_to_background(){

        coinKeeperLifecycleListener.onActivityStarted(null);

        coinKeeperLifecycleListener.registerReceiver(receiver1);
        coinKeeperLifecycleListener.registerReceiver(receiver2);
        coinKeeperLifecycleListener.registerReceiver(null);

        coinKeeperLifecycleListener.onActivityStopped(null);

        verify(receiver1).onSentToBackground();
        verify(receiver2).onSentToBackground();
    }

    @Test
    public void registerReceiver_not_backgrounded(){

        coinKeeperLifecycleListener.onActivityStarted(null);
        coinKeeperLifecycleListener.onActivityStarted(null);

        coinKeeperLifecycleListener.registerReceiver(receiver1);
        coinKeeperLifecycleListener.registerReceiver(receiver2);
        coinKeeperLifecycleListener.registerReceiver(null);

        coinKeeperLifecycleListener.onActivityStopped(null);

        verifyZeroInteractions(receiver1);
        verifyZeroInteractions(receiver2);
    }

    @Test
    public void registerReceiver_already_in_background(){

        coinKeeperLifecycleListener.registerReceiver(receiver1);
        coinKeeperLifecycleListener.registerReceiver(receiver2);

        coinKeeperLifecycleListener.onActivityStopped(null);

        verifyZeroInteractions(receiver1);
        verifyZeroInteractions(receiver2);
    }
}
