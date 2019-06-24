package app.dropbit.twitter.ui.login

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.dropbit.twitter.Twitter
import app.dropbit.twitter.model.RequestToken
import app.dropbit.twitter.model.TwitterUser
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.times

@RunWith(AndroidJUnit4::class)
class LoginViewModelTest {

    @Test
    fun `forwards retrieved request token`() {
        GlobalScope.launch {
            val requestToken = RequestToken()
            val twitter = mock(Twitter::class.java)
            whenever(twitter.requestToken()).thenReturn(requestToken)
            val callback = mock(LoginViewModel.AuthCallback::class.java)
            val loginViewModel = LoginViewModel(twitter)

            loginViewModel.requestToken(callback)

            verify(callback, times(2)).onRequestTokenReceived(requestToken)
            verify(callback).onRequestTokenReceived(requestToken)
        }
    }

    @Test
    fun `fetch user auth token from authorization`() {
        GlobalScope.launch {
            val uri = Uri.parse("https://yourWhitelistedCallbackUrl.com?oauth_token=NPcudxy0yU5T3tBzho7iCotZ3cnetKwcTIRlX0iwRl0&oauth_verifier=uw7NjWHT6OJ1MpJOXsHfNxoAhPKpgI8BlYDhxEjIBY")
            val twitter = mock(Twitter::class.java)
            val loginViewModel = LoginViewModel(twitter)
            val twitterUser = TwitterUser()
            whenever(twitter.requestAccessToken(uri)).thenReturn(twitterUser)
            val callback = mock(LoginViewModel.AuthCallback::class.java)

            loginViewModel.loadAuthToken(callback, uri)

            verify(callback).onAuthorizationTokenReceived(twitterUser)
        }
    }
}