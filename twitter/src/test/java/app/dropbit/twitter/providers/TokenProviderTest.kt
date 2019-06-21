package app.dropbit.twitter.providers

import android.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.dropbit.twitter.model.AccessTokenResponse
import app.dropbit.twitter.model.RequestToken
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TokenProviderTest {

    @Test
    fun `saves token secret`() {
        val tokenProvider = TokenProvider(ApplicationProvider.getApplicationContext())
        val requestToken = RequestToken()
        requestToken.oAuthToken = "--auth-token--"
        requestToken.oAuthTokenSecret = "--auth-token-secret-"
        requestToken.oAuthCallbackConfirmed = true
        val preferences = PreferenceManager.getDefaultSharedPreferences(tokenProvider.context)

        tokenProvider.saveRequestToken(requestToken)

        assertThat(preferences.getString(TokenProvider.authTokenSecretKey, ""),
                equalTo(requestToken.oAuthTokenSecret))
    }

    @Test
    fun `saves request token`() {
        val requestAuthToken = "--request-auth-token--"
        val authVerifier = "--auth-verifier--"
        val tokenProvider = TokenProvider(ApplicationProvider.getApplicationContext())
        val preferences = PreferenceManager.getDefaultSharedPreferences(tokenProvider.context)

        tokenProvider.saveRequestAuthToken(requestAuthToken, authVerifier)

        assertThat(preferences.getString(TokenProvider.requestAuthTokenKey, ""), equalTo(requestAuthToken))
        assertThat(preferences.getString(TokenProvider.verifierKey, ""), equalTo(authVerifier))
    }

    @Test
    fun `saves access token response`() {
        val accessTokenResponse = AccessTokenResponse()
        accessTokenResponse.oauthToken = "-oauth-token-"
        accessTokenResponse.oauthSecret = "-oauth-secret-"
        accessTokenResponse.userId = "123456"
        accessTokenResponse.screenName = "-screen name-"
        val tokenProvider = TokenProvider(ApplicationProvider.getApplicationContext())
        val preferences = PreferenceManager.getDefaultSharedPreferences(tokenProvider.context)
        val editor = preferences.edit()
        editor.putString(TokenProvider.verifierKey, "--verifierKey--")
        editor.putString(TokenProvider.requestAuthTokenKey, "--requestKey--")
        editor.apply()

        tokenProvider.saveAccessToken(accessTokenResponse)

        assertThat(preferences.getString(TokenProvider.authTokenKey, ""), equalTo(accessTokenResponse.oauthToken))
        assertThat(preferences.getString(TokenProvider.authTokenSecretKey, ""), equalTo(accessTokenResponse.oauthSecret))
        assertThat(preferences.getString(TokenProvider.userIdKey, ""), equalTo(accessTokenResponse.userId))
        assertThat(preferences.getString(TokenProvider.screenNameKey, ""), equalTo(accessTokenResponse.screenName))

        // deletes request / verifier tokens
        assertThat(preferences.getString(TokenProvider.requestAuthTokenKey, ""), equalTo(""))
        assertThat(preferences.getString(TokenProvider.verifierKey, ""), equalTo(""))
    }

    @Test
    fun `retrieves screen name from storage`() {
        val tokenProvider = TokenProvider(ApplicationProvider.getApplicationContext())
        val preferences = PreferenceManager.getDefaultSharedPreferences(tokenProvider.context)
        val editor = preferences.edit()
        val screenName = "--screen-name--"
        editor.putString(TokenProvider.screenNameKey, screenName)
        editor.apply()

        assertThat(tokenProvider.screenName(), equalTo(screenName))
    }

    @Test
    fun `retrieves userId from storage`() {
        val tokenProvider = TokenProvider(ApplicationProvider.getApplicationContext())
        val preferences = PreferenceManager.getDefaultSharedPreferences(tokenProvider.context)
        val editor = preferences.edit()
        val userId = "--user-id--"
        editor.putString(TokenProvider.userIdKey, userId)
        editor.apply()

        assertThat(tokenProvider.userId(), equalTo(userId))
    }

    @Test
    fun `retrieves auth token secret from storage`() {
        val tokenProvider = TokenProvider(ApplicationProvider.getApplicationContext())
        val preferences = PreferenceManager.getDefaultSharedPreferences(tokenProvider.context)
        val editor = preferences.edit()
        val authTokenSecret = "-token-secret-"
        editor.putString(TokenProvider.authTokenSecretKey, authTokenSecret)
        editor.apply()

        assertThat(tokenProvider.oAuthTokenSecret(), equalTo(authTokenSecret))
    }

    @Test
    fun `retrieves auth token from storage`() {
        val tokenProvider = TokenProvider(ApplicationProvider.getApplicationContext())
        val preferences = PreferenceManager.getDefaultSharedPreferences(tokenProvider.context)
        val editor = preferences.edit()
        val authToken = "-token-"
        editor.putString(TokenProvider.authTokenKey, authToken)
        editor.apply()

        assertThat(tokenProvider.oAuthToken(), equalTo(authToken))
    }

    @Test
    fun `provides verifier token`() {
        val tokenProvider = TokenProvider(ApplicationProvider.getApplicationContext())
        val preferences = PreferenceManager.getDefaultSharedPreferences(tokenProvider.context)
        val editor = preferences.edit()
        val token = "-verifier auth-token-"
        editor.putString(TokenProvider.verifierKey, token)
        editor.apply()

        assertThat(tokenProvider.verifierToken(), equalTo(token))
    }

    @Test
    fun `retrieves auth request token from storage`() {
        val tokenProvider = TokenProvider(ApplicationProvider.getApplicationContext())
        val preferences = PreferenceManager.getDefaultSharedPreferences(tokenProvider.context)
        val editor = preferences.edit()
        val authRequestToken = "-request-token-"
        editor.putString(TokenProvider.requestAuthTokenKey, authRequestToken)
        editor.apply()

        assertThat(tokenProvider.oAuthRequestToken(), equalTo(authRequestToken))
    }

    @Test
    fun `provides access token when it is available`() {
        val tokenProvider = TokenProvider(ApplicationProvider.getApplicationContext())
        val preferences = PreferenceManager.getDefaultSharedPreferences(tokenProvider.context)
        val editor = preferences.edit()
        val token = "-auth-token-"
        editor.putString(TokenProvider.authTokenKey, token)
        editor.apply()

        assertThat(tokenProvider.accessToken(), equalTo(token))
    }

    @Test
    fun `provides request token when auth token is not available`() {
        val tokenProvider = TokenProvider(ApplicationProvider.getApplicationContext())
        val preferences = PreferenceManager.getDefaultSharedPreferences(tokenProvider.context)
        val editor = preferences.edit()
        val token = "-request auth-token-"
        editor.putString(TokenProvider.requestAuthTokenKey, token)
        editor.apply()

        assertThat(tokenProvider.accessToken(), equalTo(token))
    }

    @Test
    fun `provides empty string when auth token and request token is not available`() {
        val tokenProvider = TokenProvider(ApplicationProvider.getApplicationContext())

        assertThat(tokenProvider.accessToken(), equalTo(""))
    }

    @Test
    fun `clears storage`() {
        val tokenProvider = TokenProvider(ApplicationProvider.getApplicationContext())
        val preferences = PreferenceManager.getDefaultSharedPreferences(tokenProvider.context)
        val editor = preferences.edit()
        editor.putString(TokenProvider.requestAuthTokenKey, "request-token")
        editor.putString(TokenProvider.requestAuthTokenKey, "verifier-token")
        editor.putString(TokenProvider.authTokenKey, "auth-token")
        editor.putString(TokenProvider.authTokenSecretKey, "auth-token-secret")
        editor.putString(TokenProvider.screenNameKey, "screen-name")
        editor.putString(TokenProvider.userIdKey, "user-id")
        editor.apply()

        tokenProvider.clear()

        assertThat(tokenProvider.oAuthRequestToken(), equalTo(""))
        assertThat(tokenProvider.oAuthToken(), equalTo(""))
        assertThat(tokenProvider.oAuthTokenSecret(), equalTo(""))
        assertThat(tokenProvider.screenName(), equalTo(""))
        assertThat(tokenProvider.userId(), equalTo(""))
        assertThat(tokenProvider.verifierToken(), equalTo(""))
    }
}
