package com.coinninja.coinkeeper.ui.account.verify

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.android.helpers.Views.clickOn
import com.coinninja.android.helpers.Views.withId
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.ui.account.UserServerAddressesFragment
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.matchers.IntentFilterMatchers
import com.coinninja.matchers.ViewMatcher.isGone
import com.coinninja.matchers.ViewMatcher.isVisible
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertNotNull
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class UserAccountVerificationActivityTest {

    val application = ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>()

    @Before
    fun setUp() {
        application.dropbitAccountHelper = mock(DropbitAccountHelper::class.java)
        whenever(application.authentication.isAuthenticated).thenReturn(true)
    }

    fun start(): ActivityScenario<UserAccountVerificationActivity> {
        val scenario = ActivityScenario.launch(UserAccountVerificationActivity::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)
        return scenario
    }

    @Test
    fun `shows addresses when verified`() {
        whenever(application.dropbitAccountHelper.hasVerifiedAccount).thenReturn(true)

        val scenario = start()
        scenario.onActivity { activity ->
            clickOn(withId(activity, R.id.view_dropbit_addresses))

            assertNotNull(activity.supportFragmentManager.findFragmentByTag(UserServerAddressesFragment::class.java.simpleName))
        }
    }

    @Test
    fun `observes intents when started`() {
        val scenario = start()
        scenario.onActivity { activity ->
            verify(application.localBroadCastUtil).registerReceiver(activity.receiver, activity.intentFilter)
            assertThat(activity.intentFilter, IntentFilterMatchers.containsAction(DropbitIntents.ACTION_LOCAL_ADDRESS_CACHE_CLEARED))
            assertThat(activity.intentFilter, IntentFilterMatchers.containsAction(DropbitIntents.ACTION_LOCAL_ADDRESS_CACHE_POPULATED))
        }
    }

    @Test
    fun `stops observing intents when stopped`() {
        val scenario = start()
        scenario.onActivity { activity ->
            val receiver = activity.receiver

            scenario.moveToState(Lifecycle.State.DESTROYED)

            verify(application.localBroadCastUtil).unregisterReceiver(receiver)
        }
    }

    @Test
    fun `hides addresses when not verified`() {
        val scenario = start()

        scenario.onActivity { activity ->
            assertThat(withId(activity, R.id.view_dropbit_addresses), isGone())
        }
    }

    @Test
    fun `observe address cache removal and hide address cache state`() {
        whenever(application.dropbitAccountHelper.hasVerifiedAccount).thenReturn(true).thenReturn(false)
        val scenario = start()
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity { activity ->
            assertThat(withId(activity, R.id.view_dropbit_addresses), isVisible())

            activity.receiver.onReceive(activity, Intent(DropbitIntents.ACTION_LOCAL_ADDRESS_CACHE_CLEARED))

            assertThat(withId(activity, R.id.view_dropbit_addresses), isGone())
        }
    }

    @Test
    fun `shows local cache when populated`() {
        whenever(application.dropbitAccountHelper.hasVerifiedAccount).thenReturn(false).thenReturn(true)
        val scenario = start()
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity { activity ->
            assertThat(withId(activity, R.id.view_dropbit_addresses), isGone())

            activity.receiver.onReceive(activity, Intent(DropbitIntents.ACTION_LOCAL_ADDRESS_CACHE_POPULATED))

            assertThat(withId(activity, R.id.view_dropbit_addresses), isVisible())
        }
    }
}