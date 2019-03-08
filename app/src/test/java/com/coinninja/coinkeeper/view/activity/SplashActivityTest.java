package com.coinninja.coinkeeper.view.activity;

import android.content.Intent;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.model.helpers.CreateUserTask;
import com.coinninja.coinkeeper.model.helpers.UserHelper;
import com.coinninja.coinkeeper.receiver.StartupCompleteReceiver;
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
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import junitx.util.PrivateAccessor;

import static junit.framework.Assert.assertNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class SplashActivityTest {

    private SplashActivity activity;
    private ActivityController<SplashActivity> activityActivityController;
    private TestCoinKeeperApplication application;

    @Mock
    LocalBroadCastUtil localBroadCastUtil;

    @Mock
    CreateUserTask createUserTask;

    @Mock
    UserHelper userHelper;

    @Mock
    CNWalletManager cnWalletManager;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        application = (TestCoinKeeperApplication) RuntimeEnvironment.application;
        activityActivityController = Robolectric.buildActivity(SplashActivity.class);
        activity = activityActivityController.get();
        activityActivityController.create();
        activity.createUserTask = createUserTask;
        activity.localBroadCastUtil = localBroadCastUtil;
        activity.userHelper = userHelper;
        activity.cnWalletManager = cnWalletManager;
        PrivateAccessor.setField(activity, "delayMillis", 0);
    }

    @After
    public void tearDown() throws Exception {
        activity = null;
        activityActivityController = null;
        application = null;
        localBroadCastUtil = null;
        createUserTask = null;
        userHelper = null;
        cnWalletManager = null;
    }

    void start() {
        activityActivityController.start().resume().visible();
        //TODO abstract to startup Manager class?
        activity.onUserCreated();
    }

    @Test
    public void nullsOutActivityTransitions() {
        assertNull(activity.getWindow().getExitTransition());
        assertNull(activity.getWindow().getEnterTransition());
    }

    @Test
    public void startsStartActivityWhenRecoveryWordsAreNotSaved_and_user_has_completed_training() {
        when(userHelper.hasCompletedTraining()).thenReturn(true);
        start();

        ShadowActivity shadowActivity = shadowOf(activity);
        Intent intent = shadowActivity.getNextStartedActivity();

        assertThat(intent.getComponent().getClassName(), equalTo(StartActivity.class.getName()));
    }

    @Test
    public void starts_TrainingActivity_when_recoveryWords_AreNot_Saved_and_user_has_not_completed_training() {
        when(userHelper.hasCompletedTraining()).thenReturn(false);
        start();

        ShadowActivity shadowActivity = shadowOf(activity);
        Intent intent = shadowActivity.getNextStartedActivity();

        assertThat(intent.getComponent().getClassName(),
                equalTo(TrainingActivity.class.getName()));
    }

    @Test
    public void broadcasts_start_up__with_backedup_wallet() {
        when(cnWalletManager.hasWallet()).thenReturn(true);

        start();

        verify(localBroadCastUtil).sendGlobalBroadcast(StartupCompleteReceiver.class,
                Intents.ACTION_ON_APPLICATION_FOREGROUND_STARTUP);
    }

    @Test
    public void starts_calculator_activity_when_recovery_words_are_saved() {
        when(cnWalletManager.hasWallet()).thenReturn(true);
        start();

        ShadowActivity shadowActivity = shadowOf(activity);
        Intent intent = shadowActivity.getNextStartedActivity();

        assertThat(intent.getComponent().getClassName(), equalTo(CalculatorActivity.class.getName()));
    }

    @Test
    public void itDeauthenticatesTheSessionOncreate() {
        start();

        verify(application.authentication).forceDeAuthenticate();
    }
}