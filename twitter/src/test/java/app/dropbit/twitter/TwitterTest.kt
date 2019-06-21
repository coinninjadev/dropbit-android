package app.dropbit.twitter

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.dropbit.twitter.client.TwitterApiClient
import app.dropbit.twitter.model.TwitterUser
import app.dropbit.twitter.providers.TokenProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import retrofit2.Response


@RunWith(AndroidJUnit4::class)
class TwitterTest {

    @Test
    fun `false when token or secret absent`() {
        val twitterApiClient = mock(TwitterApiClient::class.java)
        val tokenProvider = mock(TokenProvider::class.java)
        val twitter = Twitter(twitterApiClient, tokenProvider)
        whenever(twitter.tokenProvider.oAuthTokenSecret()).thenReturn("").thenReturn("secret").thenReturn("secret")
        whenever(twitter.tokenProvider.oAuthToken()).thenReturn("token").thenReturn("").thenReturn("token")

        assertFalse(twitter.hasTwitterEnabled)
        assertFalse(twitter.hasTwitterEnabled)
        assertTrue(twitter.hasTwitterEnabled)
    }

    @Test
    fun `proxies request token`() {
        val twitterApiClient = mock(TwitterApiClient::class.java)
        val tokenProvider = mock(TokenProvider::class.java)
        val twitter = Twitter(twitterApiClient, tokenProvider)
        val response = Response.success("oauth_token=geXF2wAAAAAA-WA5AAABarxZfW8&oauth_token_secret=f0i2IsdnFUOrC9B70hgOSw8i8sCNCFBu&oauth_callback_confirmed=true")
        whenever(twitterApiClient.requestToken()).thenReturn(response)

        runBlocking {
            val requestToken = twitter.requestToken()

            verify(tokenProvider).clear()
            verify(tokenProvider).saveRequestToken(requestToken)
            assertThat(requestToken.oAuthToken, equalTo("geXF2wAAAAAA-WA5AAABarxZfW8"))
        }
    }

    @Test
    fun `proxies access token request`() {
        val twitterApiClient = mock(TwitterApiClient::class.java)
        val tokenProvider = mock(TokenProvider::class.java)
        val twitter = Twitter(twitterApiClient, tokenProvider)
        val uri = Uri.parse("callback://?oauth_token=-hzb4gAAAAAA-WA5AAABasKFNPk&oauth_verifier=K75pbwsbl2PCim0cQqqeKMGpZVZKPRnX")
        val responseString = "oauth_token=authToken&oauth_token_secret=tokenSecret&user_id=11237&screen_name=screenName"
        val response = Response.success(responseString)
        val twitterUser = TwitterUser()
        val userResponse = Response.success(twitterUser)
        whenever(twitterApiClient.accessToken()).thenReturn(response)
        whenever(tokenProvider.userId()).thenReturn("0")
        whenever(tokenProvider.screenName()).thenReturn("screenName")
        whenever(twitterApiClient.getUser(any(), any(), eq(true))).thenReturn(userResponse)

        runBlocking {
            assertThat(twitter.requestAccessToken(uri), equalTo(twitterUser))
        }

        verify(tokenProvider).saveRequestAuthToken("-hzb4gAAAAAA-WA5AAABasKFNPk", "K75pbwsbl2PCim0cQqqeKMGpZVZKPRnX")
        verify(tokenProvider).saveAccessToken(any())
    }

    @Test
    fun `provides access to twitter screen name`() {
        val twitterApiClient = mock(TwitterApiClient::class.java)
        val tokenProvider = mock(TokenProvider::class.java)
        whenever(tokenProvider.screenName()).thenReturn("screenName")
        val twitter = Twitter(twitterApiClient, tokenProvider)

        assertThat(twitter.screenName(), equalTo("screenName"))

    }

    @Test
    fun `provides access to userId`() {
        val twitterApiClient = mock(TwitterApiClient::class.java)
        val tokenProvider = mock(TokenProvider::class.java)
        whenever(tokenProvider.userId()).thenReturn("userId")
        val twitter = Twitter(twitterApiClient, tokenProvider)

        assertThat(twitter.userId(), equalTo("userId"))
    }

    @Test
    fun `provides access to authToken`() {
        val twitterApiClient = mock(TwitterApiClient::class.java)
        val tokenProvider = mock(TokenProvider::class.java)
        whenever(tokenProvider.oAuthToken()).thenReturn("authToken")
        val twitter = Twitter(twitterApiClient, tokenProvider)

        assertThat(twitter.authToken, equalTo("authToken"))
    }

    @Test
    fun `provides access to authSecret`() {
        val twitterApiClient = mock(TwitterApiClient::class.java)
        val tokenProvider = mock(TokenProvider::class.java)
        whenever(tokenProvider.oAuthTokenSecret()).thenReturn("authSecret")
        val twitter = Twitter(twitterApiClient, tokenProvider)

        assertThat(twitter.authSecret, equalTo("authSecret"))
    }

    @Test
    fun `fetches user with provided snowflake and handle`() {
        val twitterApiClient = mock(TwitterApiClient::class.java)
        val tokenProvider = mock(TokenProvider::class.java)
        whenever(tokenProvider.oAuthTokenSecret()).thenReturn("authSecret")
        val twitter = Twitter(twitterApiClient, tokenProvider)
        val twitterUser = mock(TwitterUser::class.java)
        val twitterUserResponse = Response.success(twitterUser)
        val snowflake = 12345L
        val handle = "myHandle"
        whenever(twitterApiClient.getUser(snowflake, handle)).thenReturn(twitterUserResponse)

        runBlocking {
            assertThat(twitter.getUser(snowflake, handle), equalTo(twitterUser))
        }
    }
}