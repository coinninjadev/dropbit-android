package app.dropbit.twitter.client

import app.dropbit.twitter.providers.TokenProvider
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.Mockito.mock

class ConfigurationTest {

    @Test
    fun `only signing key and a &`() {
        val tokenProvider = mock(TokenProvider::class.java)
        whenever(tokenProvider.oAuthTokenSecret()).thenReturn("")
        val configuration = Configuration(tokenProvider)
        assertThat(configuration.signingKey(), equalTo(configuration.secretKey() + "&"))
    }

    @Test
    fun `only signing key and a & followed by users secret`() {
        val oAuthTokenSecret = "super00secret"
        val tokenProvider = mock(TokenProvider::class.java)
        whenever(tokenProvider.oAuthTokenSecret()).thenReturn(oAuthTokenSecret)
        val configuration = Configuration(tokenProvider)
        assertThat(configuration.signingKey(), equalTo(configuration.secretKey() + "&" + oAuthTokenSecret))
    }

    @Test
    fun `proxies access token`() {
        val tokenProvider = mock(TokenProvider::class.java)
        whenever(tokenProvider.accessToken()).thenReturn("-access-token").thenReturn("")
        val configuration = Configuration(tokenProvider)

        assertThat(configuration.accessToken(), equalTo("-access-token"))
        assertThat(configuration.accessToken(), equalTo(""))
    }

    @Test
    fun `proxies verifier token`() {
        val tokenProvider = mock(TokenProvider::class.java)
        whenever(tokenProvider.verifierToken()).thenReturn("-verifier-token").thenReturn("")
        val configuration = Configuration(tokenProvider)

        assertThat(configuration.verifierToken(), equalTo("-verifier-token"))
        assertThat(configuration.verifierToken(), equalTo(""))
    }
}