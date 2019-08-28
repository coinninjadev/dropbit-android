package com.coinninja.coinkeeper.service.client

import com.coinninja.coinkeeper.cn.wallet.DataSigner
import com.coinninja.coinkeeper.model.Contact
import com.coinninja.coinkeeper.model.db.DropbitMeIdentity
import com.coinninja.coinkeeper.service.client.model.*
import com.coinninja.coinkeeper.ui.market.Granularity
import com.coinninja.coinkeeper.util.DropbitIntents.*
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.nhaarman.mockitokotlin2.*
import junit.framework.Assert.assertTrue
import junit.framework.TestCase.assertNotNull
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

class SignedCoinKeeperApiClientTest {
    private var dataSigner: DataSigner = mock()
    private var client: CoinKeeperClient = mock()
    private var webServer: MockWebServer = MockWebServer()
    private lateinit var signedCoinKeeperApiClient: SignedCoinKeeperApiClient

    @Before
    fun setUp() {
        signedCoinKeeperApiClient = createClient(webServer.url("").toString())

    }

    @After
    fun tearDown() {
        webServer.shutdown()
    }

    @Test
    fun verifies_account() {
        val json = "{\n" +
                "  \"id\": \"ad983e63-526d-4679-a682-c4ab052b20e1\",\n" +
                "  \"created_at\": 1531921356,\n" +
                "  \"updated_at\": 1531921356,\n" +
                "  \"status\": \"pending-verification\",\n" +
                "  \"private\": true,\n" +
                "  \"identities\": [\n" +
                "    {\n" +
                "      \"id\": \"ad983e63-526d-4679-a682-c4ab052b20e1\",\n" +
                "      \"created_at\": 1531921356,\n" +
                "      \"updated_at\": 1531921356,\n" +
                "      \"type\": \"phone\",\n" +
                "      \"identity\": \"13305551212\",\n" +
                "      \"hash\": \"498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49\",\n" +
                "      \"status\": \"pending-verification\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
                "}"

        webServer.enqueue(MockResponse().setResponseCode(200).setBody(json))

        val response = signedCoinKeeperApiClient.verifyAccount()
        val cnUserAccount: CNUserAccount = response.body() as CNUserAccount
        assertThat<String>(cnUserAccount.id, equalTo("ad983e63-526d-4679-a682-c4ab052b20e1"))
        assertThat(cnUserAccount.identities.size, equalTo(1))
        assertThat(cnUserAccount.identities[0].identity, equalTo("13305551212"))
        assertThat(cnUserAccount.identities[0].type, equalTo("phone"))
    }

    @Test
    fun verifies_wallet() {
        val json = "{\n" +
                "\"id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\",\n" +
                "\"public_key_string\": \"02262233847a69026f8f3ae027af347f2501adf008fe4f6087d31a1d975fd41473\",\n" +
                "\"created_at\": 1531921356,\n" +
                "\"updated_at\": 1531921356,\n" +
                "\"user_id\": \"ad983e63-526d-4679-a682-c4ab052b20e1\"\n" +
                "}"

        webServer.enqueue(MockResponse().setResponseCode(200).setBody(json))

        val response = signedCoinKeeperApiClient.verifyWallet()
        val cnWallet = response.body() as CNWallet
        assertThat(cnWallet.publicKeyString, equalTo("02262233847a69026f8f3ae027af347f2501adf008fe4f6087d31a1d975fd41473"))
    }

    @Test
    fun deletes_users_wallet() {
        webServer.enqueue(MockResponse().setResponseCode(204).setBody(""))

        assertTrue(signedCoinKeeperApiClient.resetWallet().isSuccessful)
    }

    @Test
    fun querys_for_contacts_address() {
        val json = "{" +
                "  \"query\": {" +
                "    \"terms\": {" +
                "      \"phone_number_hash\": [" +
                "        \"9906c77c0aa1f6e4760a68719c79bdf605d1f7819a15d06bc6dfc216c047339f\"" +
                "      ]" +
                "    }," +
                "    \"address_pubkey\": true" +
                "  }" +
                "}"


        val call = mock<Call<List<AddressLookupResult>>>()
        whenever(call.execute()).thenReturn(Response.success(mock()))
        whenever(client.queryWalletAddress(any())).thenReturn(call)
        val captor = argumentCaptor<JsonObject>()
        val apiClient = SignedCoinKeeperApiClient(client, FCM_APP_ID)
        val phoneHash = "9906c77c0aa1f6e4760a68719c79bdf605d1f7819a15d06bc6dfc216c047339f"
        apiClient.queryWalletAddress(phoneHash)

        verify(client).queryWalletAddress(captor.capture())

        assertThat(captor.firstValue.toString(), equalTo(json.replace("\\s".toRegex(), "")))
    }

    @Test
    fun deserializes_results_for_contacts_address() {
        val json = "[\n" +
                "  {\n" +
                "    \"phone_number_hash\": \"498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49\",\n" +
                "    \"address\": \"1JbJbAkCXtxpko39nby44hpPenpC1xKGYw\",\n" +
                "    \"address_pubkey\": \"04cf39eab1213ad4a94e755fadaac4c8f2a256d7fa6b4044c7980113f7df60e24d5c1156b794d46652de2493013c6495469fbbac39d8c86495f1eebd65c7a6bddc\"\n" +
                "  }\n" +
                "]"

        webServer.enqueue(MockResponse().setResponseCode(200).setBody(json))
        val response = signedCoinKeeperApiClient.queryWalletAddress("--phone-hash--")
        val results = response.body() as List<AddressLookupResult>
        val (phoneNumberHash, address, addressPubKey) = results[0]

        assertThat<String>(address, equalTo("1JbJbAkCXtxpko39nby44hpPenpC1xKGYw"))
        assertThat<String>(phoneNumberHash, equalTo("498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49"))
        assertThat<String>(addressPubKey, equalTo("04cf39eab1213ad4a94e755fadaac4c8f2a256d7fa6b4044c7980113f7df60e24d5c1156b794d46652de2493013c6495469fbbac39d8c86495f1eebd65c7a6bddc"))
    }

    @Test
    fun find_coin_ninja_users_for_contacts() {
        val captor = argumentCaptor<JsonObject>()
        val call = mock<Call<Map<String, String>>>()
        whenever(call.execute()).thenReturn(Response.success(mock()))
        whenever(client.queryUsers(any())).thenReturn(call)
        val apiClient = SignedCoinKeeperApiClient(client, FCM_APP_ID)
        val contact1Hash = "df7c846cd38e5af8c94985e4ad1a699a08d5dfe11afc12b3a5aa67d3cd604a16"
        val contact2Hash = "760bb6b0bb25ee44e91e5967033899dee64a7141224792ab959977a8f1e6acda"
        val contact1: Contact = mock()
        val contact2: Contact = mock()
        whenever(contact1.hash).thenReturn(contact1Hash)
        whenever(contact2.hash).thenReturn(contact2Hash)
        val contacts = ArrayList<Contact>()
        contacts.add(contact1)
        contacts.add(contact2)

        val json = "{" +
                "  \"query\": {" +
                "    \"terms\": {" +
                "      \"phone_number_hash\": [" +
                "        \"" + contact1Hash + "\"," +
                "        \"" + contact2Hash + "\"" +
                "      ]" +
                "    }" +
                "  }" +
                "}"

        apiClient.fetchContactStatus(contacts)
        verify(client).queryUsers(captor.capture())

        assertThat(captor.firstValue.toString(), equalTo(json.replace("\\s".toRegex(), "")))
    }

