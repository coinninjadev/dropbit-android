package app.dropbit.twitter

import android.net.Uri
import app.dropbit.annotations.Mockable
import app.dropbit.twitter.client.TwitterApiClient
import app.dropbit.twitter.model.AccessTokenResponse
import app.dropbit.twitter.model.FollowersResponse
import app.dropbit.twitter.model.RequestToken
import app.dropbit.twitter.model.TwitterUser
import app.dropbit.twitter.providers.TokenProvider
import kotlinx.coroutines.*
import retrofit2.Response

@Mockable
class Twitter internal constructor(internal val twitterApiClient: TwitterApiClient, internal val tokenProvider: TokenProvider) {

    val hasTwitterEnabled get() = !authToken.isNullOrEmpty() && !authSecret.isNullOrEmpty()

    val authSecret: String?
        get() {
            return tokenProvider.oAuthTokenSecret()
        }
    val authToken: String?
        get() {
            return tokenProvider.oAuthToken()
        }


    suspend fun requestToken(): RequestToken {
        var response: Response<String>? = null

        withContext(Dispatchers.IO) {
            tokenProvider.clear()
            response = twitterApiClient.requestToken()
        }

        if (response == null || !response?.isSuccessful!!) {
            return RequestToken()
        } else {
            val requestToken = RequestToken.from(response!!.body().toString())
            tokenProvider.saveRequestToken(requestToken)
            return requestToken
        }

    }

    suspend fun requestAccessToken(uri: Uri): TwitterUser? {
        if (!uri.queryParameterNames.containsAll(listOf(Constant.OAUTH_TOKEN_KEY, Constant.OAUTH_VERIFIER_KEY))) return null
        val requestAuthToken = uri.getQueryParameter(Constant.OAUTH_TOKEN_KEY)
        val authVerifier = uri.getQueryParameter(Constant.OAUTH_VERIFIER_KEY)
        if (requestAuthToken.isNullOrEmpty() || authVerifier.isNullOrEmpty()) return null

        tokenProvider.saveRequestAuthToken(requestAuthToken, authVerifier)

        val response = twitterApiClient.accessToken()
        if (response.isSuccessful) {
            val accessTokenResponse = AccessTokenResponse.from(response.body() as String)
            tokenProvider.saveAccessToken(accessTokenResponse)
            return me()
        }

        return null
    }

    fun me(): TwitterUser {
        val response: Response<TwitterUser> = twitterApiClient
                .getUser(tokenProvider.userId().toLong(),
                        tokenProvider.screenName(), true)
        if (response.isSuccessful) {
            return response.body() as TwitterUser
        } else {
            return TwitterUser()
        }
    }

    fun screenName(): String {
        return tokenProvider.screenName()
    }

    fun userId(): String {
        return tokenProvider.userId()
    }

    suspend fun getFollowing(): Response<FollowersResponse>? {
        return twitterApiClient.getFollowing()
    }

    suspend fun search(string: String): Response<List<TwitterUser>>? {
        return twitterApiClient.search(string)
    }

    suspend fun getUser(id: Long, handle: String): TwitterUser? {
        val response = withContext(Dispatchers.IO) {
            twitterApiClient.getUser(id, handle)
        }

        val twitterUser: TwitterUser? = withContext(Dispatchers.Default) {
            if (response.isSuccessful) {
                return@withContext response.body() as TwitterUser
            } else {
                return@withContext null
            }

        }

        return twitterUser
    }

    fun clear() {
        tokenProvider.clear()
    }
}
