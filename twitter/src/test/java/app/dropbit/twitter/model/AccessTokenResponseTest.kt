package app.dropbit.twitter.model

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class AccessTokenResponseTest {
    @Test
    fun `from response string`() {
        val responseString = "oauth_token=authToken&oauth_token_secret=tokenSecret&user_id=11237&screen_name=screenName"
        val accessTokenResponse = AccessTokenResponse.from(responseString)
        assertThat(accessTokenResponse.oauthToken, equalTo("authToken"))
        assertThat(accessTokenResponse.oauthSecret, equalTo("tokenSecret"))
        assertThat(accessTokenResponse.userId, equalTo("11237"))
        assertThat(accessTokenResponse.screenName, equalTo("screenName"))
    }

    @Test
    fun `from empty response string`() {
        val responseString = ""
        val accessTokenResponse = AccessTokenResponse.from(responseString)
        assertThat(accessTokenResponse.oauthToken, equalTo(""))
        assertThat(accessTokenResponse.oauthSecret, equalTo(""))
        assertThat(accessTokenResponse.userId, equalTo(""))
        assertThat(accessTokenResponse.screenName, equalTo(""))
    }
}