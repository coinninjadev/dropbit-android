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

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        url?.let {
            handleDropbitUri(view, Uri.parse(it))
        }
        return true
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        request?.let {
            handleDropbitUri(view, it.url)
        }
        return true
    }

    private fun handleDropbitUri(view: WebView?, uri: Uri) {
        if (uri.toString().startsWith(this.uri.toString())) {
            onAuthorizeCallback?.onAuthComplete(uri)
        } else {
            view?.loadUrl(uri.toString())
        }
    }

    fun performAuth(view: WebView?, oAuthToken: String) {
        view?.loadUrl(TwitterApiClient.authRoute(oAuthToken).toString())
    }


    fun observeAuthorization(onAuthorizeCallback: Callback) {
        this.onAuthorizeCallback = onAuthorizeCallback
    }


}