    @Test
    fun sends_address_to_cn_for_contact_payments() {
        val json = "{\n" +
                "  \"id\": \"6d1d7318-81b9-492c-b3f3-9d1b24f91d14\",\n" +
                "    \"created_at\": 1531921356,\n" +
                "    \"updated_at\": 1531921357,\n" +
                "  \"address\": \"1JbJbAkCXtxpko39nby44hpPenpC1xKGYw\",\n" +
                "  \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
                "}"
        webServer.enqueue(MockResponse().setResponseCode(201).setBody(json))
        val response = signedCoinKeeperApiClient.addAddress("1JbJbAkCXtxpko39nby44hpPenpC1xKGYw", "--pub-key--")
        assertThat(response.code(), equalTo(201))
        val walletAddress = response.body() as CNWalletAddress

        assertThat(walletAddress.address, equalTo("1JbJbAkCXtxpko39nby44hpPenpC1xKGYw"))
        assertThat(walletAddress.id, equalTo("6d1d7318-81b9-492c-b3f3-9d1b24f91d14"))
        assertThat(walletAddress.createdAt, equalTo(1531921356000L))
        assertThat(walletAddress.updateAt, equalTo(1531921357000L))
        assertThat(walletAddress.walletId, equalTo("f8e8c20e-ba44-4bac-9a96-44f3b7ae955d"))
    }

    @Test
    fun posts_phone_code_for_account_confirmation() {
        val code = "012045"
        val json = "{\n" +
                "  \"id\": \"ad983e63-526d-4679-a682-c4ab052b20e1\",\n" +
                "  \"created_at\": 1531921356,\n" +
                "  \"updated_at\": 1531921356,\n" +
                "  \"status\": \"verified\",\n" +
                "  \"private\": true,\n" +
                "  \"identities\": [\n" +
                "    {\n" +
                "      \"id\": \"ad983e63-526d-4679-a682-c4ab052b20e1\",\n" +
                "      \"created_at\": 1531921356,\n" +
                "      \"updated_at\": 1531921356,\n" +
                "      \"type\": \"phone\",\n" +
                "      \"identity\": \"13305551212\",\n" +
                "      \"hash\": \"498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49\",\n" +
                "      \"status\": \"verified\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
                "}"

        webServer.enqueue(MockResponse().setResponseCode(200).setBody(json))
        val identity = CNUserIdentity()
        identity.code = code
        identity.type = "phone"
        identity.identity = "13305551111"
        val response = signedCoinKeeperApiClient.verifyIdentity(identity)
        assertThat(response.code(), equalTo(200))
        val account = response.body() as CNUserAccount
        assertThat<String>(account.status, equalTo("verified"))
        assertThat(account.identities.size, equalTo(1))
        assertThat<String>(account.identities[0].identity, equalTo("13305551212"))
        assertThat<String>(account.identities[0].type, equalTo("phone"))
    }

    @Test
    fun posts_phone_code_for_account_confirmation__route() {
        val call = mock<Call<CNUserAccount>>()
        whenever(call.execute()).thenReturn(Response.success(mock()))
        whenever(client.resendVerification(any())).thenReturn(call)
        val CNPhoneNumber = CNPhoneNumber()
        CNPhoneNumber.countryCode = 1
        CNPhoneNumber.phoneNumber = "330-555-5555"
        val apiClient = SignedCoinKeeperApiClient(client, FCM_APP_ID)

        apiClient.resendVerification(CNPhoneNumber)

        verify(client).resendVerification(CNPhoneNumber)
    }

    @Test
    fun posts_phonenumber_to_api_for_verification() {
        val json = "{\n" +
                "  \"id\": \"ad983e63-526d-4679-a682-c4ab052b20e1\",\n" +
                "  \"phone_number_hash\": \"498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49\",\n" +
                "  \"created_at\": 1531921356,\n" +
                "  \"updated_at\": 1531921356,\n" +
                "  \"status\": \"pending-verification\",\n" +
                "  \"verification_ttl\": 1531921356,\n" +
                "  \"verified_at\": 1531921356,\n" +
                "  \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
                "}"
        val CNPhoneNumber = CNPhoneNumber()
        webServer.enqueue(MockResponse().setResponseCode(201).setBody(json))
        val response = signedCoinKeeperApiClient.registerUserAccount(CNPhoneNumber)
        assertThat(response.code(), equalTo(201))
    }

    @Test
    fun posts_pubKey_to_CN_for_account_creation() {
        val call = mock<Call<CNWallet>>()
        whenever(call.execute()).thenReturn(Response.success(mock()))
        whenever(client.createWallet(any())).thenReturn(call)
        whenever(dataSigner.coinNinjaVerificationKey).thenReturn("---pub--key---")
        val apiClient = SignedCoinKeeperApiClient(client, FCM_APP_ID)

        apiClient.registerWallet(dataSigner.coinNinjaVerificationKey)

        val json = JsonObject()
        json.addProperty("public_key_string", "---pub--key---")
        verify(client).createWallet(json)
    }

    @Test
    fun creates_cnWallet() {
        val json = "{\n" +
                "  \"id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\",\n" +
                "  \"public_key_string\": \"02262233847a69026f8f3ae027af347f2501adf008fe4f6087d31a1d975fd41473\",\n" +
                "  \"created_at\": 1531921356,\n" +
                "  \"updated_at\": 1531921356\n" +
                "}"
        webServer.enqueue(MockResponse().setResponseCode(200).setBody(json))
        val response = signedCoinKeeperApiClient.registerWallet("---pub--key---")
        val cnWallet = response.body() as CNWallet
        assertThat(response.code(), equalTo(200))
        assertThat(cnWallet.id, equalTo("f8e8c20e-ba44-4bac-9a96-44f3b7ae955d"))
    }

    @Test
    @Throws(InterruptedException::class)
    fun invites_phone_based_user_identity() {
        val json = "{\n" +
                "  \"id\": \"a1bb1d88-bfc8-4085-8966-e0062278237c\",\n" +
                "  \"created_at\": 1531921356,\n" +
                "  \"updated_at\": 1531921356,\n" +
                "  \"address\": \"1JbJbAkCXtxpko39nby44hpPenpC1xKGYw\",\n" +
                "  \"metadata\": {\n" +
                "    \"amount\": {\n" +
                "      \"btc\": 120000000,\n" +
                "      \"usd\": 8292280\n" +
                "    },\n" +
                "    \"sender\": {\n" +
                "      \"type\": \"phone\",\n" +
                "      \"identity\": \"15554441234\"\n" +
                "    },\n" +
                "    \"receiver\": {\n" +
                "      \"type\": \"twitter\",\n" +
                "      \"identity\": \"3215789654\",\n" +
                "      \"handle\": \"@someuser\"\n" +
                "    },\n" +
                "    \"request_id\": \"3fbdc415-8789-490a-ad32-0c6fa3590182\"\n" +
                "  },\n" +
                "  \"identity_hash\": \"498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49\",\n" +
                "  \"request_ttl\": 1531921356,\n" +
                "  \"status\": \"new\",\n" +
                "  \"txid\": \"7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03\",\n" +
                "  \"address_pubkey\": \"04cf39eab1213ad4a94e755fadaac4c8f2a256d7fa6b4044c7980113f7df60e24d5c1156b794d46652de2493013c6495469fbbac39d8c86495f1eebd65c7a6bddc\",\n" +
                "  \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\",\n" +
                "  \"delivery_status\": \"received\"\n" +
                "}"

        webServer.enqueue(MockResponse().setResponseCode(201).setBody(json))

        val payload = InviteUserPayload(
                Amount(120000000L, 8292280L),
                Sender("phone", "15554441234", null),
                Receiver("phone", "15554440000", null), "--request-id--")

        val response = signedCoinKeeperApiClient.inviteUser(payload)
        val recordedRequest = webServer.takeRequest()

        assertThat(recordedRequest.path, equalTo("/wallet/address_requests"))
        assertThat(response.code(), equalTo(201))
        assertThat(recordedRequest.body.readUtf8(), equalTo("{\"amount\":{\"btc\":120000000,\"usd\":8292280}," +
                "\"sender\":{\"type\":\"phone\",\"identity\":\"15554441234\"}," +
                "\"receiver\":{\"type\":\"phone\",\"identity\":\"15554440000\"}," +
                "\"request_id\":\"--request-id--\"}"))
    }

