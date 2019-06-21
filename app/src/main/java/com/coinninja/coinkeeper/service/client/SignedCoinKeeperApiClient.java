package com.coinninja.coinkeeper.service.client;

import androidx.annotation.NonNull;

import com.coinninja.coinkeeper.model.Contact;
import com.coinninja.coinkeeper.model.db.DropbitMeIdentity;
import com.coinninja.coinkeeper.service.client.model.CNDevice;
import com.coinninja.coinkeeper.service.client.model.CNPhoneNumber;
import com.coinninja.coinkeeper.service.client.model.CNSharedMemo;
import com.coinninja.coinkeeper.service.client.model.CNTopic;
import com.coinninja.coinkeeper.service.client.model.CNUserPatch;
import com.coinninja.coinkeeper.service.client.model.InviteUserPayload;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import retrofit2.Response;

import static com.coinninja.coinkeeper.util.DropbitIntents.CN_API_CREATE_DEVICE_APPLICATION_KEY;
import static com.coinninja.coinkeeper.util.DropbitIntents.CN_API_CREATE_DEVICE_APPLICATION_NAME;
import static com.coinninja.coinkeeper.util.DropbitIntents.CN_API_CREATE_DEVICE_PLATFORM_KEY;
import static com.coinninja.coinkeeper.util.DropbitIntents.CN_API_CREATE_DEVICE_UUID_KEY;

public class SignedCoinKeeperApiClient extends CoinKeeperApiClient {

    private final String fcmAppId;

    public SignedCoinKeeperApiClient(CoinKeeperClient client, String fcmAppId) {
        super(client);
        this.fcmAppId = fcmAppId;
    }

    public Response resendVerification(CNPhoneNumber CNPhoneNumber) {
        return executeCall(getClient().resendVerification(CNPhoneNumber));
    }

    public Response registerUserAccount(CNPhoneNumber CNPhoneNumber) {
        return executeCall(getClient().createUserAccount(CNPhoneNumber));
    }

    public Response registerWallet(String publicVerificationKey) {
        JsonObject json = new JsonObject();
        json.addProperty("public_key_string", publicVerificationKey);
        return executeCall(getClient().createWallet(json));
    }

    public Response fetchContactStatus(List<Contact> contacts) {
        JsonArray numbers = new JsonArray();

        for (Contact contact : contacts) {
            numbers.add(contact.getHash());
        }

        JsonObject query = createQuery("phone_number_hash", numbers);

        return executeCall(getClient().queryUsers(query));
    }

    public Response getCNWalletAddresses() {
        return executeCall(getClient().getWalletAddresses());
    }

    public Response addAddress(String address) {
        JsonObject json = new JsonObject();
        json.addProperty("address", address);
        return executeCall(getClient().addAddressToCNWallet(json));
    }

    public Response addAddress(String address, String publicKey) {
        JsonObject json = new JsonObject();
        json.addProperty("address", address);
        json.addProperty("address_pubkey", publicKey);
        return executeCall(getClient().addAddressToCNWallet(json));
    }

    public Response queryWalletAddress(String phoneHash) {
        JsonArray numbers = new JsonArray();
        numbers.add(phoneHash);
        JsonObject request = createQuery("phone_number_hash", numbers);
        JsonObject query = request.getAsJsonObject("query");
        query.addProperty("address_pubkey", true);
        return executeCall(getClient().queryWalletAddress(request));
    }

    public Response inviteUser(InviteUserPayload inviteUserPayload) {
       return executeCall(getClient().inviteUser(inviteUserPayload));
    }

    public Response getReceivedInvites() {
        return executeCall(getClient().getAllIncomingInvites());
    }

    public Response getSentInvites() {
        return executeCall(getClient().getAllSentInvites());
    }

    public Response sendAddressForInvite(String inviteId, String btcAddress, String addressPubKey) {
        JsonObject query = new JsonObject();

        query.addProperty("wallet_address_request_id", inviteId);
        query.addProperty("address", btcAddress);
        query.addProperty("address_pubkey", addressPubKey);

        return executeCall(getClient().sendAddress(query));
    }

    public Response patchSuppressionForWalletAddressRequest(String inviteId) {
        JsonObject query = new JsonObject();

        query.addProperty("suppress", false);
        return executeCall(getClient().patchInvite(inviteId, query));
    }

    public Response updateInviteStatusCompleted(String inviteId, String txID) {
        JsonObject query = new JsonObject();

        query.addProperty("status", "completed");
        query.addProperty("txid", txID);

        return executeCall(getClient().patchInvite(inviteId, query));
    }

    public Response updateInviteStatusCanceled(String inviteId) {
        JsonObject query = new JsonObject();

        query.addProperty("status", "canceled");
        return executeCall(getClient().patchInvite(inviteId, query));
    }

