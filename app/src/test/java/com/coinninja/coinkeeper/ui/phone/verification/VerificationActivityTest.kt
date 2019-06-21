package com.coinninja.coinkeeper.ui.phone.verification

import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.content.Intent
import android.view.MenuItem
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.dropbit.twitter.TwitterIntents
import app.dropbit.twitter.model.TwitterUser
import com.coinninja.android.helpers.Views
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.ui.account.verify.twitter.TwitterVerificationController
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.android.ServiceWorkUtil
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocale
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocaleGenerator
import com.coinninja.coinkeeper.view.widget.phonenumber.PhoneVerificationView
import com.google.i18n.phonenumbers.Phonenumber
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.util.*

@RunWith(AndroidJUnit4::class)
class VerificationActivityTest {

    fun setUp(intent: Intent = Intent(ApplicationProvider.getApplicationContext(),
            VerificationActivity::class.java)): ActivityScenario<VerificationActivity> {

        val countryCodeLocales = mutableListOf<CountryCodeLocale>()
        val countryCodeLocaleGenerator = mock(CountryCodeLocaleGenerator::class.java)
        whenever(countryCodeLocaleGenerator.generate()).thenReturn(countryCodeLocales)
        val application = ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>()
        application.activityNavigationUtil = mock(ActivityNavigationUtil::class.java)
        application.serviceWorkUtil = mock(ServiceWorkUtil::class.java)
        application.countryCodeLocaleGenerator = countryCodeLocaleGenerator
        application.twitterVerificationController = mock(TwitterVerificationController::class.java)
        countryCodeLocales.add(CountryCodeLocale(Locale("en", "US"), 1))

        return ActivityScenario.launch(intent)
    }

    @Test
    fun `user can skip verification of key words`() {
        val scenario = setUp()
        val closeMenuItem = mock(MenuItem::class.java)
        whenever(closeMenuItem.itemId).thenReturn(R.id.action_skip_btn)

        scenario.onActivity { activity ->
            activity.onSkipClicked()

            verify(activity.activityNavigationUtil).navigateToHome(activity)
            verify(activity.analytics).trackEvent(Analytics.EVENT_PHONE_VERIFICATION_SKIPPED)
        }

        scenario.close()
    }

    @Test
    fun `sets country code locales on view`() {
        val scenario = setUp()

        scenario.onActivity { activity ->
            val phoneVerificationView = Views.withId<PhoneVerificationView>(activity, R.id.verification_view)
            assertThat(phoneVerificationView.countryCodeLocales.size, equalTo(1))
        }

        scenario.close()
    }

    @Test
    fun `processes phone number provided`() {
        val scenario = setUp()
        val phoneNumber = Phonenumber.PhoneNumber()
        phoneNumber.nationalNumber = 3305555555L
        phoneNumber.countryCode = 1
        val number = PhoneNumber(phoneNumber)

        scenario.onActivity { activity ->
            activity.onPhoneNumberValid(phoneNumber)
            verify(activity.activityNavigationUtil).navigateToVerifyPhoneNumberCode(activity, number)
            verify(activity.serviceWorkUtil).registerUsersPhone(number)
        }
        scenario.close()
    }

    @Test
    fun `hides error on pause`() {
        val scenario = setUp()

        scenario.onActivity { activity ->
            val phoneVerificationView = mock(PhoneVerificationView::class.java)
            activity.phoneVerificationView = phoneVerificationView

            scenario.moveToState(Lifecycle.State.DESTROYED)

            verify(phoneVerificationView).resetView()
        }

        scenario.close()
    }

    @Test
    fun `verifies twitter when visible`() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), VerificationActivity::class.java)
        intent.putExtra(DropbitIntents.EXTRA_SHOW_TWITTER_VERIFY_BUTTON, true)
        val scenario = setUp(intent)
        val twitterUser = TwitterUser()
        twitterUser.screenName = "JohnnyNumber5"
        twitterUser.userId = 123456789L
        val resultData = Intent()
        resultData.putExtra(TwitterIntents.TWITTER_USER, twitterUser)
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)
        Intents.intending(IntentMatchers.toPackage(ApplicationProvider.getApplicationContext<Application>().packageName)).respondWith(result)

        onView(withId(R.id.verify_twitter_button)).perform(click())

        scenario.onActivity {
            verify(it.twitterVerificationController).onTwitterAuthorized(resultData)
        }

        scenario.close()
    }

    @Test
    fun `observes twitter verified when started`() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), VerificationActivity::class.java)
        intent.putExtra(DropbitIntents.EXTRA_SHOW_TWITTER_VERIFY_BUTTON, true)
        val scenario = setUp(intent)
        scenario.onActivity {
            verify(it.twitterVerificationController).onStarted(it, null)
        }

        scenario.close()
    }

    @Test
    fun `removes observer when stopped`() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), VerificationActivity::class.java)
        intent.putExtra(DropbitIntents.EXTRA_SHOW_TWITTER_VERIFY_BUTTON, true)
        val scenario = setUp(intent)

        scenario.onActivity {
            val controller = it.twitterVerificationController

            scenario.moveToState(Lifecycle.State.DESTROYED)

            verify(controller).onStopped()
        }

        scenario.close()
    }
}