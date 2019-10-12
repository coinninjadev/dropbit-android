package com.coinninja.coinkeeper.ui.base

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.ResolveInfo
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.ui.home.HomeActivity
import com.nhaarman.mockitokotlin2.verify
import junit.framework.Assert.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadows.ShadowActivity

@RunWith(AndroidJUnit4::class)
class MessengerActivityTest {

    private val activityController: ActivityController<HomeActivity> = Robolectric.buildActivity(HomeActivity::class.java)
    private val activity: BaseActivity = activityController.get()
    private val shadowActivity: ShadowActivity get() = shadowOf(activity)
    private val application: TestCoinKeeperApplication get() = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        activityController.setup()
    }

    @Test
    fun only_shows_one_no_internet_message() {
        activity.onHealthFail()
        activity.onHealthFail()

        assertThat((activity.findViewById<View>(R.id.message_queue) as ViewGroup).childCount, equalTo(1))
    }

    @Test
    fun runs_health_check_on_startup() {
        verify(activity.healthCheckRunner).run()
    }

    @Ignore
    @Test
    fun cancels_health_check_when_backgrounded() {
        //TODO Create ViewModel To do this work and verify that it is instructed to stop
        activityController.resume().visible().pause().stop()

        //verify(parent).removeCallbacks(runner)
    }

    @Test
    fun tracks_if_in_foreground() {
        assertTrue(activity.hasForeGround)

        activityController.pause().stop()

        assertFalse(activity.hasForeGround)
    }

    @Test
    fun tears_down_no_internet_when_internet_comes_back() {
        activityController.resume().visible()
        activity.onHealthFail()

        activity.onHealthSuccess()

        assertThat(activity.findViewById<View>(R.id.mute).visibility, equalTo(View.GONE))
        assertNull(activity.noInternetView)
    }

    @Test
    fun shows_no_internet_when_health_check_fails() {
        activity.onHealthFail()

        assertThat(activity.findViewById<View>(R.id.id_no_internet_message).visibility,
                equalTo(View.VISIBLE))

    }

    @Ignore
    @Test
    fun mutes_with_message() {
        activity.muteViewsWithMessage("foo my bar")

        assertThat(activity.mutedMessage.text.toString(), equalTo("foo my bar"))
        assertThat(activity.mutedMessage.visibility, equalTo(View.VISIBLE))
    }

    @Test
    fun tracks_activity_stop() {
        activityController.pause().stop().destroy()

        verify(application.analytics).onActivityStop(activity)
    }

    @Ignore
    @Test
    fun mute_tears_down_on_stop() {
        activity.onHealthFail()
        val muted_message = activity.findViewById<View>(R.id.muted_message)
        muted_message.visibility = View.VISIBLE

        val muted_view = activity.findViewById<View>(R.id.mute)
        muted_view.visibility = View.VISIBLE
        assertThat(muted_view.visibility, equalTo(View.VISIBLE))
        assertThat(muted_message.visibility, equalTo(View.VISIBLE))

        activityController.pause().stop()
        assertThat(muted_view.visibility, equalTo(View.GONE))
        assertThat(muted_message.visibility, equalTo(View.GONE))
    }

    @Test
    fun mutes_decendent_actions_when_required() {
        activity.onHealthFail()

        assertThat(activity.queue.visibility, equalTo(View.VISIBLE))
    }

    @Test
    fun removes_notifications_when_stopped() {
        val parent = activity.findViewById<ViewGroup>(R.id.message_queue)
        parent.addView(View(activity))
        assertThat(parent.childCount, equalTo(1))

        activityController.pause().stop()
        assertThat(parent.childCount, equalTo(0))
    }

    @Test
    fun clicking_on_refresh_launches_wifi_setting() {
        val packageManager = shadowOf(application.packageManager)!!
        val info = ResolveInfo()
        info.isDefault = true
        val applicationInfo = ApplicationInfo()
        applicationInfo.packageName = "com.example"
        info.activityInfo = ActivityInfo()
        info.activityInfo.applicationInfo = applicationInfo
        info.activityInfo.name = "Example"
        packageManager.addResolveInfoForIntent(Intent(Settings.ACTION_WIFI_SETTINGS), mutableListOf(info))

        activity.onHealthFail()


        activity.findViewById<View>(R.id.component_message_action).performClick()

        val intent = shadowActivity.peekNextStartedActivity()
        assertThat(intent.action, equalTo(Settings.ACTION_WIFI_SETTINGS))
    }

    @Test
    fun show_error_message_when_no_internet() {
        activity.onHealthFail()

        assertNotNull(activity.noInternetView)
    }

    @Test
    fun does_not_add_message_when_internet_exists() {
        activity.onHealthFail()
        activity.onHealthSuccess()
        assertNull(activity.noInternetView)
    }

    @Test
    fun test_wraps_content_in_messenger_wrapper() {
        assertNotNull(activity.findViewById(R.id.message_queue))
    }

    @Test
    fun check_for_notifications_on_start() {
        verify(activity.notificationsInteractor).startListeningForNotifications(activity, true)
    }

    @Test
    fun check_for_removing_notifications_on_pause() {
        activityController.pause()
        verify(activity.notificationsInteractor).stopListeningForNotifications()
    }

}