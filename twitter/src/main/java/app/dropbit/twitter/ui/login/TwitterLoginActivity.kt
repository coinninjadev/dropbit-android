package app.dropbit.twitter.ui.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import app.dropbit.twitter.R
import app.dropbit.twitter.TwitterIntents
import app.dropbit.twitter.client.OAuthClient
import app.dropbit.twitter.client.OAuthClient.Callback
import app.dropbit.twitter.di.AppModule
import app.dropbit.twitter.di.DaggerComponent
import app.dropbit.twitter.model.RequestToken
import app.dropbit.twitter.model.TwitterUser
import javax.inject.Inject

class TwitterLoginActivity : AppCompatActivity() {

    @Inject
    internal lateinit var oAuthClient: OAuthClient

    @Inject
    internal lateinit var loginViewModel: LoginViewModel

    internal lateinit var authView: WebView

    internal var requestedAuth = false

    internal var loginViewModelCallback = object : LoginViewModel.AuthCallback {
        override fun onAuthorizationTokenReceived(twitterUser: TwitterUser?) {
            if (twitterUser != null) {
                val data = Intent()
                data.putExtra(TwitterIntents.TWITTER_USER, twitterUser)
                setResult(Activity.RESULT_OK, data)
            } else {
                setResult(Activity.RESULT_CANCELED)
            }
            finish()
        }

        override fun onRequestTokenReceived(requestToken: RequestToken) {
            authorize(requestToken)
        }
    }

    internal var oAuthCallback = object : Callback {
        override fun onAuthComplete(uri: Uri) {
            loginViewModel.loadAuthToken(loginViewModelCallback, uri)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(HAS_REQUSTED_AUTH, requestedAuth)
        authView.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            super.onRestoreInstanceState(savedInstanceState)
            requestedAuth = savedInstanceState.getBoolean(HAS_REQUSTED_AUTH, false)
            authView.restoreState(savedInstanceState)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        DaggerComponent.builder().appModule(AppModule(this)).build().inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_twitter_login)
        authView = findViewById(R.id.auth_view)
    }

    override fun onResume() {
        super.onResume()
        configureAuthView()
        requestToken()
    }

    private fun requestToken() {
        loginViewModel.requestToken(loginViewModelCallback)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureAuthView() {
        authView.apply {
            webViewClient = oAuthClient
            settings.apply {
                javaScriptEnabled = true
            }
        }
        oAuthClient.observeAuthorization(oAuthCallback)
    }

    private fun authorize(requestToken: RequestToken) {
        if (!requestedAuth) {
            oAuthClient.performAuth(authView, requestToken.oAuthToken)
            requestedAuth = true
        }
    }

    companion object {
        const val HAS_REQUSTED_AUTH = "HAS_REQUESTED_AUTH"
    }
}