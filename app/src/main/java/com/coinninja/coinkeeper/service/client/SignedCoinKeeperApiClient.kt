package com.coinninja.coinkeeper.service.client

import app.coinninja.cn.libbitcoin.model.Transaction
import app.dropbit.annotations.Mockable
import app.dropbit.commons.util.removeRange
import com.coinninja.coinkeeper.bitcoin.BroadcastProvider
import com.coinninja.coinkeeper.bitcoin.BroadcastingClient
import com.coinninja.coinkeeper.model.Contact
import com.coinninja.coinkeeper.model.db.DropbitMeIdentity
import com.coinninja.coinkeeper.service.client.model.*
import com.coinninja.coinkeeper.ui.market.Granularity
import com.coinninja.coinkeeper.util.DropbitIntents.*
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Response

@Mockable
@Suppress("UNCHECKED_CAST")
class SignedCoinKeeperApiClient(
        client: CoinKeeperClient, private val fcmAppId: String
) : CoinKeeperApiClient(client), BroadcastingClient {

    val cnWalletAddresses: Response<List<CNWalletAddress>>
        get() = executeCall(client.walletAddresses)

    val receivedInvites: Response<List<ReceivedInvite>>
        get() = executeCall(client.allIncomingInvites)

    val sentInvites: Response<List<SentInvite>>
        get() = executeCall(client.allSentInvites)

    val identities: Response<List<CNUserIdentity>>
        get() = executeCall(client.identities)

    fun resendVerification(CNPhoneNumber: CNPhoneNumber): Response<CNUserAccount> {
        return executeCall(client.resendVerification(CNPhoneNumber))
    }

    fun registerUserAccount(CNPhoneNumber: CNPhoneNumber): Response<CNUserAccount> {
        return executeCall(client.createUserAccount(CNPhoneNumber))
    }

    fun registerWallet(walletRegistrationPayload: WalletRegistrationPayload): Response<CNWallet> {
        return executeCall(client.createWallet(walletRegistrationPayload))
    }

    fun fetchContactStatus(contacts: List<Contact>): Response<Map<String, String>> {
        val numbers = JsonArray()

        for (contact in contacts) {
            numbers.add(contact.hash)
        }

        val query = createQuery("phone_number_hash", numbers)

        return executeCall(client.queryUsers(query))
    }

    fun addAddress(address: String, publicKey: String): Response<CNWalletAddress> {
        val json = JsonObject()
        json.addProperty("address", address)
        json.addProperty("address_pubkey", publicKey)
        return executeCall(client.addAddressToCNWallet(json))
    }

    fun queryWalletAddress(phoneHash: String): Response<List<AddressLookupResult>> {
        val numbers = JsonArray()
        numbers.add(phoneHash)
        val request = createQuery("phone_number_hash", numbers)
        val query = request.getAsJsonObject("query")
        query.addProperty("address_pubkey", true)
        return executeCall(client.queryWalletAddress(request))
    }

    fun inviteUser(inviteUserPayload: InviteUserPayload): Response<InvitedContact> {
        return executeCall(client.inviteUser(inviteUserPayload))
    }

    fun sendAddressForInvite(inviteId: String, btcAddress: String, addressPubKey: String): Response<CNWalletAddress> {
        val query = JsonObject()

        query.addProperty("wallet_address_request_id", inviteId)
        query.addProperty("address", btcAddress)
        query.addProperty("address_pubkey", addressPubKey)

        return executeCall(client.sendAddress(query))
    }

    fun patchSuppressionForWalletAddressRequest(inviteId: String): Response<SentInvite> {
        val query = JsonObject()

        query.addProperty("suppress", false)
        return executeCall(client.patchInvite(inviteId, query))
    }

    fun updateInviteStatusCompleted(inviteId: String, txID: String): Response<SentInvite> {
        val query = JsonObject()

        query.addProperty("status", "completed")
        query.addProperty("txid", txID)

        return executeCall(client.patchInvite(inviteId, query))
    }

    fun updateInviteStatusCanceled(inviteId: String): Response<SentInvite> {
        val query = JsonObject()

        query.addProperty("status", "canceled")
        return executeCall(client.patchInvite(inviteId, query))
    }

    fun removeAddress(address: String): Response<Map<String, String>> {
        return executeCall(client.removeAddress(address))
    }

    fun resetWallet(): Response<Void> {
        return executeCall(client.resetWallet())
    }

    fun createUpdateInviteStatusError(inviteServerID: String, errorMessage: String): Response<SentInvite> {
        val query = JsonObject()

        query.addProperty("status", "canceled")
        return createTeaPotErrorFor(client.patchInvite(inviteServerID, query), errorMessage)
    }

    fun verifyWallet(): Response<CNWallet> {
        return executeCall(client.verifyWallet())
    }

    fun disableWallet(): Response<CNWallet> {
        return executeCall(client.disableWallet(DisableWalletRequest()))
    }

    fun replaceWalletWith(replaceWalletRequest: ReplaceWalletRequest): Response<CNWallet> {
        return executeCall(client.replaceWalletWith(replaceWalletRequest))
    }

    fun verifyAccount(): Response<CNUserAccount> {
        return executeCall(client.verifyUserAccount())
    }

    fun getCNMessages(elasticSearch: CNElasticSearch): Response<List<CNGlobalMessage>> {
        return executeCall(client.getCNMessages(elasticSearch.toJson()))
    }

    fun getTransactionNotification(txid: String): Response<List<CNSharedMemo>> {
        return executeCall(client.getTransactionNotification(txid))
    }

    fun createCNDevice(uuid: String): Response<CNDevice> {
        val applicationName = CN_API_CREATE_DEVICE_APPLICATION_NAME
        val query = JsonObject()

        query.addProperty(CN_API_CREATE_DEVICE_APPLICATION_KEY, applicationName)
        query.addProperty(CN_API_CREATE_DEVICE_PLATFORM_KEY, CNDevice.DevicePlatform.ANDROID.toString())
        query.addProperty(CN_API_CREATE_DEVICE_UUID_KEY, uuid)

        return executeCall(client.createCNDevice(query))
    }

    fun registerForPushEndpoint(cnDeviceId: String, token: String): Response<CNDeviceEndpoint> {
        val queryBody = JsonObject()

        queryBody.addProperty("application", fcmAppId)
        queryBody.addProperty("platform", "GCM")
        queryBody.addProperty("token", token)

        return executeCall(client.registerForPushEndpoint(cnDeviceId, queryBody))
    }

    fun fetchRemoteEndpointsFor(cnDeviceId: String): Response<List<CNDeviceEndpoint>> {

        return executeCall(client.fetchRemoteEndpointsFor(cnDeviceId))
    }

    fun unRegisterDeviceEndpoint(cnDeviceId: String, endpoint: String): Response<Map<String, String>> {
        return executeCall(client.unRegisterDeviceEndpoint(cnDeviceId, endpoint))
    }

    fun fetchDeviceEndpointSubscriptions(devicesId: String, deviceEndpoint: String): Response<CNSubscriptionState> {
        return executeCall(client.fetchDeviceEndpointSubscriptions(devicesId, deviceEndpoint))
    }

    fun subscribeToTopics(deviceId: String, endpointId: String, topics: CNTopicSubscription): Response<Void> {
        return executeCall(client.subscribeToTopics(deviceId, endpointId, topics))
    }

    fun subscribeToWalletNotifications(deviceEndpoint: String): Response<CNSubscription> {
        val jsonObject = buildWalletSubscriptionBody(deviceEndpoint)
        return executeCall(client.subscribeToWalletTopic(jsonObject))
    }

    fun updateWalletSubscription(deviceEndpoint: String): Response<CNSubscription> {
        val jsonObject = buildWalletSubscriptionBody(deviceEndpoint)
        return executeCall(client.updateWalletSubscription(jsonObject))
    }

    fun postTransactionNotification(sharedMemo: CNSharedMemo): Response<CNTransactionNotificationResponse> {
        return executeCall(client.postTransactionNotification(sharedMemo))
    }

    fun disableDropBitMeAccount(): Response<CNUserPatch> {
        return executeCall(client.patchUserAccount(CNUserPatch(true)))
    }

    fun enableDropBitMeAccount(): Response<CNUserPatch> {
        return executeCall(client.patchUserAccount(CNUserPatch(false)))
    }

    fun getIdentity(identity: DropbitMeIdentity?): Response<*> {
        return executeCall(client.getIdentity(identity!!.serverId))
    }

    fun deleteIdentity(identity: DropbitMeIdentity?): Response<Void> {
        return executeCall(client.deleteIdentity(identity!!.serverId))
    }

    fun addIdentity(identity: CNUserIdentity): Response<CNUserIdentity> {
        return executeCall(client.addIdentity(identity))
    }

    fun verifyIdentity(identity: CNUserIdentity): Response<CNUserAccount> {
        return executeCall(client.verifyIdentity(identity))
    }

    fun createUserFromIdentity(identity: CNUserIdentity): Response<CNUserAccount> {
        return executeCall(client.createUserFrom(identity))
    }

    fun unsubscribeFromTopic(devicesId: String, deviceEndpoint: String, topicId: String): Response<Void> {
        return executeCall(client.unsubscribeFromTopic(devicesId, deviceEndpoint, topicId))
    }

    fun loadHistoricPricing(granularity: Granularity): Response<List<HistoricalPriceRecord>> {
        return executeCall(client.loadHistoricPricing(granularity.value))
    }

    fun loadNews(numArticles: Int, offset: Int): Response<MutableList<NewsArticle>> {
        val count = if (offset > 0) numArticles + offset else numArticles
        var response: Response<MutableList<NewsArticle>> = executeCall(client.loadNews(count))
        if (response.code() == 200 && offset > 0) {
            val fetched = response.body() as MutableList<NewsArticle>
            if (fetched.size == count) {
                fetched.removeRange(0 until offset)
                response = Response.success(fetched)
            }
        }

        return response
    }

    override fun broadcastTransaction(transaction: Transaction): Response<Any> {
        val requestBody = RequestBody.create(MediaType.parse("text/plain"), transaction.encodedTransaction)
        return executeCall(client.broadcastTransaction(requestBody))
    }

    override fun broadcastProvider(): BroadcastProvider {
        return BroadcastProvider.COIN_NINJA
    }

    private fun buildWalletSubscriptionBody(deviceEndpoint: String): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty("device_endpoint_id", deviceEndpoint)
        return jsonObject
    }


}
