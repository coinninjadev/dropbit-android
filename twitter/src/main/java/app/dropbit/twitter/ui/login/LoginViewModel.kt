package app.dropbit.twitter.ui.login

import android.net.Uri
import androidx.lifecycle.ViewModel
import app.dropbit.annotations.Mockable
import app.dropbit.twitter.Twitter
import app.dropbit.twitter.model.RequestToken
import app.dropbit.twitter.model.TwitterUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Mockable
class LoginViewModel @Inject constructor(val twitter: Twitter) : ViewModel() {

    fun requestToken(authCallback: AuthCallback) {
        GlobalScope.launch {
            val requestToken = twitter.requestToken()
            withContext(Dispatchers.Main) {
                authCallback.onRequestTokenReceived(requestToken)
            }
        }
    }

    fun loadAuthToken(authCallback: AuthCallback, uri: Uri) {
        GlobalScope.launch {
            val twitterUser = twitter.requestAccessToken(uri)
            withContext(Dispatchers.Main) {
                authCallback.onAuthorizationTokenReceived(twitterUser)
            }
        }
    }

    interface AuthCallback {
        fun onRequestTokenReceived(requestToken: RequestToken)
        fun onAuthorizationTokenReceived(twitterUser: TwitterUser?)
    }

}
