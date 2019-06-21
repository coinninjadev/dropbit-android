package app.dropbit.twitter.client

import app.dropbit.twitter.Twitter
import app.dropbit.twitter.model.FollowersResponse
import app.dropbit.twitter.model.TwitterUser
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TwitterClient {

    @POST("oauth/request_token")
    fun requestToken(@Body requestBody: RequestBody): Call<String>

    @POST("oauth/access_token")
    fun accessToken(): Call<String>

    @GET("1.1/friends/list.json")
    fun getFollowing(): Call<FollowersResponse>

    @GET("1.1/users/show.json")
    fun showUser(@Query("user_id") userId: Long, @Query("screen_name") screenName: String, @Query("include_entities") withIdentity: Boolean): Call<TwitterUser>

    @GET("1.1/users/search.json")
    fun search(@Query("q") query: String): Call<List<TwitterUser>>
}