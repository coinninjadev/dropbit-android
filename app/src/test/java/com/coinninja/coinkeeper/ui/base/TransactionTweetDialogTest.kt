package com.coinninja.coinkeeper.ui.base

import android.widget.Button
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.util.TwitterUtil
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class TransactionTweetDialogTest {

    companion object {
        const val tag = "TAG"
    }

    fun setup(): ActivityScenario<TestableActivity> {
        val activityScenario = ActivityScenario.launch(TestableActivity::class.java)
        val transactionTweetDialog = TransactionTweetDialog(mock(Analytics::class.java), Picasso.get(), mock(TwitterUtil::class.java), mock(SignedCoinKeeperApiClient::class.java))
        val identity = mock(Identity::class.java)
        transactionTweetDialog.receivingIdentity = identity
        whenever(identity.avatarUrl).thenReturn("--url--")
        transactionTweetDialog.inviteId = "---id---"
        activityScenario.moveToState(Lifecycle.State.RESUMED)
        activityScenario.onActivity {
            transactionTweetDialog.show(it.supportFragmentManager, tag)
        }

        return activityScenario
    }

    @Test
    @Ignore
    fun `test dropbit sends tweet is clicked`() {
        val signedCoinKeeperApiClient = mock(SignedCoinKeeperApiClient::class.java)
        val activity = setup()
        activity.onActivity { activity ->
            val dialog = activity.supportFragmentManager.findFragmentByTag(tag) as TransactionTweetDialog

            assertThat(activity.getString(R.string.app_name), equalTo(""))
            (dialog.view?.findViewById(R.id.dropbit_tweet_button) as Button).performClick()
            verify(signedCoinKeeperApiClient).patchSuppressionForWalletAddressRequest(any())
        }
    }

    @Test
    @Ignore
    fun `test you send tweet is clicked`() {
        val twitterUtil = mock(TwitterUtil::class.java)
        val scenario = setup()
        scenario.onActivity { activity ->
            val dialog = activity.supportFragmentManager.findFragmentByTag(tag) as TransactionTweetDialog
            (dialog.view?.findViewById(R.id.tweet_yourself_button) as Button).performClick()
            verify(twitterUtil).createTwitterIntent(any(), any())
        }
    }
}