    @Test
    fun get_invites() {
        val json = "[\n" +
                "  {\n" +
                "    \"id\": \"a1bb1d88-bfc8-4085-8966-e0062278237c\",\n" +
                "     \"created_at\": 1531921356,\n" +
                "    \"updated_at\": 1531921356,\n" +
                "    \"address\": \"1JbJbAkCXtxpko39nby44hpPenpC1xKGYw\",\n" +
                "    \"metadata\": {\n" +
                "      \"amount\": {\n" +
                "        \"btc\": 120000000,\n" +
                "        \"usd\": 8292280\n" +
                "      },\n" +
                "      \"sender\": {\n" +
                "        \"country_code\": 1,\n" +
                "        \"phone_number\": \"5554441234\"\n" +
                "      },\n" +
                "      \"receiver\": {\n" +
                "        \"country_code\": 1,\n" +
                "        \"phone_number\": \"5554441234\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"sender\": \"498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49\",\n" +
                "    \"status\": \"waiting-response\",\n" +
                "    \"txid\": \"7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03\"\n" +
                "  }\n" +
                "]"
        webServer.enqueue(MockResponse().setResponseCode(200).setBody(json))
        val response = signedCoinKeeperApiClient.receivedInvites
        val invites = response.body() as List<ReceivedInvite>
        assertThat(response.code(), equalTo(200))
        assertThat(invites.size, equalTo(1))

        val invite = invites[0]
        assertThat<String>(invite.address, equalTo("1JbJbAkCXtxpko39nby44hpPenpC1xKGYw"))
        assertThat(invite.created_at_millis, equalTo(1531921356000L))
        assertThat(invite.id, equalTo("a1bb1d88-bfc8-4085-8966-e0062278237c"))
        assertThat(invite.sender, equalTo("498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49"))
        assertThat(invite.status, equalTo("waiting-response"))
    }

    @Test
    fun send_address_for_invites() {
        val addressPubKey = "9sw87rgh348hgfws8ervhw4980hfw8efgv"
        val json = "{\n" +
                "  \"id\": \"6d1d7318-81b9-492c-b3f3-9d1b24f91d14\",\n" +
                "  \"created_at\": 1531921356,\n" +
                "  \"updated_at\": 1531921357,\n" +
                "  \"address\": \"1JbJbAkCXtxpko39nby44hpPenpC1xKGYw\",\n" +
                "  \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\",\n" +
                "  \"address_pubkey\": \"f8e8c20drg978w48gher44f3b7ae955d\"\n" +
                "}"

        webServer.enqueue(MockResponse().setResponseCode(200).setBody(json))
        val response = signedCoinKeeperApiClient.sendAddressForInvite("6d1d7318-81b9-492c-b3f3-9d1b24f91d14", "1JbJbAkCXtxpko39nby44hpPenpC1xKGYw", addressPubKey)
        val cnWalletAddress = response.body() as CNWalletAddress

        assertThat(response.code(), equalTo(200))
        assertThat(cnWalletAddress.address, equalTo("1JbJbAkCXtxpko39nby44hpPenpC1xKGYw"))
        assertThat(cnWalletAddress.createdAt, equalTo(1531921356000L))
        assertThat(cnWalletAddress.id, equalTo("6d1d7318-81b9-492c-b3f3-9d1b24f91d14"))
    }

    @Test
    fun update_a_sent_invite_with_tx_id_and_mark_completed_test() {
        val json = "{\n" +
                "  \"id\": \"a1bb1d88-bfc8-4085-8966-e0062278237c\",\n" +
                "  \"created_at\": 1531921356,\n" +
                "  \"updated_at\": 1531921356,\n" +
                "  \"address\": \"1JbJbAkCXtxpko39nby44hpPenpC1xKGYw\",\n" +
                "  \"metadata\": {\n" +
                "    \"amount\": {\n" +
                "      \"btc\": 120000000,\n" +
                "      \"usd\": 8292280\n" +
                "    },\n" +
                "    \"sender\": {\n" +
                "      \"type\": \"phone\",\n" +
                "      \"identity\": \"15554441234\"\n" +
                "    },\n" +
                "    \"receiver\": {\n" +
                "      \"type\": \"twitter\",\n" +
                "      \"identity\": \"15554441234\",\n" +
                "      \"handle\": \"myTwitterHandle\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"phone_number_hash\": \"498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49\",\n" +
                "  \"request_ttl\": 1531921356,\n" +
                "  \"status\": \"completed\",\n" +
                "  \"txid\": \"7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03\",\n" +
                "  \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
                "}"

        webServer.enqueue(MockResponse().setResponseCode(200).setBody(json))
        val response = signedCoinKeeperApiClient.updateInviteStatusCompleted("a1bb1d88-bfc8-4085-8966-e0062278237c", "7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03")
        assertThat(response.code(), equalTo(200))
        val sentInvite = response.body() as SentInvite

        assertThat(sentInvite.status, equalTo("completed"))
        assertThat(sentInvite.id, equalTo("a1bb1d88-bfc8-4085-8966-e0062278237c"))
        assertThat<String>(sentInvite.txid, equalTo("7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03"))
        assertThat(sentInvite.metadata.sender.type, equalTo("phone"))
        assertThat(sentInvite.metadata.sender.identity, equalTo("15554441234"))
        assertNull(sentInvite.metadata.sender.handle)
        assertThat(sentInvite.metadata.receiver.type, equalTo("twitter"))
        assertThat(sentInvite.metadata.receiver.identity, equalTo("15554441234"))
        assertThat<String>(sentInvite.metadata.receiver.handle, equalTo("myTwitterHandle"))
    }

    @Test
    fun update_a_sent_invite_mark_as_canceled_test() {
        val json = "{\n" +
                "  \"id\": \"a1bb1d88-bfc8-4085-8966-e0062278237c\",\n" +
                "  \"created_at\": 1531921356,\n" +
                "  \"updated_at\": 1531921356,\n" +
                "  \"address\": \"1JbJbAkCXtxpko39nby44hpPenpC1xKGYw\",\n" +
                "  \"metadata\": {\n" +
                "    \"amount\": {\n" +
                "      \"btc\": 120000000,\n" +
                "      \"usd\": 8292280\n" +
                "    },\n" +
                "    \"sender\": {\n" +
                "      \"country_code\": 1,\n" +
                "      \"phone_number\": \"5554441234\"\n" +
                "    },\n" +
                "    \"receiver\": {\n" +
                "      \"country_code\": 1,\n" +
                "      \"phone_number\": \"5554441233\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"phone_number_hash\": \"498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49\",\n" +
                "  \"request_ttl\": 1531921356,\n" +
                "  \"status\": \"canceled\",\n" +
                "  \"txid\": \"7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03\",\n" +
                "  \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
                "}"

        webServer.enqueue(MockResponse().setResponseCode(200).setBody(json))
        val response = signedCoinKeeperApiClient.updateInviteStatusCanceled("a1bb1d88-bfc8-4085-8966-e0062278237c")
        assertThat(response.code(), equalTo(200))
        val sentInvite = response.body() as SentInvite

        assertThat(sentInvite.status, equalTo("canceled"))
    }

