package app.dropbit.twitter.client

import androidx.test.ext.junit.runners.AndroidJUnit4
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class TwitterApiClientTest {

    var server: MockWebServer = MockWebServer()

    @Before
    fun setUp() {
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `requests token`() {
        val twitter = createTwitter()
        val mockResponse = MockResponse()
        mockResponse.setBody("oauth_token=k8SQ-wAAAAAA-WA5AAABaru4kMc&oauth_token_secret=7gEoYAVDDahYRuvGUbO1o2mldLItCl63&oauth_callback_confirmed=true")
        server
        server.enqueue(mockResponse)

        twitter.requestToken()
        val takeRequest = server.takeRequest(100, TimeUnit.MILLISECONDS)

        assertThat(takeRequest.path, equalTo("/oauth/request_token"))
    }

    @Test
    fun `builds oauth authorize uri`() {
        assertThat(TwitterApiClient.authRoute("--oauth-token--").toString(),
                equalTo("https://api.twitter.com/oauth/authorize?oauth_token=--oauth-token--"))

    }

    fun createTwitter(): TwitterApiClient {
        val retrofit = Retrofit.Builder()
                .addConverterFactory(
                        GsonConverterFactory.create())
                .baseUrl(server.url("").toString())
                .build()

        return TwitterApiClient(retrofit.create(TwitterClient::class.java))
    }

}