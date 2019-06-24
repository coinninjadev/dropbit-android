package app.dropbit.twitter.client

import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class OAuthClientTest {

    private val callbackUrl = Uri.parse("https://yourWhitelistedCallbackUrl.com")

    @Test
    fun `performs authorization for given auth token`() {
        val webView: WebView = mock(WebView::class.java)
        val oAuthClient = OAuthClient(callbackUrl)

        oAuthClient.performAuth(webView, "--auth-token--")

        verify(webView).loadUrl(TwitterApiClient.authRoute("--auth-token--").toString())
    }

    @Test
    fun `navigates to urls that are not the callback`() {
        val uri = Uri.parse("http://example.com")
        val callback = mock(OAuthClient.Callback::class.java)
        val request: WebResourceRequest = mock(WebResourceRequest::class.java)
        whenever(request.url).thenReturn(uri)
        val webView: WebView = mock(WebView::class.java)
        val oAuthClient = OAuthClient(callbackUrl)
        oAuthClient.observeAuthorization(callback)

        oAuthClient.shouldOverrideUrlLoading(webView, request)

        verify(webView).loadUrl(uri.toString())
        verifyZeroInteractions(callback)
    }

    @Test
    fun `intercepts callback route and notifies observer`() {
        val uri = Uri.parse("https://yourWhitelistedCallbackUrl.com?oauth_token=NPcudxy0yU5T3tBzho7iCotZ3cnetKwcTIRlX0iwRl0&oauth_verifier=uw7NjWHT6OJ1MpJOXsHfNxoAhPKpgI8BlYDhxEjIBY")
        val callback = mock(OAuthClient.Callback::class.java)
        val request: WebResourceRequest = mock(WebResourceRequest::class.java)
        whenever(request.url).thenReturn(uri)
        val webView: WebView = mock(WebView::class.java)
        val oAuthClient = OAuthClient(callbackUrl)
        oAuthClient.observeAuthorization(callback)

        oAuthClient.shouldOverrideUrlLoading(webView, request)

        verify(callback).onAuthComplete(uri)
        verifyZeroInteractions(webView)
    }

    @Test
    fun `intercepts callback route and notifies legacy observer`() {
        val uri = "http://example.com"
        val callback = mock(OAuthClient.Callback::class.java)
        val webView: WebView = mock(WebView::class.java)
        val oAuthClient = OAuthClient(callbackUrl)
        oAuthClient.observeAuthorization(callback)

        oAuthClient.shouldOverrideUrlLoading(webView, uri)

        verify(webView).loadUrl(uri)
        verifyZeroInteractions(callback)
    }
}