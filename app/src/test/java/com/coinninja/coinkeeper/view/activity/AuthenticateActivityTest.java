package com.coinninja.coinkeeper.view.activity;

import android.app.Activity;
import android.app.Fragment;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.view.fragment.AuthenticateFragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class AuthenticateActivityTest {


    private ActivityController<AuthenticateActivity> activityController;
    private AuthenticateActivity activity;
    AuthenticateFragment fragment;
    private ShadowActivity shadowActivity;


    @Before
    public void setUp() {
        activityController = Robolectric.buildActivity(AuthenticateActivity.class);
        activity = activityController.get();
        fragment = mock(AuthenticateFragment.class);
        shadowActivity = shadowOf(activity);
    }

    @Test
    public void does_not_teardown_mute_when_not_foregrounded() {
        activityController.create().start().resume().visible();
        activity = activityController.get();
        activity.authenticateFragment = fragment;
        activityController.pause().stop();

        activity.teardownMute();

        verify(fragment, times(0)).teardownMute();
    }

    @Test
    public void instructs_fragment_to_teardown_mute() {
        activityController.create();
        activity.authenticateFragment = fragment;
        activityController.start().resume().visible();

        activity.teardownMute();

        verify(fragment).teardownMute();
    }

    @Test
    public void instructs_authentication_fragment_to_mute_when_muted() {
        activityController.create();
        activity.authenticateFragment = fragment;
        activityController.start().resume().visible();

        activity.muteViews();

        verify(fragment).muteViews();
    }

    @Test
    public void showsAuthenticationFragmentWhenCreated() {
        activityController.create().start().resume().visible();

        List<Fragment> fragments = activity.getFragmentManager().getFragments();
        Fragment latestFragment = fragments.get(fragments.size() - 1);
        assertNotNull(latestFragment);
        assertThat(latestFragment.getClass().getCanonicalName(), equalTo(AuthenticateFragment.class.getCanonicalName()));

    }

    @Test
    public void sets_result_to_ok_when_authenticated() {
        activityController.create().start().resume().visible();

        activity.onAuthenticated();

        assertThat(shadowActivity.getResultCode(), equalTo(Activity.RESULT_OK));
        assertTrue(shadowActivity.isFinishing());

    }

}