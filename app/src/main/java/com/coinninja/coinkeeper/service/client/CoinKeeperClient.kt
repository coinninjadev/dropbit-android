package com.coinninja.coinkeeper.service.client

import com.coinninja.coinkeeper.service.client.model.*
import com.google.gson.JsonObject
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface CoinKeeperClient {

    @get:GET("wallet/addresses")
    val walletAddresses: Call<List<CNWalletAddress>>

    @get:GET("wallet/address_requests/received")
    val allIncomingInvites: Call<List<ReceivedInvite>>

    @get:GET("wallet/address_requests/sent")
    val allSentInvites: Call<List<SentInvite>>

    @get:GET("user/identity")
    val identities: Call<List<CNUserIdentity>>

    // ADDRESSES
    @POST("addresses/query")
    fun getAddresses(@Body addressBlock: JsonObject, @Query("page") page: Int, @Query("perPage") limit: Int): Call<List<GsonAddress>>

    // TRANSACTIONS
    @POST("transactions/query")
    fun queryTransactions(@Body txids: JsonObject): Call<List<TransactionDetail>>

    @GET("transactions/{txid}/stats")
    fun getTransactionStats(@Path("txid") transactionId: String): Call<TransactionStats>

    // WALLET
    @GET("wallet")
    fun verifyWallet(): Call<CNWallet>

    @POST("wallet")
    fun createWallet(@Body walletRegistrationPayload: WalletRegistrationPayload): Call<CNWallet>

    @POST("wallet/addresses")
    fun addAddressToCNWallet(@Body address: JsonObject): Call<CNWalletAddress>

    @DELETE("wallet/addresses/{address}")
    fun removeAddress(@Path("address") address: String): Call<Map<String, String>>

    @POST("wallet/addresses/query")
    fun queryWalletAddress(@Body query: JsonObject): Call<List<AddressLookupResult>>

    // WALLET - BTC VIA PHONE NUMBER

    @POST("wallet/address_requests")
    fun inviteUser(@Body inviteUserPayload: InviteUserPayload): Call<InvitedContact>

    @PATCH("wallet/address_requests/{id}")
    fun patchInvite(@Path("id") id: String, @Body query: JsonObject): Call<SentInvite>

    @POST("wallet/addresses")
    fun sendAddress(@Body query: JsonObject): Call<CNWalletAddress>

    @PUT("wallet/reset")
    fun resetWallet(): Call<Void>

    // USER ACCOUNT
    @Deprecated("")
    @GET("user")
    fun verifyUserAccount(): Call<CNUserAccount>

    @PATCH("user")
    fun patchUserAccount(@Body cnUserPatch: CNUserPatch): Call<CNUserPatch>

    @Deprecated("")
    @POST("user")
    fun createUserAccount(@Body CNPhoneNumber: CNPhoneNumber): Call<CNUserAccount>

    @POST("user")
    fun createUserFrom(@Body identity: CNUserIdentity): Call<CNUserAccount>

    @POST("user/resend")
    fun resendVerification(@Body CNPhoneNumber: CNPhoneNumber): Call<CNUserAccount>

    @POST("user/query")
    fun queryUsers(@Body json: JsonObject): Call<Map<String, String>>

    @DELETE("user/identity/{identity_id}")
    fun deleteIdentity(@Path("identity_id") serverId: String): Call<Void>

    @GET("user/identity/{id}")
    fun getIdentity(@Path("id") id: String): Call<CNUserIdentity>

    @POST("user/identity")
    fun addIdentity(@Body identity: CNUserIdentity): Call<CNUserIdentity>

    @POST("user/verify")
    fun verifyIdentity(@Body identity: CNUserIdentity): Call<CNUserAccount>

    // PRICING
    @GET("wallet/check-in")
    fun checkIn(): Call<CurrentState>

    @GET("health")
    fun checkHealth(): Call<JsonObject>

    @GET("pricing/{txid}")
    fun getHistoricPrice(@Path("txid") txid: String): Call<CNPricing>

    // CN Global Messaging
    @POST("messages/query")
    fun getCNMessages(@Body elasticSearch: JsonObject): Call<List<CNGlobalMessage>>

    @POST("transaction/notification")
    fun postTransactionNotification(@Body sharedMemo: CNSharedMemo): Call<CNTransactionNotificationResponse>

    @GET("transaction/notification/{txid}")
    fun getTransactionNotification(@Path("txid") txid: String): Call<List<CNSharedMemo>>

    //CN Device
    @POST("devices")
    fun createCNDevice(@Body query: JsonObject): Call<CNDevice>

    @POST("devices/{deviceID}/endpoints")
    fun registerForPushEndpoint(@Path("deviceID") cnDeviceId: String, @Body body: JsonObject): Call<CNDeviceEndpoint>

    @GET("devices/{deviceID}/endpoints")
    fun fetchRemoteEndpointsFor(@Path("deviceID") cnDeviceId: String): Call<List<CNDeviceEndpoint>>

    @DELETE("devices/{deviceID}/endpoints/{endpointID}")
    fun unRegisterDeviceEndpoint(@Path("deviceID") cnDeviceId: String, @Path("endpointID") endpoint: String): Call<Map<String, String>>

    @GET("devices/{deviceID}/endpoints/{endpointID}/subscriptions")
    fun fetchDeviceEndpointSubscriptions(@Path("deviceID") cnDeviceId: String, @Path("endpointID") endpoint: String): Call<CNSubscriptionState>

    @POST("devices/{deviceID}/endpoints/{endpointID}/subscriptions")
    fun subscribeToTopics(@Path("deviceID") cnDeviceId: String,
                          @Path("endpointID") endpoint: String,
                          @Body cnTopicSubscription: CNTopicSubscription): Call<Void>


    @POST("wallet/subscribe")
    fun subscribeToWalletTopic(@Body body: JsonObject): Call<CNSubscription>

    @PUT("wallet/subscribe")
    fun updateWalletSubscription(@Body body: JsonObject): Call<CNSubscription>

    @DELETE("devices/{deviceID}/endpoints/{endpointID}/subscriptions/{topicID}")
    fun unsubscribeFromTopic(@Path("deviceID") devicesId: String, @Path("endpointID") deviceEndpoint: String, @Path("topicID") topicId: String): Call<Void>

    @GET("pricing/historic")
    fun loadHistoricPricing(@Query("period") period: String): Call<List<HistoricalPriceRecord>>

    @GET("news/feed/items")
    fun loadNews(@Query("count") count: Int): Call<MutableList<NewsArticle>>

    @Streaming
    @POST("broadcast")
    fun broadcastTransaction(@Body body: RequestBody): Call<Any>

    companion object {

        const val ADDRESSES_RESULT_LIMIT = 100
        const val ADDRESSES_TO_QUERY_AT_A_TIME = 25
        const val TRANSACTIONS_TO_QUERY_AT_A_TIME = 25
    }
}
