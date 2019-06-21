package app.dropbit.twitter.client

import app.dropbit.annotations.Mockable
import app.dropbit.twitter.BuildConfig
import app.dropbit.twitter.providers.TokenProvider
import java.net.URLEncoder


@Mockable
class Configuration constructor(val tokenProvider: TokenProvider) {
    companion object {
        private const val apiKey: String = BuildConfig.TWITTER_API_KEY
        private const val secret: String = BuildConfig.TWITTER_SECRET
        private const val callBackUrl: String = BuildConfig.TWITTER_CALLBACK_URI

    }

    fun oAuthSecret(): String = URLEncoder.encode(tokenProvider.oAuthTokenSecret(), "UTF-8")

    fun apiKey(): String = apiKey

    fun secretKey(): String = URLEncoder.encode(secret, "UTF-8")

    fun callBackRoute(): String = callBackUrl

    fun signingKey(): String = "${secretKey()}&${oAuthSecret()}"

    fun accessToken(): String = tokenProvider.accessToken()

    fun verifierToken(): String = tokenProvider.verifierToken()
}