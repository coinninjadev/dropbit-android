package com.coinninja.coinkeeper.ui.account.verify.twitter

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.dropbit.twitter.TwitterIntents
import app.dropbit.twitter.model.TwitterUser
import com.coinninja.coinkeeper.ui.dropbit.me.DropbitMeConfiguration
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil
import com.coinninja.coinkeeper.util.android.ServiceWorkUtil
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.matchers.IntentFilterMatchers
import junit.framework.Assert.assertNull
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class TwitterVerificationControllerTest {

    fun setup(): TwitterVerificationController {
        return TwitterVerificationController(mock(ServiceWorkUtil::class.java),
                mock(ActivityNavigationUtil::class.java),
                mock(DropbitMeConfiguration::class.java),
                mock(LocalBroadCastUtil::class.java))
    }

    @Test
    fun `observes Twitter Verified broadcasts when started`() {
        val controller = setup()

        controller.onStarted(ApplicationProvider.getApplicationContext(), null)

        assertThat(controller.intentFilter, IntentFilterMatchers.containsAction(DropbitIntents.ACTION_VERIFY_TWITTER_COMPLETED))
        verify(controller.localBroadCastUtil).registerReceiver(controller, controller.intentFilter)
    }

    @Test
    fun `removes observer when stopped`() {
        val controller = setup()
        controller.onStarted(ApplicationProvider.getApplicationContext(), null)

        controller.onStopped()

        assertNull(controller.activityContext)
        verify(controller.localBroadCastUtil).unregisterReceiver(controller)
    }

    @Test
    fun `verifies with dropbit when twitter user authorized`() {
        val controller = setup()
        val context = ApplicationProvider.getApplicationContext<Context>()
        controller.onStarted(context, null)
        val twitterUser = TwitterUser()
        twitterUser.screenName = "JohnnyNumber5"
        twitterUser.userId = 123456789L
        val resultData = Intent()
        resultData.putExtra(TwitterIntents.TWITTER_USER, twitterUser)

        controller.onTwitterAuthorized(resultData)

        verify(controller.serviceWorkUtil).addVerifiedTwitterAccount(123456789L)
    }

    @Test
    fun `navigates to home screen with dropbit configured to show  when verification completed`() {
        val controller = setup()
        val context = ApplicationProvider.getApplicationContext<Context>()
        controller.onStarted(context, null)

        controller.onReceive(context, Intent(DropbitIntents.ACTION_VERIFY_TWITTER_COMPLETED))

        verify(controller.dropbitMeConfiguration).setInitialVerification()
        verify(controller.activityNavigationUtil).navigateToHome(controller.activityContext)

    }
}