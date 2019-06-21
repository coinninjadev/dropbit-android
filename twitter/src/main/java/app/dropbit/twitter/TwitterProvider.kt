package app.dropbit.twitter

import android.content.Context
import app.dropbit.commons.util.DateUtil
import app.dropbit.commons.util.HmacSHA1Signer
import app.dropbit.twitter.client.Configuration
import app.dropbit.twitter.client.RequestInterceptor
import app.dropbit.twitter.client.TwitterApiClient
import app.dropbit.twitter.providers.TokenProvider
import app.dropbit.twitter.util.Authorization


class TwitterProvider {
    companion object {
        fun provide(context: Context): Twitter {
            val tokenProvider = TokenProvider(context.applicationContext)
            val authorization = Authorization(DateUtil(), Configuration(tokenProvider), HmacSHA1Signer())
            val twitterApiClient = TwitterApiClient.create(RequestInterceptor(authorization))
            return Twitter(twitterApiClient, tokenProvider)
        }
    }
}