package app.dropbit.twitter.client

import android.net.Uri
import app.dropbit.annotations.Mockable
import app.dropbit.twitter.Constant
import app.dropbit.twitter.model.FollowersResponse
import app.dropbit.twitter.model.TwitterUser
import okhttp3.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.IOException

@Mockable
class TwitterApiClient constructor(val client: TwitterClient) {

    fun requestToken(): Response<String> {
        return executeCall(client.requestToken(RequestBody.create(MediaType.parse("text/plain"), " ")))
    }

    fun accessToken(): Response<String> {
        return executeCall(client.accessToken())
    }

    fun getUser(userId: Long, displayName: String, withIdentity: Boolean = true): Response<TwitterUser> {
        return executeCall(client.showUser(userId, displayName, withIdentity))
    }

    protected fun <T> executeCall(call: Call<T>): Response<T> {
        val response: Response<T>

        response = try {
            call.execute()
        } catch (e: IOException) {
            e.printStackTrace()
            createTeaPotErrorFor(call, e.message)
        }

        return response
    }

    protected fun <T> createTeaPotErrorFor(call: Call<T>, message: String?): Response<T> {
        return Response.error(okhttp3.ResponseBody.create(null, ""), okhttp3.Response.Builder()
                .code(errorCode)
                .message(if (message.isNullOrEmpty()) "" else message)
                .protocol(Protocol.HTTP_1_1)
                .request(Request.Builder().url(call.request().url()).build())
                .build())

    }

    fun getFollowing(): Response<FollowersResponse>? {
        return executeCall(client.getFollowing())
    }

    fun search(string: String): Response<List<TwitterUser>>? {
        return executeCall(client.search(string))
    }

    companion object {
        private const val route = "https://api.twitter.com/"
        private const val errorCode: Int = 418

        fun authRoute(oAuthToken: String): Uri = Uri.parse(route).buildUpon()
                .appendPath("oauth")
                .appendPath("authorize")
                .appendQueryParameter(Constant.OAUTH_TOKEN_KEY, oAuthToken)
                .build()


        fun create(requestInterceptor: RequestInterceptor): TwitterApiClient {
            val retrofit = Retrofit.Builder()
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(
                            GsonConverterFactory.create())
                    .client(OkHttpClient.Builder()
                            .addInterceptor(requestInterceptor)
                            .build()
                    )
                    .baseUrl(route)
                    .build()

            return TwitterApiClient(retrofit.create(TwitterClient::class.java))
        }
    }

}