    @Test
    fun recevier_of_an_invite_sends_an_address_to_the_server_test() {
        val addressPubKey = "9sw87rgh348hgfws8ervhw4980hfw8efgv"
        val json = "{\n" +
                "\"id\": \"a1bb1d88-bfc8-4085-8966-e0062278237c\",\n" +
                "\"created_at\": 1531921356,\n" +
                "\"updated_at\": 1531921356,\n" +
                "\"address\": \"1JbJbAkCXtxpko39nby44hpPenpC1xKGYw\",\n" +
                "\"metadata\": {\n" +
                "\"amount\": {},\n" +
                "\"sender\": {},\n" +
                "\"receiver\": {}\n" +
                "},\n" +
                "\"phone_number_hash\": \"498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49\",\n" +
                "\"request_ttl\": 1531921356,\n" +
                "\"status\": \"new\",\n" +
                "\"txid\": \"7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03\",\n" +
                "\"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
                "}"

        val addressToSend = "1JbJbAkCXtxpko39nby44hpPenpC1xKGYw"
        val inviteID = "a1bb1d88-bfc8-4085-8966-e0062278237c"

        webServer.enqueue(MockResponse().setResponseCode(200).setBody(json))
        val response = signedCoinKeeperApiClient.sendAddressForInvite(inviteID, addressToSend, addressPubKey)


        assertThat(response.code(), equalTo(200))
        val sentInvite = response.body() as CNWalletAddress
        assertThat(sentInvite.address, equalTo("1JbJbAkCXtxpko39nby44hpPenpC1xKGYw"))
    }

    @Test
    fun get_all_sent_invites_test() {

        val json = "[\n" +
                "  {\n" +
                "    \"id\": \"a1bb1d88-bfc8-4085-8966-e0062278237c\",\n" +
                "    \"created_at\": 1531921356,\n" +
                "    \"updated_at\": 1531921356,\n" +
                "    \"address\": \"1JbJbAkCXtxpko39nby44hpPenpC1xKGYw\",\n" +
                "    \"metadata\": {\n" +
                "      \"amount\": {\n" +
                "        \"btc\": 120000000,\n" +
                "        \"usd\": 8292280\n" +
                "      },\n" +
                "      \"sender\": {\n" +
                "        \"country_code\": 1,\n" +
                "        \"phone_number\": \"5554441234\"\n" +
                "      },\n" +
                "      \"receiver\": {\n" +
                "        \"country_code\": 1,\n" +
                "        \"phone_number\": \"5554441234\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"phone_number_hash\": \"498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49\",\n" +
                "    \"request_ttl\": 1531921356,\n" +
                "    \"status\": \"completed\",\n" +
                "    \"txid\": \"7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03\",\n" +
                "    \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
                "  }\n" +
                "]"


        webServer.enqueue(MockResponse().setResponseCode(200).setBody(json))
        val response = signedCoinKeeperApiClient.sentInvites


        assertThat(response.code(), equalTo(200))
        val sentInvites = response.body() as List<SentInvite>
        assertThat(sentInvites.size, equalTo(1))
        val invite = sentInvites[0]
        assertThat(invite.id, equalTo("a1bb1d88-bfc8-4085-8966-e0062278237c"))
        assertThat(invite.status, equalTo("completed"))
        assertThat<String>(invite.txid, equalTo("7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03"))
    }

    @Test
    fun get_all_incoming_invites_test() {
        val json = "[\n" +
                "  {\n" +
                "    \"id\": \"a1bb1d88-bfc8-4085-8966-e0062278237c\",\n" +
                "    \"created_at\": 1531921356,\n" +
                "    \"updated_at\": 1531921356,\n" +
                "    \"address\": \"1JbJbAkCXtxpko39nby44hpPenpC1xKGYw\",\n" +
                "    \"metadata\": {\n" +
                "      \"amount\": {\n" +
                "        \"btc\": 120000000,\n" +
                "        \"usd\": 8292280\n" +
                "      },\n" +
                "      \"sender\": {\n" +
                "        \"country_code\": 1,\n" +
                "        \"phone_number\": \"5554441234\"\n" +
                "      },\n" +
                "      \"receiver\": {\n" +
                "        \"country_code\": 1,\n" +
                "        \"phone_number\": \"5554441234\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"request_ttl\": 1531921356,\n" +
                "    \"sender\": \"498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49\",\n" +
                "    \"status\": \"waiting-response\",\n" +
                "    \"txid\": \"7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03\"\n" +
                "  }\n" +
                "]"


        webServer.enqueue(MockResponse().setResponseCode(200).setBody(json))
        val response = signedCoinKeeperApiClient.receivedInvites


        assertThat(response.code(), equalTo(200))
        val receivedInvites = response.body() as List<ReceivedInvite>
        assertThat(receivedInvites.size, equalTo(1))
        val (id, _, _, address, sender, status) = receivedInvites[0]
        assertThat(id, equalTo("a1bb1d88-bfc8-4085-8966-e0062278237c"))
        assertThat(sender, equalTo("498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49"))
        assertThat(status, equalTo("waiting-response"))
        assertThat<String>(address, equalTo("1JbJbAkCXtxpko39nby44hpPenpC1xKGYw"))
    }

    @Test
    fun manufacture_a_custom_error_test() {

        val response = signedCoinKeeperApiClient.createUpdateInviteStatusError("id", "some error")

        assertThat(response.message(), equalTo("some error"))
    }

    @Test
    fun verify_when_creating_a_cn_device_using_the_overloaded_method_that_the_post_request_body_is_correct_test() {
        val sampleUuid = "--- UUID --- "
        val captor = argumentCaptor<JsonObject>()
        val call = mock<Call<CNDevice>>()
        whenever(call.execute()).thenReturn(Response.success(mock()))
        whenever(client.createCNDevice(any())).thenReturn(call)
        val apiClient = SignedCoinKeeperApiClient(client, FCM_APP_ID)

        apiClient.createCNDevice(sampleUuid)
        verify(client).createCNDevice(captor.capture())
        val requestBody = JsonParser().parse(captor.firstValue.toString()) as JsonObject


        val expectedAppName = CN_API_CREATE_DEVICE_APPLICATION_NAME
        val expectedDevicePlatform = CNDevice.DevicePlatform.ANDROID
        assertThat(requestBody.get(CN_API_CREATE_DEVICE_APPLICATION_KEY).asString, equalTo(expectedAppName))
        assertThat(requestBody.get(CN_API_CREATE_DEVICE_PLATFORM_KEY).asString, equalTo(expectedDevicePlatform.toString()))
        assertThat(requestBody.get(CN_API_CREATE_DEVICE_UUID_KEY).asString, equalTo(sampleUuid))
    }

    @Test
    fun verify_response_body_is_correct_when_creating_an_cn_device_test() {
        val json = "{\n" +
                "  \"id\": \"158a4cf9-0362-4636-8c68-ed7a98a7f345\",\n" +
                "  \"created_at\": 1531921356,\n" +
                "  \"updated_at\": 1531921355,\n" +
                "  \"application\": \"DropBit\",\n" +
                "  \"platform\": \"android\",\n" +
                "  \"uuid\": \"998207d6-5b1e-47c9-84e9-895f52a1b455\"\n" +
                "}"


        webServer.enqueue(MockResponse().setResponseCode(200).setBody(json))
        val response = signedCoinKeeperApiClient.createCNDevice("--- some UUID ---")
        val cnDevice = response.body() as CNDevice

        assertThat(cnDevice.id, equalTo("158a4cf9-0362-4636-8c68-ed7a98a7f345"))
        assertThat(cnDevice.createdDate, equalTo(1531921356000L))
        assertThat(cnDevice.updatedDate, equalTo(1531921355000L))
        assertThat(cnDevice.applicationName, equalTo("DropBit"))
        assertThat(cnDevice.platform, equalTo(CNDevice.DevicePlatform.ANDROID))
        assertThat(cnDevice.uuid, equalTo("998207d6-5b1e-47c9-84e9-895f52a1b455"))
    }

