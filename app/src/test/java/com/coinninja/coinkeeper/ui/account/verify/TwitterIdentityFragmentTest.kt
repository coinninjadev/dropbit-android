package com.coinninja.coinkeeper.ui.account.verify

import android.app.Activity
import android.app.Instrumentation
import android.content.DialogInterface
import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.dropbit.twitter.TwitterIntents
import app.dropbit.twitter.model.TwitterUser
import app.dropbit.twitter.ui.login.TwitterLoginActivity
import com.coinninja.android.helpers.Resources.getString
import com.coinninja.android.helpers.Views.clickOn
import com.coinninja.android.helpers.Views.withId
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.model.db.DropbitMeIdentity
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.ui.account.verify.twitter.TwitterVerificationController
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.matchers.IntentFilterMatchers
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertNull
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.shadows.ShadowDialog
import org.robolectric.shadows.ShadowToast

@RunWith(AndroidJUnit4::class)
class TwitterIdentityFragmentTest {

    lateinit var scenario: FragmentScenario<TwitterIdentityFragment>

    var application: TestCoinKeeperApplication = ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>().also {
        it.dropbitAccountHelper = mock(DropbitAccountHelper::class.java)
        it.twitterVerificationController = mock(TwitterVerificationController::class.java)
        it.analytics = mock(Analytics::class.java)
    }

    @After
    fun tearDown() {
        scenario.moveToState(Lifecycle.State.DESTROYED)
    }

    fun start() {
        scenario = FragmentScenario.launch(TwitterIdentityFragment::class.java, null,
                androidx.appcompat.R.style.Base_Theme_AppCompat, null)
        scenario.onFragment { fragment ->
            fragment.analytics = application.analytics
            fragment.myTwitterProfile = mock()
        }
        scenario.moveToState(Lifecycle.State.RESUMED)
    }

    @Test
    fun `intent filter contains right actions to observe`() {
        start()

        scenario.onFragment { fragment ->
            assertThat(fragment.intentFilter, IntentFilterMatchers.containsAction(DropbitIntents.ACTION_VERIFY_TWITTER_COMPLETED))
            assertThat(fragment.intentFilter, IntentFilterMatchers.containsAction(DropbitIntents.ACTION_DEVERIFY_TWITTER_COMPLETED))
            assertThat(fragment.intentFilter, IntentFilterMatchers.containsAction(DropbitIntents.ACTION_DEVERIFY_TWITTER_FAILED))
            verify(fragment.twitterVerificationController).onStarted(fragment.activity!!, null)
        }
    }

    @Test
    fun `configures for phone verification when user is verified but phone is not`() {
        whenever(application.dropbitAccountHelper.isTwitterVerified).thenReturn(false)
        start()

        scenario.onFragment { fragment ->
            val verificationButton = fragment.getVerificationButton()!!
            assertThat(verificationButton.text.toString(), equalTo(fragment.getString(R.string.verify_twitter_account)))
            assertThat(verificationButton.visibility, equalTo(View.VISIBLE))

            val identityView = fragment.getIdentityView()!!
            assertThat(identityView.visibility, equalTo(View.GONE))

            val removeVerificationView = fragment.getRemoveVerificationView()!!
            assertThat(removeVerificationView.visibility, equalTo(View.GONE))
        }
    }

    @Test
    fun `shows verified when verified`() {
        whenever(application.dropbitAccountHelper.isTwitterVerified).thenReturn(true)
        val identity = mock(DropbitMeIdentity::class.java)
        whenever(identity.identity).thenReturn("JonnyNumber5")
        whenever(identity.type).thenReturn(IdentityType.TWITTER)
        whenever(application.dropbitAccountHelper.twitterIdentity()).thenReturn(identity)
        start()

        scenario.onFragment { fragment ->
            val verificationButton = fragment.getVerificationButton()!!
            val identityView = fragment.getIdentityView()!!
            assertThat(verificationButton.text.toString(), equalTo(fragment.getString(R.string.verify_twitter_account)))
            assertThat(verificationButton.visibility, equalTo(View.GONE))
            whenever(identity.handle).thenReturn("JohnnyNumber5")
            assertThat(identityView.visibility, equalTo(View.VISIBLE))

            val removeVerificationView = fragment.getRemoveVerificationView()!!
            assertThat(removeVerificationView.visibility, equalTo(View.VISIBLE))
        }

    }

