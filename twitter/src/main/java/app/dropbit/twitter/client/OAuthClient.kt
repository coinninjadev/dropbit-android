package app.dropbit.twitter.client

import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import app.dropbit.annotations.Mockable
import app.dropbit.twitter.di.qualifiers.TwitterCallbackUrl
import javax.inject.Inject

@Mockable
class OAuthClient @Inject constructor(@TwitterCallbackUrl val uri: Uri) : WebViewClient() {

    interface Callback {
        fun onAuthComplete(uri: Uri)
    }

    private var onAuthorizeCallback: Callback? = null

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        if (request?.url.toString().startsWith(uri.toString())) {
            onAuthorizeCallback?.onAuthComplete(request!!.url)
        } else {
            view?.loadUrl(request?.url.toString())
        }
        return true
    }

    fun performAuth(view: WebView?, oAuthToken: String) {
        view?.loadUrl(TwitterApiClient.authRoute(oAuthToken).toString())
    }

    fun observeAuthorization(onAuthorizeCallback: Callback) {
        this.onAuthorizeCallback = onAuthorizeCallback
    }


}
