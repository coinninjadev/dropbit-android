package com.coinninja.coinkeeper.view.activity;

import android.content.Intent;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.service.WalletCreationIntentService;
import com.coinninja.coinkeeper.ui.phone.verification.VerifyPhoneNumberActivity;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.view.animation.StartScreenAnimation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class StartActivityTest {

    private StartActivity activity;
    private StartScreenAnimation animation;
    private ShadowActivity shadowActivity;

    @Before
    public void setUp() {
        animation = mock(StartScreenAnimation.class);
        ActivityController<StartActivity> activityController = Robolectric.buildActivity(StartActivity.class);
        activity = activityController.get();
        activityController.create();
        activity.setStartScreenAnimation(animation);
        activityController.start().resume().visible();
        shadowActivity = shadowOf(activity);
    }

    @Test
    public void clicking_restore_wallet_navigates_to_restore_wallet_activity() {
        activity.findViewById(R.id.start_btn_restore).performClick();

        Intent intent = shadowActivity.getNextStartedActivity();
        assertThat(intent.getComponent().getClassName(), equalTo(RestoreWalletActivity.class.getName()));
    }

    @Test
    public void clicking_create_wallet_creates_wallet_and_skips_backup() {
        activity.findViewById(R.id.start_btn_new_wallet).performClick();

        Intent intent = shadowActivity.getNextStartedActivity();

        assertThat(intent.getComponent().getClassName(), equalTo(CreatePinActivity.class.getName()));
        assertThat(intent.getStringExtra(Intents.EXTRA_NEXT), equalTo(VerifyPhoneNumberActivity.class.getName()));
        Intent completionIntent = (Intent) intent.getExtras().get(Intents.EXTRA_ON_COMPLETION);
        assertThat(completionIntent.getComponent().getClassName(), equalTo(WalletCreationIntentService.class.getName()));
    }

    @Test
    public void plays_animation_on_startup() {
        verify(animation).animateIn();
    }

    @Test
    public void back_animates_out() {
        activity.onBackPressed();

        verify(animation).animateOut();
        assertTrue(shadowActivity.isFinishing());
    }
}