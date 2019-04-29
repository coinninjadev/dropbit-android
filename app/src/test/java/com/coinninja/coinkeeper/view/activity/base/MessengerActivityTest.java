package com.coinninja.coinkeeper.view.activity.base;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.interactor.InternalNotificationsInteractor;
import com.coinninja.coinkeeper.service.runner.HealthCheckTimerRunner;
import com.coinninja.coinkeeper.ui.settings.SettingsActivity;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.InternetUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowPackageManager;

import edu.emory.mathcs.backport.java.util.Collections;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class MessengerActivityTest {

    @Mock
    private InternalNotificationsInteractor notificationsInteractor;
    @Mock
    private HealthCheckTimerRunner runner;
    @Mock
    private Analytics analytics;
    @Mock
    private InternetUtil internetUtil;

    private MessengerActivity activity;
    private ActivityController<MessengerActivity> activityController;
    private ShadowActivity shadowActivity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(internetUtil.hasInternet()).thenReturn(true);
        activityController = Robolectric.buildActivity(MessengerActivity.class);
        activity = activityController.get();
        shadowActivity = shadowOf(activity);
        activityController.create();
        activity.setContentView(R.layout.cn_base_layout);
        activity.healthCheckRunner = runner;
        activity.analytics = analytics;
        activity.notificationsInteractor = notificationsInteractor;
        activityController.start();
    }

    private void setupActivity() {
        activity.internetUtil = internetUtil;
        activityController.start().resume().visible();
    }

    @Test
    public void settings_screen_sets_content_view() {
        AppCompatActivity activity = Robolectric.setupActivity(SettingsActivity.class);

        assertNotNull(activity.findViewById(R.id.message_queue));
    }

    @Test
    public void only_shows_one_no_internet_message() {
        activityController.resume().visible();

        activity.onHealthFail();
        activity.onHealthFail();

        assertThat(((ViewGroup) activity.findViewById(R.id.message_queue)).getChildCount(), equalTo(1));
    }

    @Test
    public void runs_health_check_on_startup() {
        ViewGroup parent = mock(ViewGroup.class);
        activity.queue = parent;

        activityController.resume().visible();

        verify(runner).run();
    }

    @Test
    public void cancels_health_check_when_backgrounded() {
        ViewGroup parent = mock(ViewGroup.class);
        activity.queue = parent;

        activityController.resume().visible().pause().stop();

        verify(parent).removeCallbacks(runner);
    }

    @Test
    public void tracks_if_in_foreground() {
        setupActivity();
        assertTrue(activity.isForeGrounded());

        activityController.pause().stop();
        assertFalse(activity.isForeGrounded());
    }

    @Test
    public void only_tears_down_no_internet_when_it_is_displayed() {
        ViewGroup parent = mock(ViewGroup.class);
        activity.queue = parent;
        activityController.resume().visible();

        activity.onHealthSuccess();

        assertNull(activity.findViewById(R.id.id_no_internet_message));
        verify(parent, times(0)).removeView(any(View.class));
    }

    @Test
    public void tears_down_no_internet_when_internet_comes_back() {
        activityController.resume().visible();
        activity.onHealthFail();

        activity.onHealthSuccess();

        assertThat(activity.findViewById(R.id.mute).getVisibility(), equalTo(View.GONE));
        assertNull(activity.findViewById(R.id.id_no_internet_message));
    }

    @Test
    public void shows_no_internet_when_health_check_fails() {
        activityController.resume().visible();

        activity.onHealthFail();

        assertThat(activity.findViewById(R.id.id_no_internet_message).getVisibility(),
                equalTo(View.VISIBLE));

    }

    @Test
    public void schedules_follow_up_check_on_health_fail() {
        ViewGroup parent = mock(ViewGroup.class);
        activity.queue = parent;

        activityController.resume().visible();
        activity.onHealthFail();

        verify(parent).postDelayed(runner, 30000);
    }

    @Test
    public void schedules_follow_up_check_on_health_success() {
        ViewGroup parent = mock(ViewGroup.class);
        activity.queue = parent;

        activityController.resume().visible();
        activity.onHealthSuccess();

        verify(parent).postDelayed(runner, 30000);
    }

    @Test
    public void creates_instance_of_health_check_timer_on_create() {
        assertNotNull(activity.healthCheckRunner);
    }

    @Test
    public void mutes_with_message() {
        setupActivity();

        activity.muteViewsWithMessage("foo my bar");

        TextView message = activity.findViewById(R.id.muted_message);
        assertThat(message.getVisibility(), equalTo(View.VISIBLE));
        assertThat(message.getText().toString(), equalTo("foo my bar"));
    }

    @Test
    public void tracks_activity_stop() {
        setupActivity();
        activityController.pause().stop().destroy();
        verify(analytics).onActivityStop(activity);
    }

    @Test
    public void mute_tears_down_on_stop() {
        when(internetUtil.hasInternet()).thenReturn(false);
        setupActivity();
        View muted_message = activity.findViewById(R.id.muted_message);
        muted_message.setVisibility(View.VISIBLE);

        View muted_view = activity.findViewById(R.id.mute);
        assertThat(muted_view.getVisibility(), equalTo(View.VISIBLE));

        activityController.pause().stop();
        assertThat(muted_view.getVisibility(), equalTo(View.GONE));
        assertThat(muted_message.getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void mutes_decendent_actions_when_required() {
        when(internetUtil.hasInternet()).thenReturn(false);

        setupActivity();

        ViewGroup muted_view = activity.findViewById(R.id.message_queue);
        assertThat(muted_view.getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void removes_notifications_when_stopped() {
        when(internetUtil.hasInternet()).thenReturn(false);
        setupActivity();

        ViewGroup parent = activity.findViewById(R.id.message_queue);
        assertThat(parent.getChildCount(), equalTo(1));

        activityController.pause().stop();
        assertThat(parent.getChildCount(), equalTo(0));
    }

    @Test
    public void clicking_on_refresh_launches_wifi_setting() {
        ShadowPackageManager packageManager = shadowOf(RuntimeEnvironment.application.getPackageManager());
        ResolveInfo info = new ResolveInfo();
        info.isDefault = true;
        ApplicationInfo applicationInfo = new ApplicationInfo();
        applicationInfo.packageName = "com.example";
        info.activityInfo = new ActivityInfo();
        info.activityInfo.applicationInfo = applicationInfo;
        info.activityInfo.name = "Example";
        packageManager.addResolveInfoForIntent(new Intent(Settings.ACTION_WIFI_SETTINGS), Collections.singletonList(info));

        when(internetUtil.hasInternet()).thenReturn(false);
        setupActivity();

        activity.findViewById(R.id.component_message_action).performClick();

        Intent intent = shadowActivity.peekNextStartedActivity();
        assertThat(intent.getAction(), equalTo(Settings.ACTION_WIFI_SETTINGS));
    }

    @Test
    public void show_error_message_when_no_internet() {
        when(internetUtil.hasInternet()).thenReturn(false);
        setupActivity();

        assertNotNull(activity.findViewById(R.id.id_no_internet_message));
    }

    @Test
    public void does_not_add_message_when_internet_exists() {
        setupActivity();
        assertNull(activity.findViewById(R.id.id_no_internet_message));
    }

    @Test
    public void test_wrappes_content_in_messenger_wrapper() {
        setupActivity();
        assertNotNull(activity.findViewById(R.id.message_queue));
    }


    @Test
    public void check_for_notifications_on_start() {
        setupActivity();
        verify(notificationsInteractor).startListeningForNotifications(activity, true);
    }

    @Test
    public void check_for_removing_notifications_on_pause() {
        setupActivity();
        activityController.pause();
        verify(notificationsInteractor).stopListeningForNotifications();
    }
}