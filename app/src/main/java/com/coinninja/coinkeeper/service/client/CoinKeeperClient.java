package com.coinninja.coinkeeper.service.client;

import com.coinninja.coinkeeper.service.client.model.AddressLookupResult;
import com.coinninja.coinkeeper.service.client.model.CNDevice;
import com.coinninja.coinkeeper.service.client.model.CNDeviceEndpoint;
import com.coinninja.coinkeeper.service.client.model.CNGlobalMessage;
import com.coinninja.coinkeeper.service.client.model.CNPricing;
import com.coinninja.coinkeeper.service.client.model.CNSharedMemo;
import com.coinninja.coinkeeper.service.client.model.CNSubscription;
import com.coinninja.coinkeeper.service.client.model.CNSubscriptionState;
import com.coinninja.coinkeeper.service.client.model.CNTransactionNotification;
import com.coinninja.coinkeeper.service.client.model.CNWallet;
import com.coinninja.coinkeeper.service.client.model.CNWalletAddress;
import com.coinninja.coinkeeper.service.client.model.GsonAddress;
import com.coinninja.coinkeeper.service.client.model.InvitedContact;
import com.coinninja.coinkeeper.service.client.model.CNPhoneNumber;
import com.coinninja.coinkeeper.service.client.model.ReceivedInvite;
import com.coinninja.coinkeeper.service.client.model.SentInvite;
import com.coinninja.coinkeeper.service.client.model.TransactionDetail;
import com.coinninja.coinkeeper.service.client.model.TransactionStats;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

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
    Call<InvitedContact> invitePhoneNumber(@Body JsonObject query);

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
    @GET("user")
    Call<CNUserAccount> verifiyUserAccount();

    @POST("user")
    Call<CNUserAccount> createUserAccount(@Body CNPhoneNumber CNPhoneNumber);

    @POST("user/resend")
    Call<CNUserAccount> resendVerification(@Body CNPhoneNumber CNPhoneNumber);

    @POST("user/query")
    Call<Map<String, String>> queryUsers(@Body JsonObject json);

    @POST("user/verify")
    Call<CNUserAccount> confirmAccount(@Body JsonObject content);

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
    Call<CNTransactionNotification> postTransactionNotification(@Body JsonObject transactionNotification);

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
                                 @Body JsonObject json);


    @POST("wallet/subscribe")
    Call<CNSubscription> subscribeToWalletTopic(@Body JsonObject body);

    @PUT("wallet/subscribe")
    Call<CNSubscription> updateWalletSubscription(@Body JsonObject body);

    @GET("transaction/notification/{txid}")
    Call<List<CNSharedMemo>> getTransactionNotification(@Path("txid") String txid);
}