    @Test
    fun verity_registers_endpoint() {
        val captor = argumentCaptor<JsonObject>()
        val apiClient = SignedCoinKeeperApiClient(client, FCM_APP_ID)
        val call = mock<Call<CNDeviceEndpoint>>()
        whenever(call.execute()).thenReturn(Response.success(mock()))
        whenever(client.registerForPushEndpoint(any(), any())).thenReturn(call)

        val cnDeviceId = "-- cn device id"
        val pushToken = "-- push token"

        apiClient.registerForPushEndpoint(cnDeviceId, pushToken)

        verify(client).registerForPushEndpoint(eq(cnDeviceId), captor.capture())

        val body = captor.firstValue

        assertThat(body.get("application").asString, equalTo(FCM_APP_ID))
        assertThat(body.get("platform").asString, equalTo("GCM"))
        assertThat(body.get("token").asString, equalTo(pushToken))
    }

    @Test
    fun consume_the_register_for_push_end_point() {
        val json = "{\n" +
                "  \"id\": \"5805b3a0-ed99-4073-ad18-72adff181b9e\",\n" +
                "  \"created_at\": 1531921356,\n" +
                "  \"updated_at\": 1531921356,\n" +
                "  \"application\": \"drop-it-prod-01\",\n" +
                "  \"platform\": \"APNS_SANDBOX\",\n" +
                "  \"token\": \"740f4707bebcf74f9b7c25d48e3358945f6aa01da5ddb387462c7eaf61bb78ad\",\n" +
                "  \"device\": {\n" +
                "    \"id\": \"158a4cf9-0362-4636-8c68-ed7a98a7f345\",\n" +
                "    \"created_at\": 1531921356,\n" +
                "    \"updated_at\": 1531921356,\n" +
                "    \"application\": \"DropIt\",\n" +
                "    \"platform\": \"ios\",\n" +
                "    \"uuid\": \"998207d6-5b1e-47c9-84e9-895f52a1b455\"\n" +
                "  },\n" +
                "  \"device_id\": \"158a4cf9-0362-4636-8c68-ed7a98a7f345\"\n" +
                "}"

        val cnDeviceId = "-- cn device id"
        val pushToken = "-- push token"

        webServer.enqueue(MockResponse().setResponseCode(201).setBody(json))
        val response = signedCoinKeeperApiClient.registerForPushEndpoint(cnDeviceId, pushToken)
        val cnDeviceEndpoint = response.body() as CNDeviceEndpoint

        assertThat(response.code(), equalTo(201))
        assertThat(cnDeviceEndpoint.id, equalTo("5805b3a0-ed99-4073-ad18-72adff181b9e"))
    }

    @Test
    fun unregisters_endpoint_for_device_id() {
        val deviceId = "--device"
        val endpoint = "--endpoint"
        webServer.enqueue(MockResponse().setResponseCode(204))

        val response = signedCoinKeeperApiClient.unRegisterDeviceEndpoint(deviceId, endpoint)

        assertThat(response.code(), equalTo(204))
    }

    @Test
    fun fetches_device_endpoints() {
        val deviceId = "--device"
        val json = "[\n" +
                "{\n" +
                "\"id\": \"5805b3a0-ed99-4073-ad18-72adff181b9e\",\n" +
                "\"created_at\": 1531921356,\n" +
                "\"updated_at\": 1531921356,\n" +
                "\"application\": \"dropbit-prod-01\",\n" +
                "\"platform\": \"APNS_SANDBOX\",\n" +
                "\"token\": \"740f4707bebcf74f9b7c25d48e3358945f6aa01da5ddb387462c7eaf61bb78ad\",\n" +
                "\"device\": {},\n" +
                "\"device_id\": \"158a4cf9-0362-4636-8c68-ed7a98a7f345\"\n" +
                "}\n" +
                "]"

        webServer.enqueue(MockResponse().setResponseCode(200).setBody(json))

        val response = signedCoinKeeperApiClient.fetchRemoteEndpointsFor(deviceId)
        val endpoints = response.body() as List<CNDeviceEndpoint>

        assertThat(endpoints.size, equalTo(1))
        assertThat(endpoints[0].id, equalTo("5805b3a0-ed99-4073-ad18-72adff181b9e"))
    }

    @Test
    fun builds_subscription_to_wallet_request() {
        val captor = argumentCaptor<JsonObject>()
        val apiClient = SignedCoinKeeperApiClient(client, FCM_APP_ID)
        val endpointId = "-- endpoint "

        val call = mock<Call<CNSubscription>>()
        whenever(call.execute()).thenReturn(Response.success(mock()))
        whenever(client.subscribeToWalletTopic(any())).thenReturn(call)

        apiClient.subscribeToWalletNotifications(endpointId)

        verify(client).subscribeToWalletTopic(captor.capture())

        val body = captor.firstValue
        assertThat(body.get("device_endpoint_id").asString, equalTo(endpointId))
    }

    @Test
    fun subscribes_to_wallet_topics() {
        val json = "{\n" +
                "\"id\": \"--topic id 1\",\n" +
                "\"created_at\": 1531921356,\n" +
                "\"updated_at\": 1531921356,\n" +
                "\"owner_type\": \"Wallet\",\n" +
                "\"owner_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\",\n" +
                "\"device_endpoint\": {\n" +
                "       \"id\": \"5805b3a0-ed99-4073-ad18-72adff181b9e\",\n" +
                "       \"created_at\": 1531921356,\n" +
                "       \"updated_at\": 1531921356,\n" +
                "       \"application\": \"dropbit-prod-01\",\n" +
                "       \"platform\": \"APNS_SANDBOX\",\n" +
                "       \"token\": \"740f4707bebcf74f9b7c25d48e3358945f6aa01da5ddb387462c7eaf61bb78ad\",\n" +
                "       \"device_id\": \"158a4cf9-0362-4636-8c68-ed7a98a7f345\"\n" +
                "},\n" +
                "\"device_endpoint_id\": \"5805b3a0-ed99-4073-ad18-72adff181b9e\"\n" +
                "}"

        webServer.enqueue(MockResponse().setResponseCode(200).setBody(json))
        val endpointId = "--endpoint id"

        val response = signedCoinKeeperApiClient.subscribeToWalletNotifications(endpointId)
        val subscription = response.body() as CNSubscription

        assertThat(subscription.id, equalTo("--topic id 1"))
    }

    @Test
    fun builds_update_subscription_to_wallet_request() {
        val captor = argumentCaptor<JsonObject>()
        val apiClient = SignedCoinKeeperApiClient(client, FCM_APP_ID)
        val endpointId = "-- endpoint "

        val call = mock<Call<CNSubscription>>()
        whenever(call.execute()).thenReturn(Response.success(mock()))
        whenever(client.updateWalletSubscription(any())).thenReturn(call)

        apiClient.updateWalletSubscription(endpointId)

        verify(client).updateWalletSubscription(captor.capture())

        val body = captor.firstValue
        assertThat(body.get("device_endpoint_id").asString, equalTo(endpointId))
    }

