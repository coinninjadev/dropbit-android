package com.coinninja.coinkeeper.view.activity

import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.truth.content.IntentSubject.assertThat
import app.dropbit.twitter.TwitterIntents
import app.dropbit.twitter.model.TwitterUser
import app.dropbit.twitter.ui.login.TwitterLoginActivity
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.ui.phone.verification.VerificationActivity
import com.coinninja.coinkeeper.util.DropbitIntents
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignUpSelectionActivityTest {

    fun setup(): ActivityScenario<SignUpSelectionActivity> {
        val scenario = ActivityScenario.launch(SignUpSelectionActivity::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)
        return scenario
    }

    @Test
    fun `clicking on text message invite navigates to verify phone number`() {
        setup()

        onView(withId(R.id.text_message_invite_button)).perform(click())

        val intent = Intents.getIntents().get(0)
        assertThat(intent).hasComponent(
                ApplicationProvider.getApplicationContext<Application>().packageName,
                VerificationActivity::class.java.name
        )

        assertThat(intent).extras().containsKey(DropbitIntents.EXTRA_HIDE_SKIP_BUTTON)
        assertThat(intent).extras().bool(DropbitIntents.EXTRA_HIDE_SKIP_BUTTON).isEqualTo(false)
    }

    @Test
    fun `successful auth with twitter flags navigates home and shows dropbit me dialog`() {
        val scenario = setup()
        val twitterUser = TwitterUser()
        twitterUser.screenName = "JohnnyNumber5"
        twitterUser.userId = 123456789L
        val resultData = Intent()
        resultData.putExtra(TwitterIntents.TWITTER_USER, twitterUser)
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)
        Intents.intending(toPackage(ApplicationProvider.getApplicationContext<Application>().packageName)).respondWith(result)

        onView(withId(R.id.twitter_invite_button)).perform(click())


        val intent = Intents.getIntents().get(0)
        assertThat(intent).hasComponent(
                ApplicationProvider.getApplicationContext<Application>().packageName,
                TwitterLoginActivity::class.java.name
        )

        scenario.onActivity {
            verify(it.twitterVerificationController).onTwitterAuthorized(resultData)
        }
    }

    @Test
    fun `observes twitter verified when started`() {
        val scenario = setup()
        scenario.onActivity {
            verify(it.twitterVerificationController).onStarted(it, null)
        }
    }

    @Test
    fun `removes observer when stopped`() {
        val scenario = setup()
        scenario.onActivity {
            val controller = it.twitterVerificationController

            scenario.moveToState(Lifecycle.State.DESTROYED)

            verify(controller).onStopped()
        }
    }
}