    public Response removeAddress(String address) {
        return executeCall(getClient().removeAddress(address));
    }

    public Response resetWallet() {
        return executeCall(getClient().resetWallet());
    }

    public Response createUpdateInviteStatusError(String inviteServerID, String errorMessage) {
        JsonObject query = new JsonObject();

        query.addProperty("status", "canceled");
        return createTeaPotErrorFor(getClient().patchInvite(inviteServerID, query), errorMessage);
    }

    public Response verifyWallet() {
        return executeCall(getClient().verifyWallet());
    }

    public Response verifyAccount() {
        return executeCall(getClient().verifyUserAccount());
    }

    public Response getCNMessages(CNElasticSearch elasticSearch) {
        return executeCall(getClient().getCNMessages(elasticSearch.toJson()));
    }

    public Response getTransactionNotification(String txid) {
        return executeCall(getClient().getTransactionNotification(txid));
    }

    public Response createCNDevice(String uuid) {
        String applicationName = CN_API_CREATE_DEVICE_APPLICATION_NAME;
        JsonObject query = new JsonObject();

        query.addProperty(CN_API_CREATE_DEVICE_APPLICATION_KEY, applicationName);
        query.addProperty(CN_API_CREATE_DEVICE_PLATFORM_KEY, CNDevice.DevicePlatform.ANDROID.toString());
        query.addProperty(CN_API_CREATE_DEVICE_UUID_KEY, uuid);

        return executeCall(getClient().createCNDevice(query));
    }

    public Response registerForPushEndpoint(String cnDeviceId, String token) {
        JsonObject queryBody = new JsonObject();

        queryBody.addProperty("application", fcmAppId);
        queryBody.addProperty("platform", "GCM");
        queryBody.addProperty("token", token);

        return executeCall(getClient().registerForPushEndpoint(cnDeviceId, queryBody));
    }

    public Response fetchRemoteEndpointsFor(String cnDeviceId) {

        return executeCall(getClient().fetchRemoteEndpointsFor(cnDeviceId));
    }

    public Response unRegisterDeviceEndpoint(String cnDeviceId, String endpoint) {
        return executeCall(getClient().unRegisterDeviceEndpoint(cnDeviceId, endpoint));
    }

    public Response fetchDeviceEndpointSubscriptions(String devicesId, String deviceEndpoint) {
        return executeCall(getClient().fetchDeviceEndpointSubscriptions(devicesId, deviceEndpoint));
    }

    public Response subscribeToTopics(String deviceId, String endpointId, List<CNTopic> topics) {
        JsonObject jsonObject = new JsonObject();

        JsonArray ids = new JsonArray();
        for (CNTopic cnTopic : topics) {
            ids.add(cnTopic.getId());
        }

        jsonObject.add("topic_ids", ids);

        return executeCall(getClient().subscribeToTopics(deviceId, endpointId, jsonObject));
    }

    public Response subscribeToWalletNotifications(String deviceEndpoint) {
        JsonObject jsonObject = buildWalletSubscriptionBody(deviceEndpoint);
        return executeCall(getClient().subscribeToWalletTopic(jsonObject));
    }

    public Response updateWalletSubscription(String deviceEndpoint) {
        JsonObject jsonObject = buildWalletSubscriptionBody(deviceEndpoint);
        return executeCall(getClient().updateWalletSubscription(jsonObject));
    }

    public Response postTransactionNotification(CNSharedMemo sharedMemo) {
        return executeCall(getClient().postTransactionNotification(sharedMemo));
    }

    public Response disableDropBitMeAccount() {
        return executeCall(getClient().patchUserAccount(new CNUserPatch(true)));
    }

    public Response enableDropBitMeAccount() {
        return executeCall(getClient().patchUserAccount(new CNUserPatch(false)));
    }

    @NotNull
    public Response getIdentity(@Nullable DropbitMeIdentity identity) {
        return executeCall(getClient().getIdentity(identity.getServerId()));
    }

    @NotNull
    public Response deleteIdentity(@Nullable DropbitMeIdentity identity) {
        return executeCall(getClient().deleteIdentity(identity.getServerId()));
    }

    public Response getIdentities() {
        return executeCall(getClient().getIdentities());
    }

    @NotNull
    public Response addIdentity(@Nullable CNUserIdentity identity) {
        return executeCall(getClient().addIdentity(identity));
    }

    @NotNull
    public Response verifyIdentity(@Nullable CNUserIdentity identity) {
        return executeCall(getClient().verifyIdentity(identity));
    }

    @NotNull
    public Response createUserFromIdentity(@Nullable CNUserIdentity identity) {
        return executeCall(getClient().createUserFrom(identity));
    }

    @NonNull
    private JsonObject buildWalletSubscriptionBody(String deviceEndpoint) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("device_endpoint_id", deviceEndpoint);
        return jsonObject;
    }

}