    @Test
    fun updates_wallet_topics() {
        val json = "{\n" +
                "\"id\": \"--topic id 1\",\n" +
                "\"created_at\": 1531921356,\n" +
                "\"updated_at\": 1531921356,\n" +
                "\"owner_type\": \"Wallet\",\n" +
                "\"owner_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\",\n" +
                "\"device_endpoint\": {\n" +
                "       \"id\": \"5805b3a0-ed99-4073-ad18-72adff181b9e\",\n" +
                "       \"created_at\": 1531921356,\n" +
                "       \"updated_at\": 1531921356,\n" +
                "       \"application\": \"dropbit-prod-01\",\n" +
                "       \"platform\": \"APNS_SANDBOX\",\n" +
                "       \"token\": \"740f4707bebcf74f9b7c25d48e3358945f6aa01da5ddb387462c7eaf61bb78ad\",\n" +
                "       \"device_id\": \"158a4cf9-0362-4636-8c68-ed7a98a7f345\"\n" +
                "},\n" +
                "\"device_endpoint_id\": \"5805b3a0-ed99-4073-ad18-72adff181b9e\"\n" +
                "}"

        webServer.enqueue(MockResponse().setResponseCode(200).setBody(json))
        val endpointId = "--endpoint id"

        val response = signedCoinKeeperApiClient.updateWalletSubscription(endpointId)
        val subscription = response.body() as CNSubscription

        assertThat(subscription.id, equalTo("--topic id 1"))
    }

    @Test
    @Throws(InterruptedException::class)
    fun subscribes_to_generic_topics() {
        val json = "[{\n" +
                "\"id\": \"--topic id 1\",\n" +
                "\"created_at\": 1531921356,\n" +
                "\"updated_at\": 1531921356,\n" +
                "\"owner_type\": \"Wallet\",\n" +
                "\"owner_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\",\n" +
                "\"device_endpoint\": {\n" +
                "       \"id\": \"5805b3a0-ed99-4073-ad18-72adff181b9e\",\n" +
                "       \"created_at\": 1531921356,\n" +
                "       \"updated_at\": 1531921356,\n" +
                "       \"application\": \"dropbit-prod-01\",\n" +
                "       \"platform\": \"APNS_SANDBOX\",\n" +
                "       \"token\": \"740f4707bebcf74f9b7c25d48e3358945f6aa01da5ddb387462c7eaf61bb78ad\",\n" +
                "       \"device_id\": \"158a4cf9-0362-4636-8c68-ed7a98a7f345\"\n" +
                "},\n" +
                "\"device_endpoint_id\": \"5805b3a0-ed99-4073-ad18-72adff181b9e\"\n" +
                "}]"

        webServer.enqueue(MockResponse().setResponseCode(200).setBody(json))

        val deviceId = "--device-id"
        val endpointId = "--endpoint-id"

        val topics = ArrayList<String>()
        topics.add("--topic id 1")
        topics.add("--topic id 2")
        val topicSubscription = CNTopicSubscription(topics)

        val response = signedCoinKeeperApiClient.subscribeToTopics(deviceId, endpointId, topicSubscription)
        val recordedRequest = webServer.takeRequest()

        assertThat(recordedRequest.path, equalTo("/devices/--device-id/endpoints/--endpoint-id/subscriptions"))
        assertThat(recordedRequest.method, equalTo("POST"))
        assertThat(response.code(), equalTo(200))
        assertThat(recordedRequest.body.readUtf8(), equalTo("{\"topic_ids\":[\"--topic id 1\",\"--topic id 2\"]}"))
    }

    @Test
    @Throws(InterruptedException::class)
    fun unsubscribe_from_topic() {
        val deviceId = "--device-id--"
        val endpointId = "--endpoint-id--"
        val topicId = "--topic-id--"
        webServer.enqueue(MockResponse().setResponseCode(200))

        val response = signedCoinKeeperApiClient.unsubscribeFromTopic(deviceId, endpointId, topicId)
        val recordedRequest = webServer.takeRequest()

        assertThat(recordedRequest.path, equalTo("/devices/--device-id--/endpoints/--endpoint-id--/subscriptions/--topic-id--"))
        assertThat(recordedRequest.method, equalTo("DELETE"))
        assertThat(response.code(), equalTo(200))
        assertThat(recordedRequest.body.readUtf8(), equalTo(""))
    }

    @Test
    fun fetches_general_topics() {
        val json = "{\n" +
                "    \"available_topics\": [\n" +
                "        {\n" +
                "            \"id\": \"ba3f2126-3e1a-47b7-982a-8caccba56e3d\",\n" +
                "            \"created_at\": 1540845282,\n" +
                "            \"updated_at\": 1540845282,\n" +
                "            \"name\": \"general\",\n" +
                "            \"display_name\": \"General\",\n" +
                "            \"description\": \"The latest updates for DropBit\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"subscriptions\": [\n" +
                "        {\n" +
                "            \"id\": \"8a8b9a55-fe26-49bf-9e01-a81cc0bb7a66\",\n" +
                "            \"created_at\": 1541786068,\n" +
                "            \"updated_at\": 1541786069,\n" +
                "            \"device_endpoint\": {\n" +
                "                \"id\": \"d047bed1-988b-404f-9e6d-eeb8ae12e8ca\",\n" +
                "                \"created_at\": 1541785231,\n" +
                "                \"updated_at\": 1541786068,\n" +
                "                \"application\": \"dropbit-prod-01\",\n" +
                "                \"platform\": \"GCM\",\n" +
                "                \"token\": \"fb6KEvMS8fM:APA91bGRqGAyuQX7jyXweSycAXE0wXqhJGnVRBnpabsXx7q6zO2e6G1wo5p3K9RYGtH0GLm9U17q6LLIeh-dYtJrwX48mwxMRiBQ0GvTb9tLSVQ0YmayJQGCEG_YIug8JSxHubiaVpSM\",\n" +
                "                \"device\": {\n" +
                "                    \"id\": \"\",\n" +
                "                    \"created_at\": 0,\n" +
                "                    \"updated_at\": 0,\n" +
                "                    \"application\": \"\",\n" +
                "                    \"platform\": \"\",\n" +
                "                    \"metadata\": null,\n" +
                "                    \"uuid\": \"\"\n" +
                "                },\n" +
                "                \"device_id\": \"6929b5a1-2f4f-4da6-a0d8-ef130430c413\"\n" +
                "            },\n" +
                "            \"device_endpoint_id\": \"d047bed1-988b-404f-9e6d-eeb8ae12e8ca\",\n" +
                "            \"owner\": null,\n" +
                "            \"owner_id\": \"ba3f2126-3e1a-47b7-982a-8caccba56e3d\",\n" +
                "            \"owner_type\": \"Topic\"\n" +
                "        }\n" +
                "    ]\n" +
                "}"

        webServer.enqueue(MockResponse().setResponseCode(200).setBody(json))
        val resposne = signedCoinKeeperApiClient.fetchDeviceEndpointSubscriptions("-deviceId", "-endpointId")

        assertThat(resposne.code(), equalTo(200))
        val subscriptionState = resposne.body() as CNSubscriptionState
        assertNotNull(subscriptionState.availableTopics)
        assertNotNull(subscriptionState.subscriptions)
        assertThat(subscriptionState.subscriptions[0].id,
                equalTo("8a8b9a55-fe26-49bf-9e01-a81cc0bb7a66"))
        assertThat(subscriptionState.subscriptions[0].ownerId,
                equalTo("ba3f2126-3e1a-47b7-982a-8caccba56e3d"))
        assertThat(subscriptionState.subscriptions[0].ownerType,
                equalTo("Topic"))


        assertThat(subscriptionState.availableTopics[0].id,
                equalTo("ba3f2126-3e1a-47b7-982a-8caccba56e3d"))

    }

