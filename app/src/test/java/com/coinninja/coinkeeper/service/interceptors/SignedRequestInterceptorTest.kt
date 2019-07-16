package com.coinninja.coinkeeper.service.interceptors

import com.coinninja.coinkeeper.model.db.Account
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.ArgumentCaptor
import java.io.IOException

class SignedRequestInterceptorTest {

    private val content = "{ \"phoneNumber\": \"330-555-5555\",\"countryCode\": 1}"
    private val body = RequestBody.create(MediaType.parse("text/json"), content)
    private val originalRequest = Request.Builder().url("http://example.com").post(body).build()
    private val chain: Interceptor.Chain = mock()

    private fun createInterceptor(): SignedRequestInterceptor {
        val interceptor = SignedRequestInterceptor(mock(), mock(), mock(), mock())
        val account: Account = mock()
        val response = Response.Builder()
                .protocol(Protocol.HTTP_2)
                .request(originalRequest)
                .message("")
                .code(200)
                .body(ResponseBody.create(MediaType.parse("plain/text"), ""))
                .build()
        whenever(account.cnWalletId).thenReturn(CN_WALLET_ID)
        whenever(account.cnUserId).thenReturn(CN_USER_ID)
        whenever(chain.request()).thenReturn(originalRequest)
        whenever(chain.proceed(any())).thenReturn(response)
        whenever(interceptor.cnWalletManager.account).thenReturn(account)
        whenever(interceptor.uuidFactory.provideUuid()).thenReturn(CN_AUTH_DEVICE_UUID)
        whenever(interceptor.dataSigner.sign(any())).thenReturn(SIGNED_CONTENT)
        whenever(interceptor.dateUtil.getCurrentTimeFormatted()).thenReturn(CURRENT_TIME)
        return interceptor
    }

    @Test
    fun does_not_add_user_id_when_one_does_not_exist() {
        val interceptor = createInterceptor()
        val argument: ArgumentCaptor<Request> = ArgumentCaptor.forClass(Request::class.java)
        whenever(interceptor.cnWalletManager.account.cnUserId).thenReturn(null)
        interceptor.intercept(chain)

        verify(interceptor.dataSigner).sign(content)
        verify(chain).proceed(argument.capture())

        val request = argument.value

        assertNull(request.headers().get(SignedRequestInterceptor.CN_AUTH_USER_ID))
    }


    @Test
    fun signs_timestamp_for_get_requests() {
        val interceptor = createInterceptor()
        var request = Request.Builder().url("http://localhost:8080").method("GET", null).build()
        whenever(interceptor.dataSigner.sign(CURRENT_TIME)).thenReturn(SIGNED_TIME_STAMP)

        request = interceptor.signRequest(request)

        assertThat(request.headers().get(SignedRequestInterceptor.CN_AUTH_SIG),
                equalTo(SIGNED_TIME_STAMP))
    }

    @Test
    fun signs_timestamp_as_content_when_body_of_post_is_empty() {
        val interceptor = createInterceptor()
        val body = RequestBody.create(MediaType.parse("application/json"), "")
        var request = Request.Builder().url("http://localhost:8080").method("POST", body).build()
        whenever(interceptor.dataSigner.sign(CURRENT_TIME)).thenReturn(SIGNED_TIME_STAMP)

        request = interceptor.signRequest(request)

        assertThat(request.headers().get(SignedRequestInterceptor.CN_AUTH_SIG),
                equalTo(SIGNED_TIME_STAMP))

    }

    @Test
    fun adds_UUID_to_header_test() {
        val interceptor = createInterceptor()
        val argument = ArgumentCaptor.forClass(Request::class.java)

        interceptor.intercept(chain)

        verify(interceptor.dataSigner).sign(content)
        verify(chain).proceed(argument.capture())

        val request = argument.value
        assertThat(request.headers().get(SignedRequestInterceptor.CN_AUTH_DEVICE_UUID), equalTo(CN_AUTH_DEVICE_UUID))
    }

    @Test
    @Throws(IOException::class)
    fun does_not_add_wallet_id_when_one_doesnt_exist() {
        val interceptor = createInterceptor()
        val argument = ArgumentCaptor.forClass(Request::class.java)
        whenever(interceptor.cnWalletManager.account.cnWalletId).thenReturn(null)

        interceptor.intercept(chain)

        verify(interceptor.dataSigner).sign(content)
        verify(chain).proceed(argument.capture())

        val request = argument.value

        assertNull(request.headers().get(SignedRequestInterceptor.CN_AUTH_WALLET_ID))
    }

    @Test
    @Throws(IOException::class)
    fun add_account_wallet_id_on_requests() {
        val interceptor = createInterceptor()

        val argument = ArgumentCaptor.forClass(Request::class.java)

        interceptor.intercept(chain)

        verify(interceptor.dataSigner).sign(content)
        verify(chain).proceed(argument.capture())

        val request = argument.value
        assertThat(request.headers().get(SignedRequestInterceptor.CN_AUTH_WALLET_ID),
                equalTo(CN_WALLET_ID))

    }

    @Test
    @Throws(IOException::class)
    fun adds_timestamp_to_request() {
        val interceptor = createInterceptor()
        val argument = ArgumentCaptor.forClass(Request::class.java)

        interceptor.intercept(chain)

        verify(interceptor.dataSigner).sign(content)
        verify(chain).proceed(argument.capture())

        val request = argument.value
        assertThat(request.headers().get(SignedRequestInterceptor.CN_AUTH_TIMESTAMP),
                equalTo(CURRENT_TIME))

    }

    @Test
    @Throws(IOException::class)
    fun adds_auth_signature() {
        val interceptor = createInterceptor()
        val argument = ArgumentCaptor.forClass(Request::class.java)

        interceptor.intercept(chain)

        verify(interceptor.dataSigner).sign(content)
        verify(chain).proceed(argument.capture())

        val request = argument.value
        assertThat(request.headers().get(SignedRequestInterceptor.CN_AUTH_SIG),
                equalTo(SIGNED_CONTENT))

    }

    @Test
    @Throws(IOException::class)
    fun proceeds_with_modified_request() {
        val interceptor = createInterceptor()
        val argument = ArgumentCaptor.forClass(Request::class.java)

        interceptor.intercept(chain)

        verify(chain).proceed(argument.capture())
        val request = argument.value
        assertNotNull(request)
    }

    companion object {
        private const val SIGNED_CONTENT = "3045022100aef1851655cd6e7ccc77afc3cd6c8f7a99de855571cea2dce9e94b17b392228f02206b37f35397018eb64d3f68995e6500d3c761c284d6a67a2509947da9137558d1"
        private const val SIGNED_TIME_STAMP = "3044022035d8f2b8e269cc84d49ee40fb4ccbc16bcc68d845894e37fcc08da1993bebfb202202f1cd4ef2d260644de71035b196de577f4fde8d57db770a2d8697268b71b48c6"
        private const val CN_WALLET_ID = "----wallet-id---"
        private const val CURRENT_TIME = "2018-05-09T23:45:22Z"
        private const val CN_USER_ID = "----USER-id---"
        private const val CN_AUTH_DEVICE_UUID = "----96a5d785-c449-4fc2-a92f-9c7884b29b31---"
    }
}