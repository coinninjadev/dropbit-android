package com.coinninja.coinkeeper.service.client;

import com.coinninja.coinkeeper.cn.wallet.DataSigner;
import com.coinninja.coinkeeper.model.Contact;
import com.coinninja.coinkeeper.model.db.DropbitMeIdentity;
import com.coinninja.coinkeeper.service.client.model.AddressLookupResult;
import com.coinninja.coinkeeper.service.client.model.Amount;
import com.coinninja.coinkeeper.service.client.model.CNDevice;
import com.coinninja.coinkeeper.service.client.model.CNDeviceEndpoint;
import com.coinninja.coinkeeper.service.client.model.CNPhoneNumber;
import com.coinninja.coinkeeper.service.client.model.CNSharedMemo;
import com.coinninja.coinkeeper.service.client.model.CNSubscription;
import com.coinninja.coinkeeper.service.client.model.CNSubscriptionState;
import com.coinninja.coinkeeper.service.client.model.CNTopicSubscription;
import com.coinninja.coinkeeper.service.client.model.CNUserPatch;
import com.coinninja.coinkeeper.service.client.model.CNWallet;
import com.coinninja.coinkeeper.service.client.model.CNWalletAddress;
import com.coinninja.coinkeeper.service.client.model.InviteUserPayload;
import com.coinninja.coinkeeper.service.client.model.ReceivedInvite;
import com.coinninja.coinkeeper.service.client.model.Receiver;
import com.coinninja.coinkeeper.service.client.model.Sender;
import com.coinninja.coinkeeper.service.client.model.SentInvite;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.coinninja.coinkeeper.util.DropbitIntents.CN_API_CREATE_DEVICE_APPLICATION_KEY;
import static com.coinninja.coinkeeper.util.DropbitIntents.CN_API_CREATE_DEVICE_APPLICATION_NAME;
import static com.coinninja.coinkeeper.util.DropbitIntents.CN_API_CREATE_DEVICE_PLATFORM_KEY;
import static com.coinninja.coinkeeper.util.DropbitIntents.CN_API_CREATE_DEVICE_UUID_KEY;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SignedCoinKeeperApiClientTest {

    private static final String FCM_APP_ID = "dropbit-prod-01";
    @Mock
    private DataSigner dataSigner;

    @Mock
    private CoinKeeperClient client;
    private MockWebServer webServer;
    private SignedCoinKeeperApiClient signedCoinKeeperApiClient;

    @Before
    public void setUp() {
        webServer = new MockWebServer();
        signedCoinKeeperApiClient = createClient(webServer.url("").toString());
    }

    @After
    public void tearDown() throws IOException {
        dataSigner = null;
        client = null;
        signedCoinKeeperApiClient = null;
        webServer.shutdown();
        webServer = null;
    }

    @Test
    public void verifies_account() {
        String json = "{\n" +
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
                "}";

        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(json));

        Response response = signedCoinKeeperApiClient.verifyAccount();
        CNUserAccount cnUserAccount = (CNUserAccount) response.body();
        assertThat(cnUserAccount.getId(), equalTo("ad983e63-526d-4679-a682-c4ab052b20e1"));
        assertThat(cnUserAccount.getIdentities().size(), equalTo(1));
        assertThat(cnUserAccount.getIdentities().get(0).getIdentity(), equalTo("13305551212"));
        assertThat(cnUserAccount.getIdentities().get(0).getType(), equalTo("phone"));
    }

    @Test
    public void verifies_wallet() {
        String json = "{\n" +
                "\"id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\",\n" +
                "\"public_key_string\": \"02262233847a69026f8f3ae027af347f2501adf008fe4f6087d31a1d975fd41473\",\n" +
                "\"created_at\": 1531921356,\n" +
                "\"updated_at\": 1531921356,\n" +
                "\"user_id\": \"ad983e63-526d-4679-a682-c4ab052b20e1\"\n" +
                "}";

        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(json));

        Response response = signedCoinKeeperApiClient.verifyWallet();
        CNWallet cnWallet = (CNWallet) response.body();
        assertThat(cnWallet.getPublicKeyString(), equalTo("02262233847a69026f8f3ae027af347f2501adf008fe4f6087d31a1d975fd41473"));
    }

    @Test
    public void deletes_users_wallet() {
        webServer.enqueue(new MockResponse().setResponseCode(204).setBody(""));

        assertTrue(signedCoinKeeperApiClient.resetWallet().isSuccessful());
    }

    @Test
    public void querys_for_contacts_address() {
        String json = "{" +
                "  \"query\": {" +
                "    \"terms\": {" +
                "      \"phone_number_hash\": [" +
                "        \"9906c77c0aa1f6e4760a68719c79bdf605d1f7819a15d06bc6dfc216c047339f\"" +
                "      ]" +
                "    }," +
                "    \"address_pubkey\": true" +
                "  }" +
                "}";


        when(client.queryWalletAddress(any(JsonObject.class))).thenReturn(mock(Call.class));
        ArgumentCaptor<JsonObject> captor = ArgumentCaptor.forClass(JsonObject.class);
        SignedCoinKeeperApiClient apiClient = new SignedCoinKeeperApiClient(client, FCM_APP_ID);
        String phoneHash = "9906c77c0aa1f6e4760a68719c79bdf605d1f7819a15d06bc6dfc216c047339f";
        apiClient.queryWalletAddress(phoneHash);

        verify(client).queryWalletAddress(captor.capture());

        assertThat(captor.getValue().toString(), equalTo(json.replaceAll("\\s", "")));
    }

    @Test
    public void deserializes_results_for_contacts_address() {
        String json = "[\n" +
                "  {\n" +
                "    \"phone_number_hash\": \"498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49\",\n" +
                "    \"address\": \"1JbJbAkCXtxpko39nby44hpPenpC1xKGYw\",\n" +
                "    \"address_pubkey\": \"04cf39eab1213ad4a94e755fadaac4c8f2a256d7fa6b4044c7980113f7df60e24d5c1156b794d46652de2493013c6495469fbbac39d8c86495f1eebd65c7a6bddc\"\n" +
                "  }\n" +
                "]";

        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(json));
        Response response = signedCoinKeeperApiClient.queryWalletAddress("--phone-hash--");
        List<AddressLookupResult> results = (List<AddressLookupResult>) response.body();
        AddressLookupResult result = results.get(0);

        assertThat(result.getAddress(), equalTo("1JbJbAkCXtxpko39nby44hpPenpC1xKGYw"));
        assertThat(result.getPhoneNumberHash(), equalTo("498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49"));
        assertThat(result.getAddressPubKey(), equalTo("04cf39eab1213ad4a94e755fadaac4c8f2a256d7fa6b4044c7980113f7df60e24d5c1156b794d46652de2493013c6495469fbbac39d8c86495f1eebd65c7a6bddc"));
    }

    @Test
    public void find_coin_ninja_users_for_contacts() {
        ArgumentCaptor<JsonObject> captor = ArgumentCaptor.forClass(JsonObject.class);
        when(client.queryUsers(any(JsonObject.class))).thenReturn(mock(Call.class));
        SignedCoinKeeperApiClient apiClient = new SignedCoinKeeperApiClient(client, FCM_APP_ID);
        String contact1Hash = "df7c846cd38e5af8c94985e4ad1a699a08d5dfe11afc12b3a5aa67d3cd604a16";
        String contact2Hash = "760bb6b0bb25ee44e91e5967033899dee64a7141224792ab959977a8f1e6acda";
        Contact contact1 = mock(Contact.class);
        Contact contact2 = mock(Contact.class);
        when(contact1.getHash()).thenReturn(contact1Hash);
        when(contact2.getHash()).thenReturn(contact2Hash);
        ArrayList<Contact> contacts = new ArrayList<Contact>();
        contacts.add(contact1);
        contacts.add(contact2);

        String json = "{" +
                "  \"query\": {" +
                "    \"terms\": {" +
                "      \"phone_number_hash\": [" +
                "        \"" + contact1Hash + "\"," +
                "        \"" + contact2Hash + "\"" +
                "      ]" +
                "    }" +
                "  }" +
                "}";

        apiClient.fetchContactStatus(contacts);
        verify(client).queryUsers(captor.capture());

        assertThat(captor.getValue().toString(), equalTo(json.replaceAll("\\s", "")));
    }

    public void sends_address_to_cn_for_contact_payments() {
        String json = "{\n" +
                "  \"id\": \"6d1d7318-81b9-492c-b3f3-9d1b24f91d14\",\n" +
                "  \"created_at\": \"2018-05-09T16:09:05.294Z\",\n" +
                "  \"updated_at\": \"2018-05-19T16:09:05.294Z\",\n" +
                "  \"address\": \"1JbJbAkCXtxpko39nby44hpPenpC1xKGYw\",\n" +
                "  \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
                "}";
        webServer.enqueue(new MockResponse().setResponseCode(201).setBody(json));
        Response response = signedCoinKeeperApiClient.addAddress("1JbJbAkCXtxpko39nby44hpPenpC1xKGYw");
        assertThat(response.code(), equalTo(201));
        CNWalletAddress walletAddress = (CNWalletAddress) response.body();

        assertThat(walletAddress.getAddress(), equalTo("1JbJbAkCXtxpko39nby44hpPenpC1xKGYw"));
        assertThat(walletAddress.getId(), equalTo("6d1d7318-81b9-492c-b3f3-9d1b24f91d14"));
        assertThat(walletAddress.getCreatedAt(), equalTo("2018-05-09T16:09:05.294Z"));
        assertThat(walletAddress.getUpdateAt(), equalTo("2018-05-19T16:09:05.294Z"));
        assertThat(walletAddress.getWalletId(), equalTo("f8e8c20e-ba44-4bac-9a96-44f3b7ae955d"));

    }

    @Test
    public void fetches_addresses_for_wallet() {
        String json = "[\n" +
                "  {\n" +
                "    \"id\": \"6d1d7318-81b9-492c-b3f3-9d1b24f91d14\",\n" +
                "    \"created_at\": 1531921356,\n" +
                "    \"updated_at\": 1531921357,\n" +
                "    \"address\": \"1JbJbAkCXtxpko39nby44hpPenpC1xKGYw\",\n" +
                "    \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
                "  }\n" +
                "]";

        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(json));
        Response response = signedCoinKeeperApiClient.getCNWalletAddresses();
        assertThat(response.code(), equalTo(200));
        List<CNWalletAddress> cnWalletAddresses = (List<CNWalletAddress>) response.body();

        assertThat(cnWalletAddresses.size(), equalTo(1));
        CNWalletAddress cnWalletAddress = cnWalletAddresses.get(0);
        assertThat(cnWalletAddress.getAddress(), equalTo("1JbJbAkCXtxpko39nby44hpPenpC1xKGYw"));
        assertThat(cnWalletAddress.getId(), equalTo("6d1d7318-81b9-492c-b3f3-9d1b24f91d14"));
        assertThat(cnWalletAddress.getCreatedAt(), equalTo(1531921356000L));
        assertThat(cnWalletAddress.getUpdateAt(), equalTo(1531921357000L));
        assertThat(cnWalletAddress.getWalletId(), equalTo("f8e8c20e-ba44-4bac-9a96-44f3b7ae955d"));
    }

    @Test
    public void posts_phone_code_for_account_confirmation() {
        String code = "012045";
        String json = "{\n" +
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
                "}";

        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(json));
        CNUserIdentity identity = new CNUserIdentity();
        identity.setCode(code);
        identity.setType("phone");
        identity.setIdentity("13305551111");
        Response response = signedCoinKeeperApiClient.verifyIdentity(identity);
        assertThat(response.code(), equalTo(200));
        CNUserAccount account = (CNUserAccount) response.body();
        assertThat(account.getStatus(), equalTo("verified"));
        assertThat(account.getIdentities().size(), equalTo(1));
        assertThat(account.getIdentities().get(0).getIdentity(), equalTo("13305551212"));
        assertThat(account.getIdentities().get(0).getType(), equalTo("phone"));
    }

    @Test
    public void posts_phone_code_for_account_confirmation__route() {
        when(client.resendVerification(any(CNPhoneNumber.class))).thenReturn(mock(Call.class));
        CNPhoneNumber CNPhoneNumber = new CNPhoneNumber();
        CNPhoneNumber.setCountryCode(1);
        CNPhoneNumber.setPhoneNumber("330-555-5555");
        SignedCoinKeeperApiClient apiClient = new SignedCoinKeeperApiClient(client, FCM_APP_ID);

        apiClient.resendVerification(CNPhoneNumber);

        verify(client).resendVerification(CNPhoneNumber);
    }

    @Test
    public void posts_phonenumber_to_api_for_verification___route() {
        when(client.createUserAccount(any(CNPhoneNumber.class))).thenReturn(mock(Call.class));
        CNPhoneNumber CNPhoneNumber = new CNPhoneNumber();
        CNPhoneNumber.setCountryCode(1);
        CNPhoneNumber.setPhoneNumber("330-555-5555");
        SignedCoinKeeperApiClient apiClient = new SignedCoinKeeperApiClient(client, FCM_APP_ID);

        apiClient.registerUserAccount(CNPhoneNumber);
        verify(client).createUserAccount(CNPhoneNumber);
    }

    @Test
    public void posts_phonenumber_to_api_for_verification() {
        String json = "{\n" +
                "  \"id\": \"ad983e63-526d-4679-a682-c4ab052b20e1\",\n" +
                "  \"phone_number_hash\": \"498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49\",\n" +
                "  \"created_at\": 1531921356,\n" +
                "  \"updated_at\": 1531921356,\n" +
                "  \"status\": \"pending-verification\",\n" +
                "  \"verification_ttl\": 1531921356,\n" +
                "  \"verified_at\": 1531921356,\n" +
                "  \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
                "}";
        CNPhoneNumber CNPhoneNumber = new CNPhoneNumber();
        webServer.enqueue(new MockResponse().setResponseCode(201).setBody(json));
        Response response = signedCoinKeeperApiClient.registerUserAccount(CNPhoneNumber);
        assertThat(response.code(), equalTo(201));
    }

    @Test
    public void posts_pubKey_to_CN_for_account_creation() {
        when(client.createWallet(any())).thenReturn(mock(Call.class));
        when(dataSigner.getCoinNinjaVerificationKey()).thenReturn("---pub--key---");
        SignedCoinKeeperApiClient apiClient = new SignedCoinKeeperApiClient(client, FCM_APP_ID);

        apiClient.registerWallet(dataSigner.getCoinNinjaVerificationKey());

        JsonObject json = new JsonObject();
        json.addProperty("public_key_string", "---pub--key---");
        verify(client).createWallet(json);
    }

    @Test
    public void creates_cnWallet() {
        String json = "{\n" +
                "  \"id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\",\n" +
                "  \"public_key_string\": \"02262233847a69026f8f3ae027af347f2501adf008fe4f6087d31a1d975fd41473\",\n" +
                "  \"created_at\": 1531921356,\n" +
                "  \"updated_at\": 1531921356\n" +
                "}";
        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(json));
        Response response = signedCoinKeeperApiClient.registerWallet("---pub--key---");
        CNWallet cnWallet = (CNWallet) response.body();
        assertThat(response.code(), equalTo(200));
        assertThat(cnWallet.getId(), equalTo("f8e8c20e-ba44-4bac-9a96-44f3b7ae955d"));
    }

    @Test
    public void invites_phone_based_user_identity() throws InterruptedException {
        String json = "{\n" +
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
                "}";

        webServer.enqueue(new MockResponse().setResponseCode(201).setBody(json));

        InviteUserPayload payload = new InviteUserPayload(
                new Amount(120000000L, 8292280L),
                new Sender("phone", "15554441234", null),
                new Receiver("phone", "15554440000", null), "--request-id--");

        Response response = signedCoinKeeperApiClient.inviteUser(payload);
        RecordedRequest recordedRequest = webServer.takeRequest();

        assertThat(recordedRequest.getPath(), equalTo("/wallet/address_requests"));
        assertThat(response.code(), equalTo(201));
        assertThat(recordedRequest.getBody().readUtf8(), equalTo("{\"amount\":{\"btc\":120000000,\"usd\":8292280}," +
                "\"sender\":{\"type\":\"phone\",\"identity\":\"15554441234\"}," +
                "\"receiver\":{\"type\":\"phone\",\"identity\":\"15554440000\"}," +
                "\"request_id\":\"--request-id--\"}"));
    }

    @Test
    public void get_invites() {
        String json = "[\n" +
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
                "]";
        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(json));
        Response response = signedCoinKeeperApiClient.getReceivedInvites();
        List<ReceivedInvite> invites = (List<ReceivedInvite>) response.body();
        assertThat(response.code(), equalTo(200));
        assertThat(invites.size(), equalTo(1));

        ReceivedInvite invite = invites.get(0);
        assertThat(invite.getAddress(), equalTo("1JbJbAkCXtxpko39nby44hpPenpC1xKGYw"));
        assertThat(invite.getCreated_at(), equalTo(1531921356000L));
        assertThat(invite.getId(), equalTo("a1bb1d88-bfc8-4085-8966-e0062278237c"));
        assertThat(invite.getSender(), equalTo("498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49"));
        assertThat(invite.getStatus(), equalTo("waiting-response"));
    }

    @Test
    public void send_address_for_invites() {
        String addressPubKey = "9sw87rgh348hgfws8ervhw4980hfw8efgv";
        String json = "{\n" +
                "  \"id\": \"6d1d7318-81b9-492c-b3f3-9d1b24f91d14\",\n" +
                "  \"created_at\": 1531921356,\n" +
                "  \"updated_at\": 1531921357,\n" +
                "  \"address\": \"1JbJbAkCXtxpko39nby44hpPenpC1xKGYw\",\n" +
                "  \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\",\n" +
                "  \"address_pubkey\": \"f8e8c20drg978w48gher44f3b7ae955d\"\n" +
                "}";

        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(json));
        Response response = signedCoinKeeperApiClient.sendAddressForInvite("6d1d7318-81b9-492c-b3f3-9d1b24f91d14", "1JbJbAkCXtxpko39nby44hpPenpC1xKGYw", addressPubKey);
        CNWalletAddress cnWalletAddress = (CNWalletAddress) response.body();

        assertThat(response.code(), equalTo(200));
        assertThat(cnWalletAddress.getAddress(), equalTo("1JbJbAkCXtxpko39nby44hpPenpC1xKGYw"));
        assertThat(cnWalletAddress.getCreatedAt(), equalTo(1531921356000L));
        assertThat(cnWalletAddress.getId(), equalTo("6d1d7318-81b9-492c-b3f3-9d1b24f91d14"));
    }

    @Test
    public void update_a_sent_invite_with_tx_id_and_mark_completed_test() {
        String json = "{\n" +
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
                "}";

        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(json));
        Response response = signedCoinKeeperApiClient.updateInviteStatusCompleted("a1bb1d88-bfc8-4085-8966-e0062278237c", "7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03");
        assertThat(response.code(), equalTo(200));
        SentInvite sentInvite = (SentInvite) response.body();

        assertThat(sentInvite.getStatus(), equalTo("completed"));
        assertThat(sentInvite.getId(), equalTo("a1bb1d88-bfc8-4085-8966-e0062278237c"));
        assertThat(sentInvite.getTxid(), equalTo("7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03"));
        assertThat(sentInvite.getMetadata().getSender().getType(), equalTo("phone"));
        assertThat(sentInvite.getMetadata().getSender().getIdentity(), equalTo("15554441234"));
        assertNull(sentInvite.getMetadata().getSender().getHandle());
        assertThat(sentInvite.getMetadata().getReceiver().getType(), equalTo("twitter"));
        assertThat(sentInvite.getMetadata().getReceiver().getIdentity(), equalTo("15554441234"));
        assertThat(sentInvite.getMetadata().getReceiver().getHandle(), equalTo("myTwitterHandle"));
    }

    @Test
    public void update_a_sent_invite_mark_as_canceled_test() {
        String json = "{\n" +
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
                "}";

        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(json));
        Response response = signedCoinKeeperApiClient.updateInviteStatusCanceled("a1bb1d88-bfc8-4085-8966-e0062278237c");
        assertThat(response.code(), equalTo(200));
        SentInvite sentInvite = (SentInvite) response.body();

        assertThat(sentInvite.getStatus(), equalTo("canceled"));
    }

    @Test
    public void recevier_of_an_invite_sends_an_address_to_the_server_test() {
        String addressPubKey = "9sw87rgh348hgfws8ervhw4980hfw8efgv";
        String json = "{\n" +
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
                "}";

        String addressToSend = "1JbJbAkCXtxpko39nby44hpPenpC1xKGYw";
        String inviteID = "a1bb1d88-bfc8-4085-8966-e0062278237c";

        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(json));
        Response response = signedCoinKeeperApiClient.sendAddressForInvite(inviteID, addressToSend, addressPubKey);


        assertThat(response.code(), equalTo(200));
        CNWalletAddress sentInvite = (CNWalletAddress) response.body();
        assertThat(sentInvite.getAddress(), equalTo("1JbJbAkCXtxpko39nby44hpPenpC1xKGYw"));
    }

    @Test
    public void get_all_sent_invites_test() {

        String json = "[\n" +
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
                "]";


        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(json));
        Response response = signedCoinKeeperApiClient.getSentInvites();


        assertThat(response.code(), equalTo(200));
        List<SentInvite> sentInvites = (List<SentInvite>) response.body();
        assertThat(sentInvites.size(), equalTo(1));
        SentInvite invite = sentInvites.get(0);
        assertThat(invite.getId(), equalTo("a1bb1d88-bfc8-4085-8966-e0062278237c"));
        assertThat(invite.getStatus(), equalTo("completed"));
        assertThat(invite.getTxid(), equalTo("7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03"));
    }

    @Test
    public void get_all_incoming_invites_test() {
        String json = "[\n" +
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
                "]";


        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(json));
        Response response = signedCoinKeeperApiClient.getReceivedInvites();


        assertThat(response.code(), equalTo(200));
        List<ReceivedInvite> receivedInvites = (List<ReceivedInvite>) response.body();
        assertThat(receivedInvites.size(), equalTo(1));
        ReceivedInvite invite = receivedInvites.get(0);
        assertThat(invite.getId(), equalTo("a1bb1d88-bfc8-4085-8966-e0062278237c"));
        assertThat(invite.getSender(), equalTo("498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49"));
        assertThat(invite.getStatus(), equalTo("waiting-response"));
        assertThat(invite.getAddress(), equalTo("1JbJbAkCXtxpko39nby44hpPenpC1xKGYw"));
    }

    @Test
    public void manufacture_a_custom_error_test() {

        Response response = signedCoinKeeperApiClient.createUpdateInviteStatusError("id", "some error");

        assertThat(response.message(), equalTo("some error"));
    }

    @Test
    public void verify_when_creating_a_cn_device_using_the_overloaded_method_that_the_post_request_body_is_correct_test() {
        String sampleUuid = "--- UUID --- ";
        ArgumentCaptor<JsonObject> captor = ArgumentCaptor.forClass(JsonObject.class);
        when(client.createCNDevice(any(JsonObject.class))).thenReturn(mock(Call.class));
        SignedCoinKeeperApiClient apiClient = new SignedCoinKeeperApiClient(client, FCM_APP_ID);

        apiClient.createCNDevice(sampleUuid);
        verify(client).createCNDevice(captor.capture());
        JsonObject requestBody = (JsonObject) new JsonParser().parse(captor.getValue().toString());


        String expectedAppName = CN_API_CREATE_DEVICE_APPLICATION_NAME;
        CNDevice.DevicePlatform expectedDevicePlatform = CNDevice.DevicePlatform.ANDROID;
        assertThat(requestBody.get(CN_API_CREATE_DEVICE_APPLICATION_KEY).getAsString(), equalTo(expectedAppName));
        assertThat(requestBody.get(CN_API_CREATE_DEVICE_PLATFORM_KEY).getAsString(), equalTo(expectedDevicePlatform.toString()));
        assertThat(requestBody.get(CN_API_CREATE_DEVICE_UUID_KEY).getAsString(), equalTo(sampleUuid));
    }

    @Test
    public void verify_response_body_is_correct_when_creating_an_cn_device_test() {
        String json = "{\n" +
                "  \"id\": \"158a4cf9-0362-4636-8c68-ed7a98a7f345\",\n" +
                "  \"created_at\": 1531921356,\n" +
                "  \"updated_at\": 1531921355,\n" +
                "  \"application\": \"DropBit\",\n" +
                "  \"platform\": \"android\",\n" +
                "  \"uuid\": \"998207d6-5b1e-47c9-84e9-895f52a1b455\"\n" +
                "}";


        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(json));
        Response response = signedCoinKeeperApiClient.createCNDevice("--- some UUID ---");
        CNDevice cnDevice = (CNDevice) response.body();

        assertThat(cnDevice.getId(), equalTo("158a4cf9-0362-4636-8c68-ed7a98a7f345"));
        assertThat(cnDevice.getCreatedDate(), equalTo(1531921356000l));
        assertThat(cnDevice.getUpdatedDate(), equalTo(1531921355000l));
        assertThat(cnDevice.getApplicationName(), equalTo("DropBit"));
        assertThat(cnDevice.getPlatform(), equalTo(CNDevice.DevicePlatform.ANDROID));
        assertThat(cnDevice.getUuid(), equalTo("998207d6-5b1e-47c9-84e9-895f52a1b455"));
    }

    @Test
    public void verity_registers_endpoint() {
        ArgumentCaptor<JsonObject> captor = ArgumentCaptor.forClass(JsonObject.class);
        CoinKeeperClient client = mock(CoinKeeperClient.class);
        SignedCoinKeeperApiClient apiClient = new SignedCoinKeeperApiClient(client, FCM_APP_ID);
        when(client.registerForPushEndpoint(any(), any())).thenReturn(mock(Call.class));

        String cnDeviceId = "-- cn device id";
        String pushToken = "-- push token";

        apiClient.registerForPushEndpoint(cnDeviceId, pushToken);

        verify(client).registerForPushEndpoint(eq(cnDeviceId)
                , captor.capture());

        JsonObject body = captor.getValue();

        assertThat(body.get("application").getAsString(), equalTo(FCM_APP_ID));
        assertThat(body.get("platform").getAsString(), equalTo("GCM"));
        assertThat(body.get("token").getAsString(), equalTo(pushToken));
    }

    @Test
    public void consume_the_register_for_push_end_point() {
        String json = "{\n" +
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
                "}";

        String cnDeviceId = "-- cn device id";
        String pushToken = "-- push token";

        webServer.enqueue(new MockResponse().setResponseCode(201).setBody(json));
        Response response = signedCoinKeeperApiClient.registerForPushEndpoint(cnDeviceId, pushToken);
        CNDeviceEndpoint cnDeviceEndpoint = (CNDeviceEndpoint) response.body();

        assertThat(response.code(), equalTo(201));
        assertThat(cnDeviceEndpoint.getID(), equalTo("5805b3a0-ed99-4073-ad18-72adff181b9e"));
    }

    @Test
    public void unregisters_endpoint_for_device_id() {
        String deviceId = "--device";
        String endpoint = "--endpoint";
        webServer.enqueue(new MockResponse().setResponseCode(204));

        Response response = signedCoinKeeperApiClient.unRegisterDeviceEndpoint(deviceId, endpoint);

        assertThat(response.code(), equalTo(204));
    }

    @Test
    public void fetches_device_endpoints() {
        String deviceId = "--device";
        String json = "[\n" +
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
                "]";

        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(json));

        Response response = signedCoinKeeperApiClient.fetchRemoteEndpointsFor(deviceId);
        List<CNDeviceEndpoint> endpoints = (List<CNDeviceEndpoint>) response.body();

        assertThat(endpoints.size(), equalTo(1));
        assertThat(endpoints.get(0).getID(), equalTo("5805b3a0-ed99-4073-ad18-72adff181b9e"));
    }

    @Test
    public void builds_subscription_to_wallet_request() {
        ArgumentCaptor<JsonObject> captor = ArgumentCaptor.forClass(JsonObject.class);
        CoinKeeperClient client = mock(CoinKeeperClient.class);
        SignedCoinKeeperApiClient apiClient = new SignedCoinKeeperApiClient(client, FCM_APP_ID);
        String endpointId = "-- endpoint ";

        when(client.subscribeToWalletTopic(any())).thenReturn(mock(Call.class));

        apiClient.subscribeToWalletNotifications(endpointId);

        verify(client).subscribeToWalletTopic(captor.capture());

        JsonObject body = captor.getValue();
        assertThat(body.get("device_endpoint_id").getAsString(), equalTo(endpointId));
    }

    @Test
    public void subscribes_to_wallet_topics() {
        String json = "{\n" +
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
                "}";

        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(json));
        String endpointId = "--endpoint id";

        Response response = signedCoinKeeperApiClient.subscribeToWalletNotifications(endpointId);
        CNSubscription subscription = (CNSubscription) response.body();

        assertThat(subscription.getId(), equalTo("--topic id 1"));
    }

    @Test
    public void builds_update_subscription_to_wallet_request() {
        ArgumentCaptor<JsonObject> captor = ArgumentCaptor.forClass(JsonObject.class);
        CoinKeeperClient client = mock(CoinKeeperClient.class);
        SignedCoinKeeperApiClient apiClient = new SignedCoinKeeperApiClient(client, FCM_APP_ID);
        String endpointId = "-- endpoint ";

        when(client.updateWalletSubscription(any())).thenReturn(mock(Call.class));

        apiClient.updateWalletSubscription(endpointId);

        verify(client).updateWalletSubscription(captor.capture());

        JsonObject body = captor.getValue();
        assertThat(body.get("device_endpoint_id").getAsString(), equalTo(endpointId));
    }

    @Test
    public void updates_wallet_topics() {
        String json = "{\n" +
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
                "}";

        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(json));
        String endpointId = "--endpoint id";

        Response response = signedCoinKeeperApiClient.updateWalletSubscription(endpointId);
        CNSubscription subscription = (CNSubscription) response.body();

        assertThat(subscription.getId(), equalTo("--topic id 1"));
    }

    @Test
    public void subscribes_to_generic_topics() throws InterruptedException {
        String json = "[{\n" +
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
                "}]";

        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(json));

        String deviceId = "--device-id";
        String endpointId = "--endpoint-id";

        List<String> topics = new ArrayList<>();
        topics.add("--topic id 1");
        topics.add("--topic id 2");
        CNTopicSubscription topicSubscription = new CNTopicSubscription(topics);

        Response response = signedCoinKeeperApiClient.subscribeToTopics(deviceId, endpointId, topicSubscription);
        RecordedRequest recordedRequest = webServer.takeRequest();

        assertThat(recordedRequest.getPath(), equalTo("/devices/--device-id/endpoints/--endpoint-id/subscriptions"));
        assertThat(recordedRequest.getMethod(), equalTo("POST"));
        assertThat(response.code(), equalTo(200));
        assertThat(recordedRequest.getBody().readUtf8(), equalTo("{\"topic_ids\":[\"--topic id 1\",\"--topic id 2\"]}"));
    }

    @Test
    public void unsubscribe_from_topic() throws InterruptedException {
        String deviceId = "--device-id--";
        String endpointId = "--endpoint-id--";
        String topicId = "--topic-id--";
        webServer.enqueue(new MockResponse().setResponseCode(200));

        Response response = signedCoinKeeperApiClient.unsubscribeFromTopic(deviceId, endpointId, topicId);
        RecordedRequest recordedRequest = webServer.takeRequest();

        assertThat(recordedRequest.getPath(), equalTo("/devices/--device-id--/endpoints/--endpoint-id--/subscriptions/--topic-id--"));
        assertThat(recordedRequest.getMethod(), equalTo("DELETE"));
        assertThat(response.code(), equalTo(200));
        assertThat(recordedRequest.getBody().readUtf8(), equalTo(""));
    }

    @Test
    public void fetches_general_topics() {
        String json = "{\n" +
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
                "}";

        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(json));
        Response resposne = signedCoinKeeperApiClient.fetchDeviceEndpointSubscriptions("-deviceId", "-endpointId");

        assertThat(resposne.code(), equalTo(200));
        CNSubscriptionState subscriptionState = (CNSubscriptionState) resposne.body();
        assertNotNull(subscriptionState.getAvailableTopics());
        assertNotNull(subscriptionState.getSubscriptions());
        assertThat(subscriptionState.getSubscriptions().get(0).getId(),
                equalTo("8a8b9a55-fe26-49bf-9e01-a81cc0bb7a66"));
        assertThat(subscriptionState.getSubscriptions().get(0).getOwnerId(),
                equalTo("ba3f2126-3e1a-47b7-982a-8caccba56e3d"));
        assertThat(subscriptionState.getSubscriptions().get(0).getOwnerType(),
                equalTo("Topic"));


        assertThat(subscriptionState.getAvailableTopics().get(0).getId(),
                equalTo("ba3f2126-3e1a-47b7-982a-8caccba56e3d"));

    }

    @Test
    public void posts_transaction_notification_passes_response_through() throws InterruptedException {

        String json = "{}";
        MockResponse expectedResponse = new MockResponse().setResponseCode(200).setBody(json);
        webServer.enqueue(expectedResponse);

        String encryptedPayload = "ENCReYaw54987thq3Ptyer9t8hga35g87h98ae54yhg9ert85u0q9834ty80";
        String phoneNumberHash = "sau4yre6zto8uhj430a8jtw497ethyu39q78hyta908puyth89sa4ht893shyts98e45y";
        String address = "q";
        String txid = "42";

        Response response = signedCoinKeeperApiClient.postTransactionNotification(new CNSharedMemo(txid, address, phoneNumberHash, encryptedPayload, "2"));
        RecordedRequest recordedRequest = webServer.takeRequest();

        assertThat(recordedRequest.getPath(), equalTo("/transaction/notification"));
        assertThat(recordedRequest.getBody().readUtf8(), equalTo("{\"txid\":\"42\",\"address\":\"q\",\"identity_hash\":\"sau4yre6zto8uhj430a8jtw497ethyu39q78hyta908puyth89sa4ht893shyts98e45y\",\"encrypted_payload\":\"ENCReYaw54987thq3Ptyer9t8hga35g87h98ae54yhg9ert85u0q9834ty80\",\"encrypted_format\":\"2\"}"));
        assertThat(response.code(), equalTo(200));
    }

    @Test
    public void get_transaction_notification() throws InterruptedException {
        String txid = "7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03";
        String address = "34Xa8X8pfwvUyq4VGZFXUhzrTemJrbgcsu";
        String encryptedPayload = "Y2QyM2M4ZjY4MTU4ZjhiYmRjZDMzN2E1ZjI0NTE2MjBkNmY3ZjNhMjc5MGQ1OTg1M2ZkYzYyMGI4NzZkNGUwMzgxNThmOGJiZGNkNzIzM2ViN2EzM2IK";
        String format = "1";

        String json = "[\n" +
                "  {\n" +
                "    \"txid\": \"" + txid + "\",\n" +
                "    \"address\": \"" + address + "\",\n" +
                "    \"encrypted_payload\": \"" + encryptedPayload + "\",\n" +
                "    \"encrypted_format\": \"" + format + "\"\n" +
                "  }\n" +
                "]";
        MockResponse expectedResponse = new MockResponse().setResponseCode(200).setBody(json);
        webServer.enqueue(expectedResponse);

        Response response = signedCoinKeeperApiClient.getTransactionNotification(txid);

        CNSharedMemo memo = ((List<CNSharedMemo>) response.body()).get(0);

        assertThat(memo.getTxid(), equalTo(txid));
        assertThat(memo.getAddress(), equalTo(address));
        assertThat(memo.getEncrypted_payload(), equalTo(encryptedPayload));
        assertThat(memo.getEncrypted_format(), equalTo(format));
        RecordedRequest recordedRequest = webServer.takeRequest();
        assertThat(recordedRequest.getPath(), equalTo("/transaction/notification/" + txid));
    }

    @Test
    public void enables_dropbit_me_account() throws InterruptedException {
        String json = "{\n" +
                "  \"id\": \"ad983e63-526d-4679-a682-c4ab052b20e1\",\n" +
                "  \"created_at\": 1531921356,\n" +
                "  \"updated_at\": 1531921356,\n" +
                "  \"status\": \"pending-verification\",\n" +
                "  \"private\": false,\n" +
                "  \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
                "}";
        MockResponse expectedResponse = new MockResponse().setResponseCode(200).setBody(json);
        webServer.enqueue(expectedResponse);

        Response response = signedCoinKeeperApiClient.enableDropBitMeAccount();

        RecordedRequest recordedRequest = webServer.takeRequest();
        assertThat(recordedRequest.getPath(), equalTo("/user"));
        assertThat(recordedRequest.getBody().readUtf8(), equalTo("{\"private\":false}"));
        CNUserPatch userPatch = (CNUserPatch) response.body();
        assert userPatch != null;
        assertFalse(userPatch.isPrivate());
    }

    @Test
    public void disables_dropbit_me_account() throws InterruptedException {
        String json = "{\n" +
                "  \"id\": \"ad983e63-526d-4679-a682-c4ab052b20e1\",\n" +
                "  \"created_at\": 1531921356,\n" +
                "  \"updated_at\": 1531921356,\n" +
                "  \"status\": \"pending-verification\",\n" +
                "  \"private\": true,\n" +
                "  \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
                "}";
        MockResponse expectedResponse = new MockResponse().setResponseCode(200).setBody(json);
        webServer.enqueue(expectedResponse);

        Response response = signedCoinKeeperApiClient.disableDropBitMeAccount();

        RecordedRequest recordedRequest = webServer.takeRequest();
        assertThat(recordedRequest.getPath(), equalTo("/user"));
        assertThat(recordedRequest.getBody().readUtf8(), equalTo("{\"private\":true}"));
        CNUserPatch userPatch = (CNUserPatch) response.body();
        assert userPatch != null;
        assertTrue(userPatch.isPrivate());
    }

    @Test
    public void deletes_user_identity() throws InterruptedException {
        MockResponse expectedResponse = new MockResponse().setResponseCode(204).setBody("");
        webServer.enqueue(expectedResponse);
        DropbitMeIdentity identity = mock(DropbitMeIdentity.class);
        when(identity.getServerId()).thenReturn("--server-id--");

        Response response = signedCoinKeeperApiClient.deleteIdentity(identity);

        RecordedRequest recordedRequest = webServer.takeRequest();
        assertThat(recordedRequest.getPath(), equalTo("/user/identity/--server-id--"));
        assertThat(response.code(), equalTo(204));
    }

    @Test
    public void fetches_identities() throws InterruptedException {
        String json = "[\n" +
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
                "]";
        MockResponse expectedResponse = new MockResponse().setResponseCode(200).setBody(json);
        webServer.enqueue(expectedResponse);

        Response response = signedCoinKeeperApiClient.getIdentities();
        List<CNUserIdentity> identities = (List<CNUserIdentity>) response.body();
        RecordedRequest recordedRequest = webServer.takeRequest();

        assertThat(recordedRequest.getPath(), equalTo("/user/identity"));
        assertThat(response.code(), equalTo(200));
        assertThat(identities.size(), equalTo(1));
        assertThat(identities.get(0).getId(), equalTo("ad983e63-526d-4679-a682-c4ab052b20e1"));
        assertThat(identities.get(0).getType(), equalTo("phone"));
        assertThat(identities.get(0).getIdentity(), equalTo("13305551212"));
        assertThat(identities.get(0).getHash(), equalTo("498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49"));
    }

    @Test
    public void adds_identity() throws InterruptedException {
        String json = "  {\n" +
                "    \"id\": \"ad983e63-526d-4679-a682-c4ab052b20e1\",\n" +
                "    \"created_at\": 1531921356,\n" +
                "    \"updated_at\": 1531921356,\n" +
                "    \"type\": \"phone\",\n" +
                "    \"identity\": \"13305551212\",\n" +
                "    \"handle\": \"498803d5964a\",\n" +
                "    \"hash\": \"498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49\",\n" +
                "    \"status\": \"pending-verification\"\n" +
                "  }\n";
        MockResponse expectedResponse = new MockResponse().setResponseCode(200).setBody(json);
        webServer.enqueue(expectedResponse);
        CNUserIdentity identity = new CNUserIdentity();

        Response response = signedCoinKeeperApiClient.addIdentity(identity);
        identity = (CNUserIdentity) response.body();
        RecordedRequest recordedRequest = webServer.takeRequest();

        assertThat(recordedRequest.getPath(), equalTo("/user/identity"));
        assertThat(recordedRequest.getMethod(), equalTo("POST"));
        assertThat(response.code(), equalTo(200));
        assertThat(identity.getId(), equalTo("ad983e63-526d-4679-a682-c4ab052b20e1"));
        assertThat(identity.getType(), equalTo("phone"));
        assertThat(identity.getIdentity(), equalTo("13305551212"));
        assertThat(identity.getHash(), equalTo("498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49"));
    }

    @Test
    public void verifies_identity() throws InterruptedException {
        String json = "{\n" +
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
                "}";

        MockResponse expectedResponse = new MockResponse().setResponseCode(200).setBody(json);
        webServer.enqueue(expectedResponse);
        CNUserIdentity identity = new CNUserIdentity();

        Response response = signedCoinKeeperApiClient.verifyIdentity(identity);
        CNUserAccount account = (CNUserAccount) response.body();
        RecordedRequest recordedRequest = webServer.takeRequest();

        assertThat(recordedRequest.getPath(), equalTo("/user/verify"));
        assertThat(recordedRequest.getMethod(), equalTo("POST"));
        assertThat(response.code(), equalTo(200));
        assertThat(account.getIdentities().get(0).getId(), equalTo("ad983e63-526d-4679-a682-c4ab052b20e1"));
        assertThat(account.getIdentities().get(0).getType(), equalTo("phone"));
        assertThat(account.getIdentities().get(0).getIdentity(), equalTo("13305551212"));
        assertThat(account.getIdentities().get(0).getHash(), equalTo("498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49"));
    }

    @Test
    public void create_user_from_identity() throws InterruptedException {
        String json = "{\n" +
                "  \"id\": \"ad983e63-526d-4679-a682-c4ab052b20e1\",\n" +
                "  \"created_at\": 1531921356,\n" +
                "  \"updated_at\": 1531921356,\n" +
                "  \"status\": \"pending-verification\",\n" +
                "  \"private\": true,\n" +
                "  \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
                "}";

        MockResponse expectedResponse = new MockResponse().setResponseCode(200).setBody(json);
        webServer.enqueue(expectedResponse);
        CNUserIdentity identity = new CNUserIdentity();

        Response response = signedCoinKeeperApiClient.createUserFromIdentity(identity);
        CNUserAccount account = (CNUserAccount) response.body();
        RecordedRequest recordedRequest = webServer.takeRequest();

        assertThat(recordedRequest.getPath(), equalTo("/user"));
        assertThat(recordedRequest.getMethod(), equalTo("POST"));
        assertThat(response.code(), equalTo(200));
        assertThat(account.getId(), equalTo("ad983e63-526d-4679-a682-c4ab052b20e1"));
        assertThat(account.getStatus(), equalTo("pending-verification"));
        assertThat(account.isPrivate(), equalTo(true));
        assertThat(account.getWallet_id(), equalTo("f8e8c20e-ba44-4bac-9a96-44f3b7ae955d"));
    }

    private SignedCoinKeeperApiClient createClient(String host) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.connectTimeout(100, TimeUnit.MILLISECONDS)
                .writeTimeout(100, TimeUnit.MILLISECONDS)
                .readTimeout(100, TimeUnit.MILLISECONDS);

        CoinKeeperClient client = new Retrofit.Builder().
                baseUrl(host).
                client(clientBuilder.build()).
                addConverterFactory(GsonConverterFactory.create()).
                build().create(CoinKeeperClient.class);

        return new SignedCoinKeeperApiClient(client, FCM_APP_ID);
    }

}