    @Test
    @Throws(InterruptedException::class)
    fun posts_transaction_notification_passes_response_through() {

        val json = "{}"
        val expectedResponse = MockResponse().setResponseCode(200).setBody(json)
        webServer.enqueue(expectedResponse)

        val encryptedPayload = "ENCReYaw54987thq3Ptyer9t8hga35g87h98ae54yhg9ert85u0q9834ty80"
        val phoneNumberHash = "sau4yre6zto8uhj430a8jtw497ethyu39q78hyta908puyth89sa4ht893shyts98e45y"
        val address = "q"
        val txid = "42"

        val response = signedCoinKeeperApiClient.postTransactionNotification(CNSharedMemo(txid, address, phoneNumberHash, encryptedPayload, "2"))
        val recordedRequest = webServer.takeRequest()

        assertThat(recordedRequest.path, equalTo("/transaction/notification"))
        assertThat(recordedRequest.body.readUtf8(), equalTo("{\"txid\":\"42\",\"address\":\"q\",\"identity_hash\":\"sau4yre6zto8uhj430a8jtw497ethyu39q78hyta908puyth89sa4ht893shyts98e45y\",\"encrypted_payload\":\"ENCReYaw54987thq3Ptyer9t8hga35g87h98ae54yhg9ert85u0q9834ty80\",\"encrypted_format\":\"2\"}"))
        assertThat(response.code(), equalTo(200))
    }

    @Test
    @Throws(InterruptedException::class)
    fun get_transaction_notification() {
        val txid = "7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03"
        val address = "34Xa8X8pfwvUyq4VGZFXUhzrTemJrbgcsu"
        val encryptedPayload = "Y2QyM2M4ZjY4MTU4ZjhiYmRjZDMzN2E1ZjI0NTE2MjBkNmY3ZjNhMjc5MGQ1OTg1M2ZkYzYyMGI4NzZkNGUwMzgxNThmOGJiZGNkNzIzM2ViN2EzM2IK"
        val format = "1"

        val json = "[\n" +
                "  {\n" +
                "    \"txid\": \"" + txid + "\",\n" +
                "    \"address\": \"" + address + "\",\n" +
                "    \"encrypted_payload\": \"" + encryptedPayload + "\",\n" +
                "    \"encrypted_format\": \"" + format + "\"\n" +
                "  }\n" +
                "]"
        val expectedResponse = MockResponse().setResponseCode(200).setBody(json)
        webServer.enqueue(expectedResponse)

        val response = signedCoinKeeperApiClient.getTransactionNotification(txid)

        val (txid1, address1, _, encrypted_payload, encrypted_format) = (response.body() as List<CNSharedMemo>)[0]

        assertThat<String>(txid1, equalTo(txid))
        assertThat<String>(address1, equalTo(address))
        assertThat<String>(encrypted_payload, equalTo(encryptedPayload))
        assertThat<String>(encrypted_format, equalTo(format))
        val recordedRequest = webServer.takeRequest()
        assertThat(recordedRequest.path, equalTo("/transaction/notification/$txid"))
    }

    @Test
    @Throws(InterruptedException::class)
    fun enables_dropbit_me_account() {
        val json = "{\n" +
                "  \"id\": \"ad983e63-526d-4679-a682-c4ab052b20e1\",\n" +
                "  \"created_at\": 1531921356,\n" +
                "  \"updated_at\": 1531921356,\n" +
                "  \"status\": \"pending-verification\",\n" +
                "  \"private\": false,\n" +
                "  \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
                "}"
        val expectedResponse = MockResponse().setResponseCode(200).setBody(json)
        webServer.enqueue(expectedResponse)

        val response = signedCoinKeeperApiClient.enableDropBitMeAccount()

        val recordedRequest = webServer.takeRequest()
        assertThat(recordedRequest.path, equalTo("/user"))
        assertThat(recordedRequest.body.readUtf8(), equalTo("{\"private\":false}"))
        val userPatch = response.body() as CNUserPatch
        assertFalse(userPatch.isPrivate)
    }

    @Test
    @Throws(InterruptedException::class)
    fun disables_dropbit_me_account() {
        val json = "{\n" +
                "  \"id\": \"ad983e63-526d-4679-a682-c4ab052b20e1\",\n" +
                "  \"created_at\": 1531921356,\n" +
                "  \"updated_at\": 1531921356,\n" +
                "  \"status\": \"pending-verification\",\n" +
                "  \"private\": true,\n" +
                "  \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
                "}"
        val expectedResponse = MockResponse().setResponseCode(200).setBody(json)
        webServer.enqueue(expectedResponse)

        val response = signedCoinKeeperApiClient.disableDropBitMeAccount()

        val recordedRequest = webServer.takeRequest()
        assertThat(recordedRequest.path, equalTo("/user"))
        assertThat(recordedRequest.body.readUtf8(), equalTo("{\"private\":true}"))
        val userPatch = response.body() as CNUserPatch
        assertTrue(userPatch.isPrivate)
    }

    @Test
    @Throws(InterruptedException::class)
    fun deletes_user_identity() {
        val expectedResponse = MockResponse().setResponseCode(204).setBody("")
        webServer.enqueue(expectedResponse)
        val identity: DropbitMeIdentity = mock()
        whenever(identity.serverId).thenReturn("--server-id--")

        val response = signedCoinKeeperApiClient.deleteIdentity(identity)

        val recordedRequest = webServer.takeRequest()
        assertThat(recordedRequest.path, equalTo("/user/identity/--server-id--"))
        assertThat(response.code(), equalTo(204))
    }

    @Test
    @Throws(InterruptedException::class)
    fun fetches_identities() {
        val json = "[\n" +
                "  {\n" +
                "    \"id\": \"ad983e63-526d-4679-a682-c4ab052b20e1\",\n" +
                "    \"created_at\": 1531921356,\n" +
                "    \"updated_at\": 1531921356,\n" +
                "    \"type\": \"phone\",\n" +
                "    \"identity\": \"13305551212\",\n" +
                "    \"handle\": \"498803d5964a\",\n" +
                "    \"hash\": \"498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49\",\n" +
                "    \"status\": \"pending-verification\"\n" +
                "  }\n" +
                "]"
        val expectedResponse = MockResponse().setResponseCode(200).setBody(json)
        webServer.enqueue(expectedResponse)

        val response = signedCoinKeeperApiClient.identities
        val identities = response.body() as List<CNUserIdentity>
        val recordedRequest = webServer.takeRequest()

        assertThat(recordedRequest.path, equalTo("/user/identity"))
        assertThat(response.code(), equalTo(200))
        assertThat(identities.size, equalTo(1))
        assertThat<String>(identities[0].id, equalTo("ad983e63-526d-4679-a682-c4ab052b20e1"))
        assertThat<String>(identities[0].type, equalTo("phone"))
        assertThat<String>(identities[0].identity, equalTo("13305551212"))
        assertThat<String>(identities[0].hash, equalTo("498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49"))
    }

