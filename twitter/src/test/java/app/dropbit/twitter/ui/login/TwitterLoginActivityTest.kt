package app.dropbit.twitter.ui.login

import android.app.Activity
import android.app.Application
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.dropbit.twitter.TwitterIntents
import app.dropbit.twitter.client.OAuthClient
import app.dropbit.twitter.model.RequestToken
import app.dropbit.twitter.model.TwitterUser
import junit.framework.Assert.assertTrue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowPackageManager

@RunWith(AndroidJUnit4::class)
class TwitterLoginActivityTest {
    private val loginViewModel = mock(LoginViewModel::class.java)
    private val oAuthClient = mock(OAuthClient::class.java)

    private lateinit var scenario: ActivityScenario<TwitterLoginActivity>

    @Before
    fun setup() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val packageManager = application.packageManager
        val shadowPackageManager: ShadowPackageManager = shadowOf(packageManager)
        val activityInfo = ActivityInfo()
        activityInfo.theme = androidx.appcompat.R.style.Base_Theme_AppCompat
        activityInfo.packageName = application.packageName
        activityInfo.name = TwitterLoginActivity::class.java.name
        shadowPackageManager.addOrUpdateActivity(activityInfo)
        scenario = ActivityScenario.launch(TwitterLoginActivity::class.java)
        scenario.moveToState(Lifecycle.State.CREATED)
        scenario.onActivity { activity ->
            activity.loginViewModel = loginViewModel
            activity.oAuthClient = oAuthClient
        }
        scenario.moveToState(Lifecycle.State.RESUMED)
    }

    @After
    fun teardown() {
        scenario.close()
    }

    @Test
    fun `fetches request token when started`() {
        scenario.onActivity { activity ->
            verify(loginViewModel).requestToken(activity.loginViewModelCallback)
        }
    }

    @Test
    fun `authorizes access when token received`() {
        scenario.onActivity { activity ->
            activity.loginViewModelCallback
                    .onRequestTokenReceived(RequestToken("--oauth-token--",
                            "--oauth-token-secret--",
                            true))
            verify(oAuthClient).performAuth(activity.authView, "--oauth-token--")
        }
    }

    @Test
    fun `sets callback for oauth authorization`() {
        scenario.onActivity { activity ->
            verify(oAuthClient).observeAuthorization(activity.oAuthCallback)
        }
    }

    @Test
    fun `requests view model to convert to oauth token`() {
        val uri = Uri.parse("https://yourWhitelistedCallbackUrl.com?oauth_token=NPcudxy0yU5T3tBzho7iCotZ3cnetKwcTIRlX0iwRl0&oauth_verifier=uw7NjWHT6OJ1MpJOXsHfNxoAhPKpgI8BlYDhxEjIBY")

        scenario.onActivity { activity ->
            activity.oAuthCallback.onAuthComplete(uri)

            verify(loginViewModel).loadAuthToken(activity.loginViewModelCallback, uri)
        }

    }

    @Test
    fun `authorization without twitter user result cancels request`() {
        scenario.onActivity { activity ->
            activity.loginViewModelCallback.onAuthorizationTokenReceived(null)
            val shadowActivity = shadowOf(activity)
            assertThat(shadowActivity.resultCode, equalTo(Activity.RESULT_CANCELED))
            assertTrue(activity.isFinishing)
        }
    }

    @Test
    fun `finishes with success result once token obtained`() {
        val twitterUser = TwitterUser()
        twitterUser.screenName = "my screen name"
        twitterUser.userId = 123245623L

        scenario.onActivity { activity ->
            activity.loginViewModelCallback.onAuthorizationTokenReceived(twitterUser)
            val shadowActivity = shadowOf(activity)
            val data = shadowActivity.resultIntent
            assertThat(shadowActivity.resultCode, equalTo(Activity.RESULT_OK))
            assertTrue(activity.isFinishing)
            val user: TwitterUser = data.getParcelableExtra(TwitterIntents.TWITTER_USER)
            assertThat(user.screenName, equalTo(twitterUser.screenName))
            assertThat(user.userId, equalTo(twitterUser.userId))
        }
    }

    @Test
    fun `retains uri state when leaving`() {
        val url = "http://twitter.com/step/one"
        scenario.onActivity { activity ->
            activity.authView.loadUrl(url)
        }

        scenario.recreate()

        scenario.onActivity { activity ->
            assertThat(shadowOf(activity.authView).lastLoadedUrl, equalTo(url))
        }

    }
}