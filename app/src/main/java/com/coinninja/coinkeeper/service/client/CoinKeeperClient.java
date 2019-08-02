package com.coinninja.coinkeeper.service.client;

import com.coinninja.coinkeeper.service.client.model.AddressLookupResult;
import com.coinninja.coinkeeper.service.client.model.CNDevice;
import com.coinninja.coinkeeper.service.client.model.CNDeviceEndpoint;
import com.coinninja.coinkeeper.service.client.model.CNGlobalMessage;
import com.coinninja.coinkeeper.service.client.model.CNPhoneNumber;
import com.coinninja.coinkeeper.service.client.model.CNPricing;
import com.coinninja.coinkeeper.service.client.model.CNSharedMemo;
import com.coinninja.coinkeeper.service.client.model.CNSubscription;
import com.coinninja.coinkeeper.service.client.model.CNSubscriptionState;
import com.coinninja.coinkeeper.service.client.model.CNTopicSubscription;
import com.coinninja.coinkeeper.service.client.model.CNTransactionNotificationResponse;
import com.coinninja.coinkeeper.service.client.model.CNUserPatch;
import com.coinninja.coinkeeper.service.client.model.CNWallet;
import com.coinninja.coinkeeper.service.client.model.CNWalletAddress;
import com.coinninja.coinkeeper.service.client.model.GsonAddress;
import com.coinninja.coinkeeper.service.client.model.HistoricalPriceRecord;
import com.coinninja.coinkeeper.service.client.model.InviteUserPayload;
import com.coinninja.coinkeeper.service.client.model.InvitedContact;
import com.coinninja.coinkeeper.service.client.model.NewsArticle;
import com.coinninja.coinkeeper.service.client.model.ReceivedInvite;
import com.coinninja.coinkeeper.service.client.model.SentInvite;
import com.coinninja.coinkeeper.service.client.model.TransactionDetail;
import com.coinninja.coinkeeper.service.client.model.TransactionStats;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface CoinKeeperClient {

    int ADDRESSES_RESULT_LIMIT = 100;
    int ADDRESSES_TO_QUERY_AT_A_TIME = 25;
    int TRANSACTIONS_TO_QUERY_AT_A_TIME = 25;

    // ADDRESSES
    @POST("addresses/query")
    Call<List<GsonAddress>> getAddresses(@Body JsonObject addressBlock, @Query("page") int page, @Query("perPage") int limit);

    // TRANSACTIONS
    @POST("transactions/query")
    Call<List<TransactionDetail>> queryTransactions(@Body JsonObject txids);

    @GET("transactions/{txid}/stats")
    Call<TransactionStats> getTransactionStats(@Path("txid") String transactionId);

    // WALLET
    @GET("wallet")
    Call<CNWallet> verifyWallet();

    @POST("wallet")
    Call<CNWallet> createWallet(@Body JsonObject publicKeyHex);

    @GET("wallet/addresses")
    Call<List<CNWalletAddress>> getWalletAddresses();

    @POST("wallet/addresses")
    Call<CNWalletAddress> addAddressToCNWallet(@Body JsonObject address);

    @DELETE("wallet/addresses/{address}")
    Call<Map<String, String>> removeAddress(@Path("address") String address);

    @POST("wallet/addresses/query")
    Call<List<AddressLookupResult>> queryWalletAddress(@Body JsonObject query);

    // WALLET - BTC VIA PHONE NUMBER

    @POST("wallet/address_requests")
    Call<InvitedContact> inviteUser(@Body InviteUserPayload inviteUserPayload);

    @GET("wallet/address_requests/received")
    Call<List<ReceivedInvite>> getAllIncomingInvites();

    @GET("wallet/address_requests/sent")
    Call<List<SentInvite>> getAllSentInvites();

    @PATCH("wallet/address_requests/{id}")
    Call<SentInvite> patchInvite(@Path("id") String id, @Body JsonObject query);

    @POST("wallet/addresses")
    Call<CNWalletAddress> sendAddress(@Body JsonObject query);

    @PUT("wallet/reset")
    Call<Void> resetWallet();

    // USER ACCOUNT
    @Deprecated
    @GET("user")
    Call<CNUserAccount> verifyUserAccount();

    @PATCH("user")
    Call<CNUserPatch> patchUserAccount(@Body CNUserPatch cnUserPatch);

    @Deprecated
    @POST("user")
    Call<CNUserAccount> createUserAccount(@Body CNPhoneNumber CNPhoneNumber);

    @POST("user")
    Call<CNUserAccount> createUserFrom(@Body CNUserIdentity identity);

    @POST("user/resend")
    Call<CNUserAccount> resendVerification(@Body CNPhoneNumber CNPhoneNumber);

    @POST("user/query")
    Call<Map<String, String>> queryUsers(@Body JsonObject json);

    @DELETE("user/identity/{identity_id}")
    Call<Void> deleteIdentity(@Path("identity_id") String serverId);

    @GET("user/identity/{id}")
    Call<CNUserIdentity> getIdentity(@Path("id") String id);

    @GET("user/identity")
    Call<List<CNUserIdentity>> getIdentities();

    @POST("user/identity")
    Call<CNUserIdentity> addIdentity(@Body CNUserIdentity identity);

    @POST("user/verify")
    Call<CNUserAccount> verifyIdentity(@Body CNUserIdentity identity);

    // PRICING
    @GET("wallet/check-in")
    Call<CurrentState> checkIn();

    @GET("health")
    Call<JSONObject> checkHealth();

    @GET("pricing/{txid}")
    Call<CNPricing> getHistoricPrice(@Path("txid") String txid);

    // CN Global Messaging
    @POST("messages/query")
    Call<List<CNGlobalMessage>> getCNMessages(@Body JsonObject elasticSearch);

    @POST("transaction/notification")
    Call<CNTransactionNotificationResponse> postTransactionNotification(@Body CNSharedMemo sharedMemo);

    @GET("transaction/notification/{txid}")
    Call<List<CNSharedMemo>> getTransactionNotification(@Path("txid") String txid);

    //CN Device
    @POST("devices")
    Call<CNDevice> createCNDevice(@Body JsonObject query);

    @POST("devices/{deviceID}/endpoints")
    Call<CNDeviceEndpoint> registerForPushEndpoint(@Path("deviceID") String cnDeviceId, @Body JsonObject body);

    @GET("devices/{deviceID}/endpoints")
    Call<List<CNDeviceEndpoint>> fetchRemoteEndpointsFor(@Path("deviceID") String cnDeviceId);

    @DELETE("devices/{deviceID}/endpoints/{endpointID}")
    Call<Map<String, String>> unRegisterDeviceEndpoint(@Path("deviceID") String cnDeviceId, @Path("endpointID") String endpoint);

    @GET("devices/{deviceID}/endpoints/{endpointID}/subscriptions")
    Call<CNSubscriptionState> fetchDeviceEndpointSubscriptions(@Path("deviceID") String cnDeviceId, @Path("endpointID") String endpoint);

    @POST("devices/{deviceID}/endpoints/{endpointID}/subscriptions")
    Call<Void> subscribeToTopics(@Path("deviceID") String cnDeviceId,
                                 @Path("endpointID") String endpoint,
                                 @Body CNTopicSubscription cnTopicSubscription);


    @POST("wallet/subscribe")
    Call<CNSubscription> subscribeToWalletTopic(@Body JsonObject body);

    @PUT("wallet/subscribe")
    Call<CNSubscription> updateWalletSubscription(@Body JsonObject body);

    @DELETE("devices/{deviceID}/endpoints/{endpointID}/subscriptions/{topicID}")
    Call<Void> unsubscribeFromTopic(@Path("deviceID") String devicesId, @Path("endpointID") String deviceEndpoint, @Path("topicID") String topicId);

    @GET("pricing/historic")
    Call<List<HistoricalPriceRecord>> loadHistoricPricing(@Query("period") String period);

    @GET("news/feed/items")
    Call<List<NewsArticle>> loadNews(@Query("count") int count);

    @Streaming
    @POST("broadcast")
    Call<ResponseBody> broadcastTransaction(@Body RequestBody body);
}