    @Test
    fun `stops observing receiver calls when stopped`() {
        start()

        scenario.onFragment { fragment ->
            val receiver = fragment.receiver

            scenario.moveToState(Lifecycle.State.DESTROYED)

            verify(application.localBroadCastUtil).unregisterReceiver(receiver)
            verify(application.twitterVerificationController).onStopped()
        }
    }

    @Test
    fun `prompts to deVerify`() {
        whenever(application.dropbitAccountHelper.isTwitterVerified).thenReturn(true)
        val identity = mock(DropbitMeIdentity::class.java)
        whenever(identity.identity).thenReturn("JonnyNumber5")
        whenever(identity.type).thenReturn(IdentityType.TWITTER)
        whenever(application.dropbitAccountHelper.twitterIdentity()).thenReturn(identity)
        start()

        scenario.onFragment { fragment ->
            clickOn(fragment.findViewById(R.id.remove_verification))
        }

        val dialog = ShadowDialog.getLatestDialog() as AlertDialog
        assertThat(dialog.findViewById<TextView>(R.id.warning)!!.text.toString(),
                equalTo(application.getString(R.string.deverification_twitter_removed_warning_message)))
        assertThat(dialog.findViewById<TextView>(R.id.message)!!.text.toString(),
                equalTo(application.getString(R.string.deverification_twitter_removed_message)))

    }

    @Test
    fun `accepting idea of deVerifing prompts to confirm`() {
        whenever(application.dropbitAccountHelper.isTwitterVerified).thenReturn(true)
        val identity = mock(DropbitMeIdentity::class.java)
        whenever(identity.identity).thenReturn("JonnyNumber5")
        whenever(identity.type).thenReturn(IdentityType.TWITTER)
        whenever(application.dropbitAccountHelper.twitterIdentity()).thenReturn(identity)
        start()
        scenario.onFragment { fragment ->
            clickOn(fragment.findViewById(R.id.remove_verification))
        }

        val dialog = ShadowDialog.getLatestDialog() as AlertDialog
        clickOn(withId(dialog, R.id.ok))

        val confirmation = ShadowDialog.getLatestDialog() as AlertDialog

        assertThat(confirmation.findViewById<TextView>(android.R.id.message)!!.text.toString(),
                equalTo(application.getString(R.string.deverification_twitter_removed_message_follow_up)))
    }

    @Test
    fun `accepting follow up performs deVerification`() {
        whenever(application.dropbitAccountHelper.isTwitterVerified).thenReturn(true)
        val identity = mock(DropbitMeIdentity::class.java)
        whenever(identity.identity).thenReturn("JonnyNumber5")
        whenever(identity.type).thenReturn(IdentityType.TWITTER)
        whenever(application.dropbitAccountHelper.twitterIdentity()).thenReturn(identity)
        start()
        scenario.onFragment { fragment ->
            clickOn(fragment.findViewById(R.id.remove_verification))
        }

        val dialog = ShadowDialog.getLatestDialog() as AlertDialog
        clickOn(withId(dialog, R.id.ok))

        val confirmation = ShadowDialog.getLatestDialog() as AlertDialog
        confirmation.getButton(DialogInterface.BUTTON_POSITIVE).performClick()

        scenario.onFragment { fragment ->
            assertNull(fragment.activity?.supportFragmentManager?.findFragmentByTag("CONFIRM_DEVERIFICATION_NOTICE"))
            assertNull(fragment.activity?.supportFragmentManager?.findFragmentByTag("CONFIRM_DEVERIFICATION_CONFIRMATION_NOTICE"))
        }

        verify(application.serviceWorkUtil).deVerifyTwitter()

        scenario.onFragment { fragment ->
            verify(application.localBroadCastUtil).registerReceiver(fragment.receiver, fragment.intentFilter)
        }
    }

