package app.dropbit.twitter.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RequestTokenTest {
    @Test
    fun `inits from string`() {
        val requestToken: RequestToken = RequestToken.from("oauth_token=geXF2wAAAAAA-WA5AAABarxZfW8&" +
                "oauth_token_secret=f0i2IsdnFUOrC9B70hgOSw8i8sCNCFBu&" +
                "oauth_callback_confirmed=true")

        assertThat(requestToken.oAuthToken, equalTo("geXF2wAAAAAA-WA5AAABarxZfW8"))
        assertThat(requestToken.oAuthTokenSecret, equalTo("f0i2IsdnFUOrC9B70hgOSw8i8sCNCFBu"))
        assertThat(requestToken.oAuthCallbackConfirmed, equalTo(true))
    }

    @Test
    fun `inits from nothing`() {
        val requestToken: RequestToken = RequestToken.from("")

        assertThat(requestToken.oAuthToken, equalTo(""))
        assertThat(requestToken.oAuthTokenSecret, equalTo(""))
        assertThat(requestToken.oAuthCallbackConfirmed, equalTo(false))
    }

}