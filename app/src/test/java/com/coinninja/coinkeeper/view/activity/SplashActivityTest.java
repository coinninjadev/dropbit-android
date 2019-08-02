package com.coinninja.coinkeeper.view.activity;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.model.helpers.UserHelper;
import com.coinninja.coinkeeper.receiver.StartupCompleteReceiver;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static junit.framework.Assert.assertNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class SplashActivityTest {

    @Mock
    LocalBroadCastUtil localBroadCastUtil;
    @Mock
    UserHelper userHelper;
    @Mock
    CNWalletManager cnWalletManager;
    @Mock
    ActivityNavigationUtil activityNavigationUtil;
    private SplashActivity activity;
    private ActivityController<SplashActivity> activityActivityController;
    private TestCoinKeeperApplication application;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        application = ApplicationProvider.getApplicationContext();
        activityActivityController = Robolectric.buildActivity(SplashActivity.class);
        activity = activityActivityController.get();
        activityActivityController.create();
        activity.localBroadCastUtil = localBroadCastUtil;
        activity.cnWalletManager = cnWalletManager;
        activity.activityNavigationUtil = activityNavigationUtil;
    }

    @After
    public void tearDown() {
        activity = null;
        activityActivityController = null;
        application = null;
        localBroadCastUtil = null;
        userHelper = null;
        cnWalletManager = null;
    }

    @Test
    public void nullsOutActivityTransitions() {
        assertNull(activity.getWindow().getExitTransition());
        assertNull(activity.getWindow().getEnterTransition());
    }

    @Test
    public void startsStartActivityWhenRecoveryWordsAreNotSaved() {
        start();

        ShadowActivity shadowActivity = shadowOf(activity);
        Intent intent = shadowActivity.getNextStartedActivity();

        assertThat(intent.getComponent().getClassName(), equalTo(StartActivity.class.getName()));
    }

    @Test
    public void broadcasts_start_up__with_backedup_wallet() {
        when(cnWalletManager.getHasWallet()).thenReturn(true);

        start();

        verify(localBroadCastUtil).sendGlobalBroadcast(StartupCompleteReceiver.class,
                DropbitIntents.ACTION_ON_APPLICATION_FOREGROUND_STARTUP);
    }

    @Test
    public void starts_home_activity_when_recovery_words_are_saved() {
        when(cnWalletManager.getHasWallet()).thenReturn(true);

        start();

        verify(activityNavigationUtil).navigateToHome(activity);
    }

    @Test
    public void itDeauthenticatesTheSessionOncreate() {
        start();

        verify(application.authentication).forceDeAuthenticate();
    }

    void start() {
        activityActivityController.start().resume().visible();
        activity.displayDelayRunnable.run();
    }
}