    @Test
    fun `dismissing follow up dismisses dialog`() {
        whenever(application.dropbitAccountHelper.isTwitterVerified).thenReturn(true)
        val identity = mock(DropbitMeIdentity::class.java)
        whenever(identity.handle).thenReturn("JohnnyNumber5")
        whenever(identity.type).thenReturn(IdentityType.TWITTER)
        whenever(application.dropbitAccountHelper.twitterIdentity()).thenReturn(identity)
        start()
        scenario.onFragment { fragment ->
            clickOn(fragment.findViewById(R.id.remove_verification))
        }

        val dialog = ShadowDialog.getLatestDialog() as AlertDialog
        clickOn(withId(dialog, R.id.ok))

        val confirmation = ShadowDialog.getLatestDialog() as AlertDialog
        confirmation.getButton(DialogInterface.BUTTON_NEGATIVE).performClick()

        scenario.onFragment { fragment ->
            assertNull(fragment.activity?.supportFragmentManager?.findFragmentByTag("CONFIRM_DEVERIFICATION_NOTICE"))
            assertNull(fragment.activity?.supportFragmentManager?.findFragmentByTag("CONFIRM_DEVERIFICATION_CONFIRMATION_NOTICE"))
        }
    }


    @Test
    fun `observing twitter deVerification updates ui`() {
        whenever(application.dropbitAccountHelper.isTwitterVerified).thenReturn(true)
        val identity = mock(DropbitMeIdentity::class.java)
        whenever(identity.handle).thenReturn("JohnnyNumber5")
        whenever(identity.type).thenReturn(IdentityType.TWITTER)
        whenever(application.dropbitAccountHelper.twitterIdentity()).thenReturn(identity)
        start()

        scenario.onFragment { fragment ->
            fragment.receiver.onReceive(fragment.context, Intent(DropbitIntents.ACTION_DEVERIFY_TWITTER_COMPLETED))

            val verificationButton = fragment.getVerificationButton()!!
            assertThat(verificationButton.text.toString(), equalTo(fragment.getString(R.string.verify_twitter_account)))
            assertThat(verificationButton.visibility, equalTo(View.VISIBLE))

            val identityView = fragment.getIdentityView()!!
            assertThat(identityView.visibility, equalTo(View.GONE))

            val removeVerificationView = fragment.getRemoveVerificationView()!!
            assertThat(removeVerificationView.visibility, equalTo(View.GONE))


            verify(application.analytics).setUserProperty(Analytics.PROPERTY_TWITTER_VERIFIED, false)
            verify(fragment.myTwitterProfile).clear()
        }
    }

    @Test
    fun `failing to deVerify shows toast`() {
        whenever(application.dropbitAccountHelper.isTwitterVerified).thenReturn(true)
        val identity = mock(DropbitMeIdentity::class.java)
        whenever(identity.handle).thenReturn("JohnnyNumber5")
        whenever(identity.type).thenReturn(IdentityType.TWITTER)
        whenever(application.dropbitAccountHelper.twitterIdentity()).thenReturn(identity)
        start()

        scenario.onFragment { fragment ->
            fragment.receiver.onReceive(fragment.context, Intent(DropbitIntents.ACTION_DEVERIFY_TWITTER_FAILED))
        }

        assertThat(ShadowToast.getTextOfLatestToast(), equalTo(getString(application, R.string.deverification_twitter_failed)))
    }

    @Test
    fun `verification requests authentication with twitter`() {
        whenever(application.dropbitAccountHelper.isTwitterVerified).thenReturn(false)
        start()
        val twitterUser = TwitterUser()
        twitterUser.screenName = "JohnnyNumber5"
        twitterUser.userId = 123456789L
        val resultData = Intent()
        resultData.putExtra(TwitterIntents.TWITTER_USER, twitterUser)
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)
        Intents.intending(toPackage(application.packageName)).respondWith(result)

        scenario.onFragment { fragment ->
            clickOn(fragment.findViewById(R.id.verify_button))
        }

        assertThat(Intents.getIntents()[0].component.className, equalTo(TwitterLoginActivity::class.java.name))
        scenario.onFragment { fragment ->
            verify(fragment.twitterVerificationController).onTwitterAuthorized(resultData)
        }
    }
}