    @Test
    @Throws(InterruptedException::class)
    fun adds_identity() {
        val json = "  {\n" +
                "    \"id\": \"ad983e63-526d-4679-a682-c4ab052b20e1\",\n" +
                "    \"created_at\": 1531921356,\n" +
                "    \"updated_at\": 1531921356,\n" +
                "    \"type\": \"phone\",\n" +
                "    \"identity\": \"13305551212\",\n" +
                "    \"handle\": \"498803d5964a\",\n" +
                "    \"hash\": \"498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49\",\n" +
                "    \"status\": \"pending-verification\"\n" +
                "  }\n"
        val expectedResponse = MockResponse().setResponseCode(200).setBody(json)
        webServer.enqueue(expectedResponse)
        var identity = CNUserIdentity()

        val response = signedCoinKeeperApiClient.addIdentity(identity)
        identity = response.body() as CNUserIdentity
        val recordedRequest = webServer.takeRequest()

        assertThat(recordedRequest.path, equalTo("/user/identity"))
        assertThat(recordedRequest.method, equalTo("POST"))
        assertThat(response.code(), equalTo(200))
        assertThat<String>(identity.id, equalTo("ad983e63-526d-4679-a682-c4ab052b20e1"))
        assertThat<String>(identity.type, equalTo("phone"))
        assertThat<String>(identity.identity, equalTo("13305551212"))
        assertThat<String>(identity.hash, equalTo("498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49"))
    }

    @Test
    @Throws(InterruptedException::class)
    fun verifies_identity() {
        val json = "{\n" +
                "  \"id\": \"ad983e63-526d-4679-a682-c4ab052b20e1\",\n" +
                "  \"created_at\": 1531921356,\n" +
                "  \"updated_at\": 1531921356,\n" +
                "  \"status\": \"pending-verification\",\n" +
                "  \"private\": true,\n" +
                "  \"identities\": [\n" +
                "    {\n" +
                "      \"id\": \"ad983e63-526d-4679-a682-c4ab052b20e1\",\n" +
                "      \"created_at\": 1531921356,\n" +
                "      \"updated_at\": 1531921356,\n" +
                "      \"type\": \"phone\",\n" +
                "      \"identity\": \"13305551212\",\n" +
                "      \"hash\": \"498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49\",\n" +
                "      \"status\": \"pending-verification\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
                "}"

        val expectedResponse = MockResponse().setResponseCode(200).setBody(json)
        webServer.enqueue(expectedResponse)
        val identity = CNUserIdentity()

        val response = signedCoinKeeperApiClient.verifyIdentity(identity)
        val account = response.body() as CNUserAccount
        val recordedRequest = webServer.takeRequest()

        assertThat(recordedRequest.path, equalTo("/user/verify"))
        assertThat(recordedRequest.method, equalTo("POST"))
        assertThat(response.code(), equalTo(200))
        assertThat(account.identities[0].id, equalTo("ad983e63-526d-4679-a682-c4ab052b20e1"))
        assertThat(account.identities[0].type, equalTo("phone"))
        assertThat(account.identities[0].identity, equalTo("13305551212"))
        assertThat(account.identities[0].hash, equalTo("498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49"))
    }

    @Test
    @Throws(InterruptedException::class)
    fun create_user_from_identity() {
        val json = "{\n" +
                "  \"id\": \"ad983e63-526d-4679-a682-c4ab052b20e1\",\n" +
                "  \"created_at\": 1531921356,\n" +
                "  \"updated_at\": 1531921356,\n" +
                "  \"status\": \"pending-verification\",\n" +
                "  \"private\": true,\n" +
                "  \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
                "}"

        val expectedResponse = MockResponse().setResponseCode(200).setBody(json)
        webServer.enqueue(expectedResponse)
        val identity = CNUserIdentity()

        val response = signedCoinKeeperApiClient.createUserFromIdentity(identity)
        val account = response.body() as CNUserAccount
        val recordedRequest = webServer.takeRequest()

        assertThat(recordedRequest.path, equalTo("/user"))
        assertThat(recordedRequest.method, equalTo("POST"))
        assertThat(response.code(), equalTo(200))
        assertThat<String>(account.id, equalTo("ad983e63-526d-4679-a682-c4ab052b20e1"))
        assertThat<String>(account.status, equalTo("pending-verification"))
        assertThat(account.isPrivate, equalTo(true))
        assertThat<String>(account.wallet_id, equalTo("f8e8c20e-ba44-4bac-9a96-44f3b7ae955d"))
    }

    @Test
    @Throws(InterruptedException::class)
    fun fetches_pricing_for_granularity() {
        val json = TEST_DATA_PRICING.pricing
        val expectedResponse = MockResponse().setResponseCode(200).setBody(json)
        webServer.enqueue(expectedResponse)

        val response = signedCoinKeeperApiClient.loadHistoricPricing(Granularity.DAY)
        val priceRecords = response.body() as List<HistoricalPriceRecord>
        val recordedRequest = webServer.takeRequest()

        assertThat(recordedRequest.path, equalTo("/pricing/historic?period=daily"))
        assertThat(recordedRequest.method, equalTo("GET"))
        assertThat(response.code(), equalTo(200))
        assertThat(priceRecords.size, equalTo(2))
        assertThat(priceRecords[0].average, equalTo(12186.82f))

    }

    @Test
    @Throws(InterruptedException::class)
    fun fetchesNews() {
        val json = TEST_DATA_PRICING.news
        val expectedResponse = MockResponse().setResponseCode(200).setBody(json)
        webServer.enqueue(expectedResponse)

        val response = signedCoinKeeperApiClient.loadNews(4, 0)
        val articles = response.body() as List<NewsArticle>
        val recordedRequest = webServer.takeRequest()

        assertThat(recordedRequest.path, equalTo("/news/feed/items?count=4"))
        assertThat(recordedRequest.method, equalTo("GET"))
        assertThat(response.code(), equalTo(200))
        assertThat(articles.size, equalTo(4))
        assertThat(articles[0].id, equalTo("https://www.coindesk.com/?p=408236"))
        assertThat<String>(articles[0].title, equalTo("tZERO Tokenizes Atari Founder’s Biopic"))
        assertThat<String>(articles[0].link, equalTo("https://www.coindesk.com/tzero-tokenizes-atari-founders-biopic"))
        assertThat<String>(articles[0].description, equalTo("The subsidiary of Overstock is attempting to break into the movie business."))
        assertThat<String>(articles[0].source, equalTo("coindesk"))
        assertThat<String>(articles[0].author, equalTo("Daniel Kuhn"))
        assertThat(articles[0].pubTime, equalTo(1562704225L))
        assertThat(articles[0].added, equalTo(1562704492L))
    }

    @Test
    @Throws(InterruptedException::class)
    fun fetchingNewsWithOffsetTrimsList() {
        val json = TEST_DATA_PRICING.news
        val expectedResponse = MockResponse().setResponseCode(200).setBody(json)
        webServer.enqueue(expectedResponse)

        val response = signedCoinKeeperApiClient.loadNews(2, 2)
        val articles = response.body() as List<NewsArticle>
        val recordedRequest = webServer.takeRequest()

        assertThat(recordedRequest.path, equalTo("/news/feed/items?count=4"))
        assertThat(recordedRequest.method, equalTo("GET"))
        assertThat(response.code(), equalTo(200))
        assertThat(articles.size, equalTo(2))
        assertThat<String>(articles[0].title, equalTo("Italy to Lead European Blockchain Partnership Until July 2020"))
    }

    private fun createClient(host: String): SignedCoinKeeperApiClient {
        val clientBuilder = OkHttpClient.Builder()
        clientBuilder.connectTimeout(100, TimeUnit.MILLISECONDS)
                .writeTimeout(100, TimeUnit.MILLISECONDS)
                .readTimeout(100, TimeUnit.MILLISECONDS)

        val client = Retrofit.Builder().baseUrl(host).client(clientBuilder.build()).addConverterFactory(GsonConverterFactory.create()).build().create(CoinKeeperClient::class.java)

        return SignedCoinKeeperApiClient(client, FCM_APP_ID)
    }

    companion object {

        private val FCM_APP_ID = "dropbit-prod-01"
    }

}