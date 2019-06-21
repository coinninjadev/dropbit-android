package app.dropbit.twitter.util

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.dropbit.commons.util.DateUtil
import app.dropbit.commons.util.HmacSHA1Signer
import app.dropbit.twitter.Constant
import app.dropbit.twitter.client.Configuration
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertNull
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations


@RunWith(AndroidJUnit4::class)
class AuthorizationTest {

    val timeInSeconds = 1318622958L

    @Mock
    lateinit var dateUtil: DateUtil

    @Mock
    lateinit var configuration: Configuration

    lateinit var authorization: Authorization

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(dateUtil.timeInSeconds()).thenReturn(timeInSeconds)
        whenever(configuration.apiKey()).thenReturn("--api-key--")
        whenever(configuration.secretKey()).thenReturn("--signing-key--")
        whenever(configuration.callBackRoute()).thenReturn("https://example.com")
        whenever(configuration.signingKey()).thenCallRealMethod()
        whenever(configuration.accessToken()).thenReturn("")
        whenever(configuration.verifierToken()).thenReturn("")
        authorization = Authorization(dateUtil, configuration, HmacSHA1Signer())
    }

    @Test
    fun `add post parameters to signature`() {
        whenever(configuration.verifierToken()).thenReturn("--verifier-token--")
        val requestBuilder = Request.Builder()
        requestBuilder.url("https://api.twitter.com/oauth/access_token")
        requestBuilder.method("POST", RequestBody.create(MediaType.parse("plain/text"), ""))

        val parameters: Map<String, String> = authorization.parametersForRequest(requestBuilder.build())

        assertThat(parameters.get(Constant.OAUTH_VERIFIER_KEY), equalTo("--verifier-token--"))
    }

    @Test
    fun `adds authorization header to provided request`() {
        val requestBuilder = Request.Builder()
        requestBuilder.url("https://api.twitter.com/oauth/request_token")
        requestBuilder.method("POST", RequestBody.create(MediaType.parse("plain/text"), ""))
        val expectedAuthHeader = authorization.asHeaderString(requestBuilder)

        authorization.addAuthorizationHeader(requestBuilder)

        val request = requestBuilder.build()
        assertThat(request.headers().get("Authorization"), equalTo(expectedAuthHeader))
    }

    @Test
    fun `generates authorization header for request token`() {
        val requestBuilder = Request.Builder()
        requestBuilder.url("https://api.twitter.com/oauth/request_token?foo=bar")
        requestBuilder.method("POST", RequestBody.create(MediaType.parse("plain/text"), ""))

        assertThat(authorization.asHeaderString(requestBuilder), equalTo("OAuth " +
                "oauth_callback=\"https%3A%2F%2Fexample.com\", " +
                "oauth_consumer_key=\"--api-key--\", " +
                "oauth_nonce=\"MTMxODYyMjk1OA%3D%3D\", " +
                "oauth_signature=\"_bE2roANlPrL5862NUNqpq9s5XE%3D\", " +
                "oauth_signature_method=\"HMAC-SHA1\", " +
                "oauth_timestamp=\"1318622958\", " +
                "oauth_version=\"1.0\""
        ))
    }

    @Test
    fun `builds a parameters required for signing`() {
        val parameters: Map<String, String> = authorization.parametersForSigning()
        assertThat(parameters.get(Constant.OAUTH_TIMESTAMP_KEY), equalTo(timeInSeconds.toString()))
        assertThat(parameters.get(Constant.OAUTH_NONCE_KEY), equalTo("MTMxODYyMjk1OA=="))
        assertThat(parameters.get(Constant.OAUTH_CONSUMER_KEY_KEY), equalTo("--api-key--"))
        assertThat(parameters.get(Constant.OAUTH_SIGNATURE_METHOD_KEY), equalTo("HMAC-SHA1"))
        assertThat(parameters.get(Constant.OAUTH_VERSION_KEY), equalTo("1.0"))
    }

    @Test
    fun `adds auth token to parameters when signing and available`() {
        whenever(configuration.accessToken()).thenReturn("--access-token--")
        val parameters: Map<String, String> = authorization.parametersForSigning()
        assertThat(parameters.get(Constant.OAUTH_TOKEN_KEY), equalTo("--access-token--"))
        assertNull(parameters.get(Constant.OAUTH_CALLBACK_KEY))
    }

    @Test
    fun `adds callback token when access token not available`() {
        val parameters: Map<String, String> = authorization.parametersForSigning()
        assertNull(parameters.get(Constant.OAUTH_TOKEN_KEY))
        assertThat(parameters.get(Constant.OAUTH_CALLBACK_KEY), equalTo("https://example.com"))
    }

    @Test
    fun `builds a parameters required for signing with callback containing query string arguments`() {
        val requestBuilder = Request.Builder()
        requestBuilder.url("https://api.twitter.com/oauth/request_token?foo=bar&fiz=ban")
        requestBuilder.method("POST", RequestBody.create(MediaType.parse("plain/text"), ""))
        val parameters: Map<String, String> = authorization.parametersForRequest(requestBuilder.build())

        assertThat(parameters.get(Constant.OAUTH_TIMESTAMP_KEY), equalTo(timeInSeconds.toString()))
        assertThat(parameters.get(Constant.OAUTH_NONCE_KEY), equalTo("MTMxODYyMjk1OA=="))
        assertThat(parameters.get(Constant.OAUTH_CONSUMER_KEY_KEY), equalTo("--api-key--"))
        assertThat(parameters.get(Constant.OAUTH_SIGNATURE_METHOD_KEY), equalTo("HMAC-SHA1"))
        assertThat(parameters.get(Constant.OAUTH_VERSION_KEY), equalTo("1.0"))
        assertThat(parameters.get("foo"), equalTo("bar"))
        assertThat(parameters.get("fiz"), equalTo("ban"))
    }

    @Test
    fun `builds signature base string`() {
        val requestBuilder = Request.Builder()
        requestBuilder.url("https://api.twitter.com/oauth/request_token")
        requestBuilder.method("POST", RequestBody.create(MediaType.parse("plain/text"), ""))
        val parameters: Map<String, String> = authorization.parametersForRequest(requestBuilder.build())

        assertThat(authorization.buildSignatureBaseString(parameters),
                equalTo("oauth_callback=https%3A%2F%2Fexample.com&" +
                        "oauth_consumer_key=--api-key--&" +
                        "oauth_nonce=MTMxODYyMjk1OA%3D%3D&" +
                        "oauth_signature_method=HMAC-SHA1&" +
                        "oauth_timestamp=1318622958&" +
                        "oauth_version=1.0"
                ))
    }

    @Test
    fun `builds signature string for web resource request`() {
        val requestBuilder = Request.Builder()
        val uri = Uri.parse("https://api.twitter.com/oauth/request_token?foo=bar")
        requestBuilder.url(uri.toString())
        requestBuilder.method("post", null)
        val request = requestBuilder.build()
        val parameters: Map<String, String> = authorization.parametersForRequest(request)

        val stringToSign: String = authorization.buildSigningString(uri, request.method(), parameters)

        assertThat(stringToSign, equalTo("POST&https%3A%2F%2Fapi.twitter.com%2Foauth%2Frequest_token&" +
                "foo%3Dbar%26" +
                "oauth_callback%3Dhttps%253A%252F%252Fexample.com%26" +
                "oauth_consumer_key%3D--api-key--%26" +
                "oauth_nonce%3DMTMxODYyMjk1OA%253D%253D%26" +
                "oauth_signature_method%3DHMAC-SHA1%26" +
                "oauth_timestamp%3D1318622958%26" +
                "oauth_version%3D1.0"
        ))

    }
}

